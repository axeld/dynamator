/***********************************************************************
*   Copyright 2000-2003 by Jay Dunning.
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

package dynamator.jsp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dynamator.Utils;

public
class Generator
extends dynamator.Generator
{
//    private static final String rcsID_ = 
//        "$Id: Generator.java,v 1.8 2004/02/24 07:47:04 jaydunning Exp $";

    // Perhaps this should be a proper class hierarchy, but 
    // I don't know if that would make it any easier to understand.
    private static final
    class CollectionType
    {
        public static final CollectionType ARRAY = new CollectionType();
        public static final CollectionType DICTIONARY = new CollectionType();
        public static final CollectionType PROPERTIES = new CollectionType();
        public static final CollectionType ENUMERATION = new CollectionType();
        public static final CollectionType ITERATOR = new CollectionType();
        public static final CollectionType ITERABLE = new CollectionType();
        public static final CollectionType MAP = new CollectionType();
    }

    private static class Collection
    {
        String name;
        String declaredType;
        CollectionType type;
        String elementType;
        String keySuffix;
        String valueSuffix;
        int paramterType;

        public Collection(
            String name,
            String declaredType,
            CollectionType type,
            String elementType,
            String keySuffix,
            String valueSuffix,
            int parameterType
            )
        {
            this.name = name;
            this.declaredType = declaredType;
            this.type = type;
            this.elementType = elementType;
            this.keySuffix = keySuffix;
            this.valueSuffix = valueSuffix;
            this.paramterType = parameterType;
        }
    }

    private static final int NONE = 0;
    private static final int ELEMENT = 1;
    private static final int KEY_ELEMENT = 2;
    private static final int KEY_VALUE = 3;

    private static final List<Collection> collections_ = new ArrayList<>();

    static
    {
        collections_.add(new Collection("Dictionary", "java.util.Dictionary", CollectionType.DICTIONARY, null, "Key", null, KEY_ELEMENT));
        collections_.add(new Collection("Map", "java.util.Map", CollectionType.MAP, "java.util.Map.Entry", "Key", "Value", KEY_VALUE));
        collections_.add(new Collection("Properties", "java.util.Properties", CollectionType.PROPERTIES, "String", "Name", null, NONE));
        collections_.add(new Collection("Iterator", "java.util.Iterator", CollectionType.ITERATOR, null, null, null, ELEMENT));
        collections_.add(new Collection("Iterable", "java.util.Iterator", CollectionType.ITERABLE, null, null, null, ELEMENT));
        collections_.add(new Collection("Set", "java.util.Iterator", CollectionType.ITERABLE, null, null, null, ELEMENT));
        collections_.add(new Collection("List", "java.util.Iterator", CollectionType.ITERABLE, null, null, null, ELEMENT));
        collections_.add(new Collection("Vector", "java.util.Iterator", CollectionType.ITERABLE, null, null, null, ELEMENT));
        collections_.add(new Collection("Enumeration", "java.util.Enumeration", CollectionType.ENUMERATION, null, null, null, ELEMENT));
    }

    private
    String
    convertToVariable(
        String reference
        )
    {
        StringBuffer buf = new StringBuffer(reference.length());
        buf.append("$");
        boolean capitalizeNextCharacter = false;
        char c;
        int lim = reference.length();
        int i = -1;
        while ( ++i < lim )
        {
            c = reference.charAt(i);
            if ( Character.isJavaIdentifierPart(c) )
            {
                if ( c == 'g'  
                    && i > 2
                    && i + 3 < lim
                    && reference.charAt(i-1) == '.'
                    && reference.charAt(i+1) == 'e'
                    && reference.charAt(i+2) == 't'
                    && Character.isUpperCase(reference.charAt(i+3)) 
                   )
                {
                    i += 2;
                }
                else
                {
                    if ( buf.length() == 1 )
                    {
                        buf.append(Character.toLowerCase(c));
                        capitalizeNextCharacter = false;
                    }
                    else
                    if ( capitalizeNextCharacter )
                    {
                        buf.append(Character.toUpperCase(c));
                        capitalizeNextCharacter = false;
                    }
                    else
                    {
                        buf.append(c);
                    }
                }
            }
            else
            {
                capitalizeNextCharacter = true;
            }
        }

        return buf.toString();
    }

    public
    void
    outputDynamicValueExpression(
        String value
        )
    {
        outputRaw(
            ( value.startsWith("<") )
            ? value
            : dynamicExpression(value));
    }
    
    private
    String
    dynamicExpression(
        String value
        )
    {
        return "<%= " + value + " %>";
    }

    private String scriptletIndent_;

    private
    void
    startScriptlet(
        boolean newLine
        )
    {
// 20020809: browser whitespace bug
//        if ( newLine )
//        {
//            startProgramLine();
//        }
        inTemplateLine(false);
        scriptletIndent_ = indentation_.current();
        outputRaw("<%");
        indentation_.increase();
        endProgramLine();
        startProgramLine();
    }
    
    private
    void
    endScriptlet(
        boolean newLine
        )
    {
        indentation_.decrease();
        if ( formatting() )
        {
            outputRaw("\n");
            outputRaw(scriptletIndent_);
        }
        outputRaw("%>");
        inTemplateLine(true);
    }
    
    public
    void
    start(
        boolean produceGenerationComment
        )
    {
        if ( produceGenerationComment )
        {
            outputRaw("<%-- generated by Dynamator " + new Date() + "\n--%>");
        }
    }
    
    public
    Object          // if-expression
    startIfBlock(
        String expression,
        boolean newLine
        )
    {
        expression = expression.trim();     // just in case

        startScriptlet(newLine);

        String ifExpression = "if ( " + expression + " ) ";

        outputRaw(ifExpression);
        nextProgramLine();
        outputRaw("{");
        indentation_.increase();
        if ( ! newLine )
        {
            scriptletIndent_ += indentation_.string();
            scriptletIndent_ += indentation_.string();
        }
        endScriptlet(newLine);
        indentation_.increase();

        return ifExpression;
    }
    
    public
    void
    endIfBlock(
        Object ifExpression,
        boolean newLine
        )
    {
        indentation_.decrease();
        indentation_.decrease();
        startScriptlet(newLine);
        outputRaw("}");
//        outputRaw("} /* " + ifExpression.toString() + " */");
        endScriptlet(newLine);
    }
    
    public
    Object
    startCollectionIterationBlock(
        String collectionExpression,
        String collectionTypeString, 
        String elementName,
        String iName,
        String collectionName
        )
    {
        collectionExpression = collectionExpression.trim();

        String result = 
            collectionName != null
            ? collectionName
            : collectionExpression;
            
        String elementType = null;

        String keyType = null;          // only used for Dictionaries and Maps
        String keySuffix = null;

        String valueType = null;        // only used for Maps
        String valueSuffix = null;

        String collectionDeclaredType = null;
        CollectionType collectionType = null;

        if ( collectionTypeString != null )
        {
            if ( collectionTypeString.endsWith("[]") )
            {
                elementType = collectionTypeString.substring(
                    0, collectionTypeString.length()-2);
                collectionType = CollectionType.ARRAY;
                collectionDeclaredType = collectionTypeString;
            }
            else
            {
                for (Collection collection : collections_)
                {
                    if ( collection.paramterType == NONE
                            && !collectionTypeString.equals(collection.name)
                            || collection.paramterType != NONE
                            && !collectionTypeString.startsWith(collection.name) )
                        continue;

                    elementType = collection.elementType;
                    collectionDeclaredType = collection.declaredType;
                    collectionType = collection.type;
                    keySuffix = collection.keySuffix;
                    valueSuffix = collection.valueSuffix;

                    if ( collection.paramterType != NONE )
                    {
                        int last = collectionTypeString.length() - 1;
                        if ( last < collection.name.length() + 2
                                || collectionTypeString.charAt(collection.name.length()) != '['
                                || collectionTypeString.charAt(last) != ']' )
                        {
                            error("Invalid foreach type: " + collectionTypeString);
                            return result;
                        }

                        String type = collectionTypeString.substring(collection.name.length() + 1,
                                last).trim();
                        if ( collection.paramterType == ELEMENT )
                        {
                            elementType = type;
                        }
                        else
                        {
                            String[] types = type.split(",");
                            if ( types.length != 2 )
                            {
                                error(
                                        "Invalid foreach type: " + collectionTypeString
                                        + " (should be " + collection.name + "[keytype,valuetype])"
                                        );
                                return result;
                            }

                            keyType = types[0].trim();
                            if ( collection.paramterType == KEY_ELEMENT)
                                elementType = types[1].trim();
                            else
                                valueType = types[1].trim();
                        }
                    }
                }

                if ( collectionType == null )
                {
                    error("Invalid foreach type: " + collectionTypeString);
                    return result;
                }
            }
        }
        else
        {
            error(
                "Must declare 'type' attribute for 'foreach'"
                );
            return result;
        }

        String refVarName = convertToVariable(collectionExpression);

        String iVarName =
            iName != null
            ? iName
            : collectionName != null
              ? "i" + Utils.capitalizeFirstCharacter(collectionName)
              : "i" + Utils.capitalizeFirstCharacter(refVarName);

        String limVarName = 
            "lim" 
            + ( collectionName != null 
                ? Utils.capitalizeFirstCharacter(collectionName)
                : Utils.capitalizeFirstCharacter(refVarName) );

        if ( collectionName == null )
        {
            collectionName = refVarName;
        }
        
        String lengthExpression = collectionName;
        
        if ( elementName == null 
            && 
            ( collectionType == CollectionType.ENUMERATION
                || collectionType == CollectionType.DICTIONARY
                || collectionType == CollectionType.PROPERTIES
                || collectionType == CollectionType.ITERATOR
                || collectionType == CollectionType.MAP
                || collectionType == CollectionType.ITERABLE
            )
           )
        {
            elementName = collectionName + "Element";
        }
        
        /*
            output a jsp scriptlet to begin an iteration block.
            The following variables are declared within the block:
            - limXxx: the total number of elements in the indexed property
            - iXxx:   the offset of the element currently being
                      output at runtime 
        */

        startScriptlet(true);
        outputRaw("{");
        indentation_.increase();

        nextProgramLine();
        outputRaw(
            collectionDeclaredType + " " 
            + collectionName
            + " = " + collectionExpression + (collectionType == CollectionType.ITERABLE ? ".iterator()" : "") + ";"
            );

        if ( collectionType == CollectionType.ARRAY )
        {
            nextProgramLine();
            outputRaw(
                "int " + limVarName
                + " = " + lengthExpression 
                + ".length;"
                );
        }

        if ( elementName != null )
        {
            nextProgramLine();
            outputRaw(
                elementType + " " + elementName + ";"
                );
        }

        if ( keyType != null )
        {
            nextProgramLine();
            outputRaw(
                keyType + " " + elementName + keySuffix + ";"
                );
        }

        if ( valueType != null )
        {
            nextProgramLine();
            outputRaw(
                valueType + " " + elementName + valueSuffix + ";"
                );
        }

        if ( collectionType == CollectionType.DICTIONARY )
        {
            nextProgramLine();
            outputRaw(
                "java.util.Enumeration " 
                + collectionName + "Keys = " 
                + collectionName + ".keys();"
                );
        }
        else
        if ( collectionType == CollectionType.MAP )
        {
            nextProgramLine();
            outputRaw(
                "java.util.Iterator " 
                + collectionName + "Entries = " 
                + collectionName + ".entrySet().iterator();"
                );
        }
        else
        if ( collectionType == CollectionType.PROPERTIES )
        {
            nextProgramLine();
            outputRaw(
                "java.util.Enumeration " 
                + collectionName + "Names = " 
                + collectionName + ".propertyNames();"
                );
        }

        nextProgramLine();
        if ( collectionType == CollectionType.ENUMERATION )
        {
            if ( iName != null )
            {
                outputRaw("int " + iName + " = -1;");
                nextProgramLine();
            }
            outputRaw(
                "while ( "
                + collectionName + ".hasMoreElements()"
                + " )"
                );
        }
        else
        if ( collectionType == CollectionType.ITERATOR || collectionType == CollectionType.ITERABLE )
        {
            if ( iName != null )
            {
                outputRaw("int " + iName + " = -1;");
                nextProgramLine();
            }
            outputRaw(
                "while ( "
                + collectionName + ".hasNext()"
                + " )"
                );
        }
        else
        if ( collectionType == CollectionType.PROPERTIES )
        {
            if ( iName != null )
            {
                outputRaw("int " + iName + " = -1;");
                nextProgramLine();
            }
            outputRaw(
                "while ( "
                + collectionName + "Names.hasMoreElements()"
                + " )"
                );
        }
        else
        if ( collectionType == CollectionType.DICTIONARY )
        {
            if ( iName != null )
            {
                outputRaw("int " + iName + " = -1;");
                nextProgramLine();
            }
            outputRaw(
                "while ( "
                + collectionName + "Keys.hasMoreElements()"
                + " )"
                );
        }
        else
        if ( collectionType == CollectionType.MAP )
        {
            if ( iName != null )
            {
                outputRaw("int " + iName + " = -1;");
                nextProgramLine();
            }
            outputRaw(
                "while ( "
                + collectionName + "Entries.hasNext()"
                + " )"
                );
        }
        else
        {
            outputRaw(
                "for ( int " + iVarName + " = 0; "
                + iVarName + " < " + limVarName + "; "
                + "++" + iVarName
                + " )"
                );
        }
        
        nextProgramLine();
        outputRaw("{");
        indentation_.increase();
        if ( elementName != null )
        {
            if ( collectionType == CollectionType.DICTIONARY )
            {
                nextProgramLine();
                outputRaw(
                    elementName + "Key = (" + keyType + ") "
                    + collectionName + "Keys.nextElement();"
                    );
                nextProgramLine();
                outputRaw(
                    elementName + " = (" + elementType + ") "
                    + collectionName + ".get(" + elementName + "Key);"
                    );
                if ( iName != null )
                {
                    nextProgramLine();
                    outputRaw("++" + iName + ";");
                }
            }
            else
            if ( collectionType == CollectionType.MAP )
            {
                nextProgramLine();
                outputRaw(
                    elementName + " = (java.util.Map.Entry) "
                    + collectionName + "Entries.next();"
                    );
                nextProgramLine();
                outputRaw(
                    elementName + "Key = (" + keyType + ") "
                    + elementName + ".getKey();"
                    );
                nextProgramLine();
                outputRaw(
                    elementName + "Value = (" + valueType + ") "
                    + elementName + ".getValue();"
                    );
                if ( iName != null )
                {
                    nextProgramLine();
                    outputRaw("++" + iName + ";");
                }
            }
            else
            if ( collectionType == CollectionType.PROPERTIES )
            {
                nextProgramLine();
                outputRaw(
                    elementName + "Name = (" + keyType + ") "
                    + collectionName + "Names.nextElement();"
                    );
                nextProgramLine();
                outputRaw(
                    elementName + " = "
                    + collectionName + ".getProperty(" + elementName + "Name);"
                    );
                if ( iName != null )
                {
                    nextProgramLine();
                    outputRaw("++" + iName + ";");
                }
            }
            else
            if ( collectionType == CollectionType.ENUMERATION )
            {
                nextProgramLine();
                outputRaw(
                    elementName + " = (" + elementType + ") "
                    + collectionName + ".nextElement();"
                    );
                if ( iName != null )
                {
                    nextProgramLine();
                    outputRaw("++" + iName + ";");
                }
            }
            else
            if ( collectionType == CollectionType.ITERATOR || collectionType == CollectionType.ITERABLE )
            {
                nextProgramLine();
                outputRaw(
                    elementName + " = (" + elementType + ") "
                    + collectionName + ".next();"
                    );
                if ( iName != null )
                {
                    nextProgramLine();
                    outputRaw("++" + iName + ";");
                }
            }
            else
            {
                nextProgramLine();
                outputRaw(
                    elementName + " = " + collectionName + "[" + iVarName + "];"
                    );
            }
        }

        endScriptlet(true);
        indentation_.increase();

        return result;
    }
    
    public
    void
    endCollectionIterationBlock(
        Object foreachExpression
        )
    {
        indentation_.decrease();
        indentation_.decrease();
        indentation_.decrease();
        startScriptlet(true);
        outputRaw(indentation_.string());
        outputRaw("}");
//        outputRaw("}  /* foreach " + foreachExpression.toString() + " */");
        nextProgramLine();
        outputRaw("}");
        endScriptlet(true);
    }

    public
    Object
    startSequencedIterationBlock(
        String i,       // ignored
        String first,
        String last, 
        String step
        )
    {
        Object result = null;

        /*
            output a jsp scriptlet to begin an iteration block.
        */

        startScriptlet(true);
        
        outputRaw(
            "for ( " + first + "; " + last + "; " + step + " )"
            );
        
        nextProgramLine();
        outputRaw("{");
        indentation_.increase();

        endScriptlet(true);
        indentation_.increase();

        return result;
    }
    
    public
    void
    endSequencedIterationBlock(
        Object nothing
        )
    {
        indentation_.decrease();
        indentation_.decrease();
        startScriptlet(true);
        outputRaw("}  // for");
        endScriptlet(true);
    }

    public
    Object
    startForBlock(
        String expression
        )
    {
        /*
            output a jsp scriptlet to begin a for block.
        */

        startScriptlet(true);
        
        outputRaw("for ");
        if ( ! expression.startsWith("(") )
        {
            outputRaw("( ");
        }
        outputRaw(expression);
        if ( ! expression.startsWith(")") )
        {
            outputRaw(" )");
        }
        
        nextProgramLine();
        outputRaw("{");
        indentation_.increase();

        endScriptlet(true);
        indentation_.increase();

        return null;
    }
    
    public
    void
    endForBlock(
        Object nothing
        )
    {
        indentation_.decrease();
        indentation_.decrease();
        startScriptlet(true);
        outputRaw("}  // for");
        endScriptlet(true);
    }
}
