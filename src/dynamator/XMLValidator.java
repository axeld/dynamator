/***********************************************************************
*   Copyright 2000-2004 by Jay Dunning.
*   All rights reserved.
*
*   Redistribution and use in source and binary forms, with or without
*   modification, are permitted provided that the following conditions 
*   are met:
*
*   1.  Redistributions of source code must retain the above copyright
*       notice, this list of conditions, and the following disclaimer.
*
*   2.  Redistributions in binary form must reproduce the above
*       copyright notice, this list of conditions, and the following
*       disclaimer in the documentation and/or other materials provided
*       with the distribution.
*
*   THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
*   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
*   OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*   DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR ANY CONTRIBUTORS
*   BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
*   OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
*   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
*   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
*   OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
*   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
*   USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF 
*   SUCH DAMAGE.
***********************************************************************/

package dynamator;

import java.util.StringTokenizer;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
    Simple XML validator; good enough for Dynamator.  
    Dynamator files can't be validated using a DTD, because
    tag elements can take any attributes.
**/
public
class XMLValidator
{
    public static
    String
    validateNmtoken(
        String s
        ) 
    {
        int i = s.length();
        if ( i == 0 )
        {
            return "Value is empty";
        }
        char c;
        while ( --i >= 0 )
        {
            c = s.charAt(i);
            if ( ! ( Character.isLetterOrDigit(c)
                    || c == '.'
                    || c == '-'
                    || c == '_'
                    || c == ':' 
                    // omitting combining chars and extenders
                    ) 
               )
            {
                return "Value is not a name token: '" + s + "'";
            }
        }

        return null;
    }        

    public static
    String
    validateNmtokens(
        String s
        ) 
    {
        StringTokenizer tokenizer = new StringTokenizer(s);
        if ( ! tokenizer.hasMoreTokens() )
        {
            return "Value is empty";
        }
        
        String message = null;
        while ( tokenizer.hasMoreTokens()
            && (message = validateNmtoken(tokenizer.nextToken())) != null )
        {}

        return message;
    }        

    private static
    class Locator
    implements org.xml.sax.Locator
    {
        private String fileName_;
        private int lineNumber_;
        
        public
        Locator(
            String location
            )
        {
            int p = location.lastIndexOf(':');
            fileName_ = location.substring(0, p);
            lineNumber_ = Integer.parseInt(location.substring(p+1));
        }
            
        public
        int
        getColumnNumber()
        {
            return -1;
        }
        
        public
        int
        getLineNumber()
        {
            return lineNumber_;
        }
        
        public
        String
        getPublicId()
        {
            return null;
        }

        public
        String
        getSystemId()
        {
            return fileName_;
        }
    }
    
    public static
    class AttrSpec
    {
        public 
        interface Type
        {
            String
            validate(
                String s
                );
        }

        private static
        class NonEmptyType
        implements Type
        {
            public
            String
            validate(
                String s
                )
            {
                return s == null || s.length() == 0
                    ? "A value is required"
                    : null;
            }
        }
        public static final Type nonEmpty = new NonEmptyType();

        private static
        class UnspecifiedType
        implements Type
        {
            public
            String
            validate(
                String s
                )
            {
                return null;
            }
        }
        public static final Type unspecified = new UnspecifiedType();

        private static
        class NmtokenType
        implements Type
        {
            public
            String
            validate(
                String s
                )
            {
                return validateNmtoken(s);
            }
        }
        public static final Type nmtoken = new NmtokenType();

        private static
        class NmtokensType
        implements Type
        {
            public
            String
            validate(
                String s
                )
            {
                return validateNmtokens(s);
            }
        }
        public static final Type nmtokens = new NmtokensType();

        private static
        class EnumeratedValueType
        implements Type
        {
            String[] values_;
            String message_;

            public
            EnumeratedValueType(
                String[] values
                )
            {
                values_ = values;

                StringBuffer buf = new StringBuffer();
                int i = -1;
                while ( ++i < values.length )
                {
                    buf.append(values[i]);
                    buf.append(' ');
                }
                message_ = buf.toString();
            }

            public
            String
            validate(
                String s
                )
            {
                int i = values_.length;
                while ( --i >= 0 )
                {
                    if ( s.equals(values_[i]) )
                    {
                        return null;
                    }
                }

                return "Value must be one of the following: " 
                    + message_;
            }
        }
        public static
        Type
        enumeratedValue(
            String valueList
            )
        {
            StringTokenizer tokenizer = new StringTokenizer(valueList);
            String[] values = new String[tokenizer.countTokens()];
            int i = -1;
            while ( tokenizer.hasMoreTokens() )
            {
                values[++i] = tokenizer.nextToken();
            }

            return new EnumeratedValueType(values);
        }
        
        private String name_;
        private Type type_;
        private boolean required_;
        
        public
        AttrSpec(
            String name,
            Type type,
            boolean required
            )
        {
            name_ = name;
            type_ = type;
            required_ = required;
        }

        public
        String
        validate(
            String value
            )
        {
            return type_.validate(value);
        }

        public
        String
        name()
        {
            return name_;
        }

        public
        boolean
        isRequired()
        {
            return required_;
        }
    }
    
    public static
    class ChildSpec
    {
        private ElementSpec elementSpec_;
        private boolean allowsMultiples_;

        public 
        ChildSpec(
            ElementSpec elementSpec,
            boolean allowsMultiples
            )
        {
            elementSpec_ = elementSpec;
            allowsMultiples_ = allowsMultiples;
        }

        public
        ElementSpec
        elementSpec()
        {
            return elementSpec_;
        }

        public
        boolean
        allowsMultiples()
        {
            return allowsMultiples_;
        }
    }
    
    public static
    class TextContent
    {
        public static final TextContent none = new TextContent();
        public static final TextContent optional = new TextContent();
        public static final TextContent required = new TextContent();
    }

    public static
    class ElementSpec
    {
        private String name_;
        private AttrSpec[] attrSpecs_;
        private ChildSpec[] children_;
        private TextContent textContent_;
        private boolean allowsUnspecifiedAttrs_;

        public
        ElementSpec(
            String name,
            AttrSpec[] attributes,
            ChildSpec[] children,
            TextContent textContent
            )
        {
            name_ = name;
            attrSpecs_ = attributes;
            children_ = children;
            textContent_ = textContent;
            allowsUnspecifiedAttrs_ = false;
        }

        public
        ElementSpec(
            String name,
            AttrSpec[] attributes,
            ChildSpec[] children,
            TextContent textContent,
            boolean allowsUnspecifiedAttrs
            )
        {
            name_ = name;
            attrSpecs_ = attributes;
            children_ = children;
            textContent_ = textContent;
            allowsUnspecifiedAttrs_ = allowsUnspecifiedAttrs;
        }

        public
        String
        name()
        {
            return name_;
        }
        
        private
        int
        findAttrSpec(
            String name
            )
        {
            int i = attrSpecs_.length;
            while ( --i >= 0 )
            {
                if ( name.equals(attrSpecs_[i].name()) )
                {
                    break;
                }
            }

            return i;
        }
        
        public
        void
        validate(
            Element element,
            ErrorHandler errors
            )
        {
            int i;

            // validate attributes:
            // - all required attributes are present
            // - no unexpected attributes are present
            // - all attribute values match specification
            
            NamedNodeMap attributes = element.getAttributes();
            
            boolean[] attrPresent = new boolean[attrSpecs_.length];
            
            i = attributes.getLength();
            while ( --i >= 0 )
            {
                Attr attr = (Attr) attributes.item(i);
                if ( "_dyn_loc_".equals(attr.getName()) )
                {
                    continue;
                }

                int iAttrSpec = findAttrSpec(attr.getName());
                if ( iAttrSpec == -1 )
                {
                    if ( ! allowsUnspecifiedAttrs_ )
                    {
                        error(errors, element, 
                            "Element '" + element.getTagName()
                            + "': attribute '" 
                            + attr.getName()
                            + "' not allowed");
                    }

                    continue;
                }
                
                attrPresent[iAttrSpec] = true;
                String message = 
                    attrSpecs_[iAttrSpec].validate(attr.getValue());
                if ( message != null )
                {
                    error(errors, element, 
                        "Element '" + element.getTagName()
                        + "', attribute '" 
                        + attrSpecs_[iAttrSpec].name()
                        + "': "
                        + message);
                }
            }
            
            i = attrSpecs_.length;
            while ( --i >= 0 )
            {
                if ( ! attrPresent[i]
                    && attrSpecs_[i].isRequired() )
                {
                    error(errors, element, 
                        "Element '" + element.getTagName()
                        + "': Required attribute not specified: " 
                        + attrSpecs_[i].name());
                }
            }

            // are there any unexpected children?
            // (No dynamator element has any specific required
            // children, so that is not tested for)
            
            int[] counts = new int[children_.length];
            
            NodeList children = element.getChildNodes();
            int nChildren = children.getLength();

            boolean hasTextContent = false;
            i = -1;
            while ( ++i < nChildren )
            {
                Node node = children.item(i);
                switch (node.getNodeType())
                {
                    case Node.COMMENT_NODE:
                    // pi's are inserted by FakeoutSaxInputStream
                    case Node.PROCESSING_INSTRUCTION_NODE:
                    {}

                    break;
                    case Node.CDATA_SECTION_NODE:
                    case Node.TEXT_NODE:
                    {
                        hasTextContent = true;
                        if ( textContent_ == TextContent.none )
                        {
                            if ( ! Utils.onlyWhitespace(node.getNodeValue()) )
                            {
                                error(errors, element, 
                                    "Element '" + element.getTagName()
                                    + "' may not have text content");
                            }
                        }
                    }
                
                    break;
                    case Node.ENTITY_REFERENCE_NODE:
                    {
                        error(errors, element,
                            "Entity references not supported");
                    }
                    
                    break;
                    case Node.ELEMENT_NODE:
                    {
                        Element child = (Element)node;
                        String name = child.getTagName();
                        ChildSpec childSpec;
                        int iChildSpec = children_.length;
                        while ( --iChildSpec >= 0 )
                        {
                            childSpec = children_[iChildSpec];
                            if ( name.equals(
                                childSpec.elementSpec().name()) )
                            {
                                if ( ++counts[iChildSpec] > 1
                                    && ! childSpec.allowsMultiples() )
                                {
                                    error(errors, child,
                                        "Element '" + name
                                        + "' may occur at most once"
                                        + " within element '" 
                                        + element.getTagName() + "'");
                                }
                                else
                                {
                                    childSpec.elementSpec().validate(
                                        child, errors);
                                }

                                break;
                            }
                        }

                        if ( iChildSpec < 0 )
                        {
                            error(errors, child,
                                "Element '" + name 
                                + "' not allowed as child of"
                                + " element '" + element.getTagName()
                                + "'");
                        }
                    }

                    break;
                    default:
                    {}
                }
            }

            if ( textContent_ == TextContent.required 
                && ! hasTextContent )
            {
                error(errors, element, 
                    "Element '" + element.getTagName()
                    + "' must have text content");
            }
        }
    }

    private static
    void
    error(
        ErrorHandler errors,
        Element element,
        String message
        )
    {
        String location = element.getAttribute("_dyn_loc_");
        if ( location == null || location.length() == 0 )
        {
            errors.error(message, new Locator("unknown:-1"));
        }
        else
        {
            errors.error(message, new Locator(location));
        }
    }

    private static ElementSpec dynamatorSpec_ = initializeDynamatorSpec();

    private static
    ElementSpec
    initializeDynamatorSpec()
    {
        AttrSpec[] nullAttrSpecs = new AttrSpec[0];
        ChildSpec[] nullChildSpecs = new ChildSpec[0];

        ElementSpec content = new ElementSpec(
            "content",
            nullAttrSpecs,
            nullChildSpecs,
            TextContent.optional
            );

        ElementSpec iff = new ElementSpec(
            "if",
            nullAttrSpecs,
            nullChildSpecs,
            TextContent.required
            );

        ElementSpec rename = new ElementSpec(
            "rename",
            new AttrSpec[]
            {
                new AttrSpec("to", AttrSpec.nonEmpty, true),
            },
            nullChildSpecs,
            TextContent.none
            );

        ElementSpec forr = new ElementSpec(
            "for",
            nullAttrSpecs,
            new ChildSpec[]
            {
                new ChildSpec(iff, false)
            },
            TextContent.required
            );

        ElementSpec foreach = new ElementSpec(
            "foreach",
            new AttrSpec[]
            {
                new AttrSpec("type", AttrSpec.nonEmpty, false),
                new AttrSpec("element", AttrSpec.nonEmpty, false),
                new AttrSpec("i", AttrSpec.nonEmpty, false),
                new AttrSpec("collection", AttrSpec.nonEmpty, false),
            },
            new ChildSpec[]
            {
                new ChildSpec(iff, false)
            },
            TextContent.required
            );

        AttrSpec[] rawElementAttrs = new AttrSpec[]
        {
            new AttrSpec("indent", 
                AttrSpec.enumeratedValue("yes no"), false),
            new AttrSpec("indent-program", 
                AttrSpec.enumeratedValue("yes no"), false)
        };
        
        ElementSpec dynamatorCopy = new ElementSpec(
            "dynamator:copy",
            new AttrSpec[]
            {
                new AttrSpec("file", AttrSpec.nonEmpty, true)
            },
            nullChildSpecs,
            TextContent.none
            );

        ElementSpec before = new ElementSpec(
            "before",
            rawElementAttrs,
            new ChildSpec[]
            {
                new ChildSpec(dynamatorCopy, true)
            },
            TextContent.required
            );
        
        ElementSpec beforeContent = new ElementSpec(
            "before-content",
            rawElementAttrs,
            new ChildSpec[]
            {
                new ChildSpec(dynamatorCopy, true)
            },
            TextContent.required
            );

        ElementSpec after = new ElementSpec(
            "after",
            rawElementAttrs,
            new ChildSpec[]
            {
                new ChildSpec(dynamatorCopy, true)
            },
            TextContent.required
            );
        
        ElementSpec afterContent = new ElementSpec(
            "after-content",
            rawElementAttrs,
            new ChildSpec[]
            {
                new ChildSpec(dynamatorCopy, true)
            },
            TextContent.required
            );
        
        ElementSpec rawContent = new ElementSpec(
            "raw-content",
            rawElementAttrs,
            new ChildSpec[]
            {
                new ChildSpec(dynamatorCopy, true)
            },
            TextContent.optional
            );

        ElementSpec rawAttrs = new ElementSpec(
            "raw-attrs",
            new AttrSpec[]
            {
                new AttrSpec("space", 
                    AttrSpec.enumeratedValue("yes no"), false)
            },
            nullChildSpecs,
            TextContent.required
            );

        ElementSpec extract = new ElementSpec(
            "extract",
            new AttrSpec[]
            {
                new AttrSpec("to-file",
                    AttrSpec.nonEmpty, false)
            },
            nullChildSpecs,
            TextContent.optional
            );
        
        ElementSpec discard = new ElementSpec(
            "discard",
            nullAttrSpecs,
            nullChildSpecs,
            TextContent.none
            );

        ElementSpec discardTag = new ElementSpec(
            "discard-tag",
            nullAttrSpecs,
            nullChildSpecs,
            TextContent.none
            );

        ElementSpec attr = new ElementSpec(
            "attr",
            new AttrSpec[]
            {
                new AttrSpec("name", AttrSpec.nmtoken, true)
            },
            new ChildSpec[]
            {
                new ChildSpec(iff, false),
                new ChildSpec(content, false),
                new ChildSpec(rawContent, false),
                new ChildSpec(discard, false),
                new ChildSpec(rename, false)
            },
            TextContent.none
            );

        ChildSpec[] modifierChildSpec = new ChildSpec[]
        {
            new ChildSpec(content, false),
            new ChildSpec(rawContent, false),
            new ChildSpec(attr, true),
            new ChildSpec(iff, false),
            new ChildSpec(forr, false),
            new ChildSpec(foreach, false),
            new ChildSpec(discard, false),
            new ChildSpec(discardTag, false),
            new ChildSpec(rename, false),
            new ChildSpec(before, false),
            new ChildSpec(beforeContent, false),
            new ChildSpec(after, false),
            new ChildSpec(afterContent, false),
            new ChildSpec(rawAttrs, false),
            new ChildSpec(extract, false)
        };
        
        ElementSpec exclusiveIterationTags = new ElementSpec(
            "exclusiveIterationTags", 
            nullAttrSpecs,
            nullChildSpecs,
            TextContent.optional
            );
        
        ElementSpec preformattedTags = new ElementSpec(
            "preformattedTags", 
            nullAttrSpecs,
            nullChildSpecs,
            TextContent.optional
            );
        
        ElementSpec flagAttributes = new ElementSpec(
            "flagAttributes", 
            nullAttrSpecs,
            nullChildSpecs,
            TextContent.optional
            );
        
        ElementSpec idAttribute = new ElementSpec(
            "idAttribute", 
            nullAttrSpecs,
            nullChildSpecs,
            TextContent.required
            );
        
        ElementSpec classAttribute = new ElementSpec(
            "classAttribute", 
            nullAttrSpecs,
            nullChildSpecs,
            TextContent.required
            );
        
        ElementSpec prolog = new ElementSpec(
            "prolog", 
            nullAttrSpecs,
            new ChildSpec[]
            {
                new ChildSpec(dynamatorCopy, true)
            },
            TextContent.required
            );
        
        ElementSpec epilog = new ElementSpec(
            "epilog", 
            nullAttrSpecs,
            new ChildSpec[]
            {
                new ChildSpec(dynamatorCopy, true)
            },
            TextContent.required
            );
        
        ElementSpec beforeExtracts = new ElementSpec(
            "before-extracts", 
            nullAttrSpecs,
            nullChildSpecs,
            TextContent.required
            );
        
        ElementSpec afterExtracts = new ElementSpec(
            "after-extracts", 
            nullAttrSpecs,
            nullChildSpecs,
            TextContent.required
            );
        
        ElementSpec id = new ElementSpec(
            "id", 
            new AttrSpec[]
            {
                new AttrSpec("name", AttrSpec.nmtoken, true)
            },
            modifierChildSpec,
            TextContent.none
            );
        
        ElementSpec klass = new ElementSpec(
            "class", 
            new AttrSpec[]
            {
                new AttrSpec("name", AttrSpec.nmtoken, true)
            },
            modifierChildSpec,
            TextContent.none
            );
        
        ElementSpec tag = new ElementSpec(
            "tag", 
            new AttrSpec[]
            {
                new AttrSpec("with-attr", AttrSpec.nmtokens, false),
                new AttrSpec("without-attr", AttrSpec.nmtokens, false),
            },
            modifierChildSpec,
            TextContent.none,
            true
            );
        
        ElementSpec include = new ElementSpec(
            "include",
            new AttrSpec[]
            {
                new AttrSpec("file", AttrSpec.nonEmpty, true),
            },
            nullChildSpecs,
            TextContent.none
            );

        ElementSpec result = new ElementSpec(
            "dynamator",
            new AttrSpec[]
            {
                new AttrSpec("language", AttrSpec.nmtoken, false),
                new AttrSpec("suffix", AttrSpec.nmtoken, false),
                new AttrSpec("template", AttrSpec.nonEmpty, false),
                new AttrSpec("filename", AttrSpec.nonEmpty, false),
                new AttrSpec("comment-start", AttrSpec.unspecified, false),
                new AttrSpec("comment-end", AttrSpec.unspecified, false)
            },
            new ChildSpec[]
            {
                new ChildSpec(exclusiveIterationTags, false),
                new ChildSpec(preformattedTags, false),
                new ChildSpec(flagAttributes, false),
                new ChildSpec(idAttribute, false),
                new ChildSpec(classAttribute, false),
                new ChildSpec(prolog, true),
                new ChildSpec(beforeExtracts, true),
                new ChildSpec(afterExtracts, true),
                new ChildSpec(epilog, true),
                new ChildSpec(id, true),
                new ChildSpec(klass, true),
                new ChildSpec(tag, true),
                new ChildSpec(include, true)
            },
            TextContent.none
            );

        return result;
    }

    public static
    void
    validate(
        Element root,
        ElementSpec documentSpec,
        ErrorHandler errors
        )
    {
        dynamatorSpec_.validate(root, errors);
    }

    public static
    void
    validate(
        Element root,
        ErrorHandler errors
        )
    {
        validate(root, dynamatorSpec_, errors);
    }
}
