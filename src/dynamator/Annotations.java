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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import org.xml.sax.Attributes;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
    Dynamator file access methods.
    (Dynamator files used to be called Annotations files.)
**/
public
class Annotations
{
//    private static final String rcsID_ = 
//        "$Id: Annotations.java,v 1.12 2004/03/23 07:36:49 jaydunning Exp $";

    private AnnotationsIndex content_;
    private String uri_;

    protected String language_;
    protected boolean contentIsRaw_ = false;

    public
    Annotations(
        File file,
        Options options
        )
    throws IOException
    {
        if ( ! file.exists() )
        {
            throw new FatalError(
                "Dynamator file not found: " 
                + file.getAbsolutePath());
        }

        uri_ = file.getAbsolutePath().replace('\\','/');
        File uriDir = new File(Utils.pathTo(uri_));

        if ( Trace.on )
        {
            Trace.clearPrefix();
            Trace.display("processing Dynamator file " + file.toString());
        }

        IncludeResolver includeResolver = (IncludeResolver)
            options.includeResolver.clone();
        includeResolver.addDirectory(uriDir);

        AnnotationsLoader loader = 
            new AnnotationsLoader(options, includeResolver);

        String filepath = file.getCanonicalPath();
        
        Document document = loader.load(file, filepath);

        if ( loader.foundErrors() )
        {
            throw new FatalError();
        }

        if ( options.bodyOnlyHtml )
        {
            applyBodyOnlyHtml(document);
        }

        content_ = AnnotationsIndex.factory.obtain(
            document, filepath, loader);
        
        if ( loader.foundErrors() )
        {
            throw new FatalError();
        }

        language_ = Utils.defaultIfEmpty(
            content_.getRootAttribute("language"), "none");

        if ( "none".equals(language_) )
        {
            contentIsRaw_ = true;
        }
    }
    
    /**
        If the -B option (Body-only HTML) was specified, 
        insert elements to remove the following HTML elements
        (which will have been inserted by JTidy even if they 
        weren't in the original HTML):
<pre>
* &lt;!DOCTYPE&gt;  - removed
* &lt;html&gt;      - removed
*   &lt;head&gt;    - removed
*   ...             - removed
*   &lt;/head&gt;   - removed
*   &lt;body&gt;    - removed
*     (body)        - retained
*   &lt;/body&gt;   - removed
* &lt;/html&gt;     - removed
</pre>
        <p>
        Patch 896127, Chris Felaco:
        This is now done directly through the DOM API instead of
        indirectly by parsing a String.
    **/
    private
    void
    applyBodyOnlyHtml(
        Document document
        )
    {
        Element[] elements = new Element[]
        {
            bodyOnlyHtmlElement(document, "html", "discard-tag"),
            bodyOnlyHtmlElement(document, "head", "discard"),
            bodyOnlyHtmlElement(document, "body", "discard-tag"),
        };

        Node root = document.getDocumentElement();
        Node firstChild = root.getFirstChild();
        if ( firstChild == null )
        {
            int i = -1;
            while ( ++i < elements.length )
            {
                root.appendChild(elements[i]);
            }
        }
        else
        {
            int i = -1;
            while ( ++i < elements.length )
            {
                root.insertBefore(elements[i], firstChild);
            }
        }
   }
    
    /**
        Convenience method used by applyBodyOnlyHtml.
    **/
    private
    Element
    bodyOnlyHtmlElement(
        Document document,
        String elementName,
        String discardName
        )
    {
        Element result = document.createElement("tag");
        result.setAttribute("tag", elementName);
        result.appendChild(document.createElement(discardName));        

        return result;
    }

    /**
        The URI of the Annotations file.
    **/
    public
    String
    name()
    {
        return uri_;
    }
    
    /**
        The language of the output file, from the root element.
        If the root element does not specify a language, the
        value is "none".
    **/
    public
    String
    getLanguage()
    {
        return language_;
    }
    
    /**
        The suffix specified for the output file, from the root element.

        @return 
            null if no suffix attribute was specified or if the
            attribute value is an empty string.
    **/
    public
    String
    getSuffix()
    {
        return Utils.nullIfEmpty(content_.getRootAttribute("suffix"));
    }
   
    /**
        The template file specified in the root element.

        @return 
            null if no template attribute was specified or if the
            attribute value is an empty string.
    **/
    public
    String
    getTemplate()
    {
        return Utils.nullIfEmpty(content_.getRootAttribute("template"));
    }
   
    /**
        The output file name specified in the root element.

        @return 
            null if no filename attribute was specified or if the
            attribute value is an empty string.
    **/
    public
    String
    getFilename()
    {
        return Utils.nullIfEmpty(content_.getRootAttribute("filename"));
    }
   
    /**
        The character sequence to be used to start the file generation
        comment in the output file, from the root element.

        @return 
            the value of the comment-start attribute, 
            or null if the attribute is not present.
    **/
    public
    String
    getCommentStart()
    {
        Element root = content_.getRootElement();
        
        if ( root.hasAttribute("comment-start") )
        {
            return root.getAttribute("comment-start");
        }

        return null;
    }
   
    /**
        The character sequence to be used to end the file generation
        comment in the output file, from the root element.

        @return 
            the value of the comment-end attribute,
            or null if the attribute is not present.
    **/
    public
    String
    getCommentEnd()
    {
        Element root = content_.getRootElement();
        
        if ( root.hasAttribute("comment-end") )
        {
            return root.getAttribute("comment-end");
        }

        return null;
    }

    /**
        Obtain the content of each prolog element, in the order
        encountered. 
    **/
    public
    String[]
    getPrologs()
    {
        return content_.getPrologs();
    }

    /**
        Obtain the content of each epilog element, in the order
        encountered. 
    **/
    public
    String[]
    getEpilogs()
    {
        return content_.getEpilogs();
    }
    
    /**
        Obtain all class elements for a specified class name.
    **/
    public
    Element[]
    getClassElements(
        String name
        )
    {
        return content_.getClassElements(name);
    }
    
    /**
        Obtain all id elements for a specified id name.
    **/
    public
    Element[]
    getIdElements(
        String name
        )
    {
        return content_.getIdElements(name);
    }
    
    /**
        Obtain the content of a configuration element.  
        A configuration element is a top-level element that is not 
        an id, include, class, or tag element, and that is expected
        to appear only once in a document.

        @param name
            The element name.

        @return 
            The content of the element, or null if no 
            element with that name is present.
    **/
    public
    String
    getConfigurable(
        String name
        )
    {
        return content_.getConfigurable(name);
    }

    /**
        Obtain the set of attribute overrides for a set of parent
        annotations elements. 

        @param parents
            Elements to be searched for attr children.
        @return 
            The set of attribute overrides; empty if none found.
    **/
    public
    AttributeOverride[]
    getAttributeOverrides(
        Element[] parents
        )
    {
        Vector vResult = new Vector();

        Element parent;
        int iParents = -1;
        int limParents = parents.length;
        while ( ++iParents < limParents )
        {
            parent = parents[iParents];
            
            NodeList attrs = 
                ElementUtils.getChildrenByTagName(parent, "attr");
            if ( attrs != null )
            {
                Element element;
                AttributeOverride override;
                int nAttrs = attrs.getLength();
                String tagName = parent.getTagName();
                String key = 
                    tagName.equals("tag") 
                    ? parent.getAttribute("tag")
                    : parent.getAttribute("name");

                int i = -1;
                while ( ++i < nAttrs )
                {
                    element = (Element) (attrs.item(i));
                    override = newAttributeOverride(
                        element, tagName, key);
                    vResult.addElement(override);
                }
            }
        }

        AttributeOverride[] result = new AttributeOverride[vResult.size()];
        vResult.copyInto(result);
        return result;
    }

    /**
        Create an attribute override.
        
        @param element
            The &lt;attr&gt; element.
        @param keyType
            The tagname of the element containing the &lt;attr&gt;
            element (i.e. 'class' or 'id' or 'tag').
        @param key
            The value of the 'name' attribute of the element containing
            the &lt;attr&gt; element.
    **/
    protected
    AttributeOverride
    newAttributeOverride(
        Element element,
        String keyType,
        String key
        )
    {
        String name = element.getAttribute("name");

        String newName = null;
        Element renameElement = 
            ElementUtils.getSingleElement(element, "rename");
        if ( renameElement != null )
        {
            newName = renameElement.getAttribute("to");
            if ( newName != null && newName.length() == 0 )
            {
                newName = null;
            }
        }
        
        String value = 
            ElementUtils.getSingleContent(element, "content", false);
        boolean valueIsRaw  = contentIsRaw_;
        if ( value == null )
        {
            value = ElementUtils.getSingleContent(element, "raw-content");
            if ( value != null )
            {
                valueIsRaw = true;
            }
        }
        if ( value != null && value.length() == 0 )
        {
            value = null;
        }
        String ifExpression = 
            ElementUtils.getSingleContent(element, "if");
        String flag         = element.getAttribute("flag");
        boolean isFlag      = "yes".equals(flag);
        boolean discard     = 
            (ElementUtils.getSingleElement(element, "discard") != null);

        return new AttributeOverride(
            keyType, key, name, newName, value, valueIsRaw, 
            isFlag, discard, ifExpression);
    }

    /**
        Determine whether a template element satisfies attribute
        matching criteria as specified in a Dynamator &lt;tag&gt;
        element. 

        @param element
            The annotations element to be matched.

        @param attributes
            Attributes of the template element.
    **/
    private static
    boolean
    matchAttributes(
        Element element,
        Attributes attributes
        )
    {
        NamedNodeMap tagAttrs = element.getAttributes();
        Node tagAttr;
        String name;
        String templateValue;
        int iTagAttr = -1;
        int limTagAttrs = tagAttrs.getLength();
        while ( ++iTagAttr < limTagAttrs )
        {
            tagAttr = tagAttrs.item(iTagAttr);
            name = tagAttr.getNodeName();
            if ( "tag".equals(name) )
            {
                continue;
            }
            if ( "_dyn_loc_".equals(name) )
            {
                continue;
            }

            if ( "with-attr".equals(name) )
            {
                if ( ! ElementUtils.hasAllAttributes(
                        tagAttr.getNodeValue(), attributes) )
                {
                    break;
                }
            }
            else
            if ( "without-attr".equals(name) )
            {
                if ( ElementUtils.hasAnyAttribute(
                        tagAttr.getNodeValue(), attributes) )
                {
                    break;
                }
            }
            else
            {
                templateValue = attributes.getValue(name);
                if ( templateValue == null )
                {
                    // if the current template tag doesn't have this
                    // attribute, then it's not the right tag
                    break;
                }
                if ( ! templateValue.equals(tagAttr.getNodeValue()) )
                {
                    // if the current template tag doesn't have the
                    // same value, then it's not the right tag
                    break;
                }
            }
        }

        return iTagAttr == limTagAttrs;
    }        

    /**
        Obtain &lt;tag&gt; elements for a specific tagname and
        attribute value set. 
        
        <pre>
            &lt;tag <i>tag</i>="<i>tagName</i>" ... &gt;
        </pre>
        @param tagName
            The name of the template element for which tag elements
            are to be obtained.
        @param attributes
            The attributes of the template element for which tag
            elements are to be obtained.
        @return
            All elements where &lt;tag tag=<i>tagName</i>
            AND where all attributes of that element in the Dynamator
            file (except for 'tag')
            match the corresponding argument attributes (but not
            necessarily the converse), AND where any attributes
            specified by the 'with-attr' attribute of the tag
            element are present.  The order of elements in the
            result array is consistent with their order in the
            Dynamator file.
    **/
    public
    Element[]
    getMatchingTagElements(
        String tagname,
        Attributes attributes
        )
    {
        Vector vResult = new Vector();
        Element element;
        Element[] tags = content_.getTagElements(tagname);
        int iTag = -1;
        int limTag = tags.length;
        while ( ++iTag < limTag )
        {
            element = tags[iTag];
            if ( matchAttributes(element, attributes) )
            {
                vResult.addElement(element);
            }
        }

        Element[] result = new Element[vResult.size()];
        vResult.copyInto(result);
        
        return result;
    }

    /**
        Output the Annotations object for debugging.
    **/
    public
    void
    dump(
        PrintWriter writer
        )
    {
        content_.dump(writer);
    }
    
    public static 
    void 
    main(
        String[] args
        )
    throws IOException
    {
        PrintWriter writer = new PrintWriter(System.out, true);
        
        Options options = new Options();
        ArgumentParser argumentParser = new ArgumentParser(args);
        String arg;
        
        while ( argumentParser.hasMoreArguments() )
        {
            arg = argumentParser.nextArgument();
            if ( arg.startsWith("-") )
            {
                if ( ! options.handleArgument(arg, argumentParser) )
                {
                    return;
                }
            }
            else
            {
                Annotations annotations = 
                    new Annotations(new File(arg), options);
                annotations.dump(writer);
            }
        }

        writer.flush();
        writer.close();
    }
}

