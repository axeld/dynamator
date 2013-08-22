// print(String) needs to check for newlines and print templates accordingly.

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

package dynamator.java;

import java.util.Date;

import dynamator.Utils;

public
class Generator
extends dynamator.Generator
{
//    private static final String rcsID_ = 
//        "$Id: Generator.java,v 1.8 2004/02/24 07:47:04 jaydunning Exp $";

    // Perhaps this should be a proper class hierarchy, but 
    // I don't think that would make it any easier to understand.
    private static final
    class CollectionType
    {
        public static final CollectionType ARRAY = new CollectionType();
        public static final CollectionType VECTOR = new CollectionType();
        public static final CollectionType DICTIONARY = new CollectionType();
        public static final CollectionType PROPERTIES = new CollectionType();
        public static final CollectionType ENUMERATION = new CollectionType();
        public static final CollectionType ITERATOR = new CollectionType();
        public static final CollectionType MAP = new CollectionType();
    }
    
//======================================================================
    private String programIndent_ = "        ";  //! arbitrary for now

    private boolean expectsTemplateIndent_ = false;
    
    public
    void
    startTemplateLine()
    {
        outputRaw(programIndent_);
        outputRaw("out.write(\"");

        if ( formatting() && expectsTemplateIndent_ )
        {
            outputRaw(indentation_.current());
        }
        inTemplateLine(true);
    }
    
    /**
        Assumes current line is a template line
    */
    public
    void
    endTemplateLine()
    {
        endTemplateLine(true);
    }
    
    public
    void
    endTemplateLine(
        boolean newline
        )
    {
        if ( newline )
        {
            outputRaw("\\n");
        }
        
        outputRaw("\");");

        super.endTemplateLine(true);
        expectsTemplateIndent_ = newline;
    }
    
    /**
        Print the beginning of a new program line.
    **/
    public
    void
    startProgramLine()
    {
        if ( inTemplateLine() )
        {
            endTemplateLine();
        }
        else
        if ( inProgramLine() )
        {
            endProgramLine();
        }

        outputRaw(programIndent_);
        inProgramLine(true);
    }

    public
    void
    outputTemplate(
        char c
        )
    {
        if ( c == '"' )
        {
            super.outputTemplate('\\');
            super.outputRaw('"');
        }
        else
        {
            super.outputTemplate(c);
        }
    }
    
    public
    void
    outputTemplate(
        String value
        )
    {
        outputTemplate(value.toCharArray(), 0, value.length());
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
                // skip 'get' if present
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
        // Don't use String concatenation, which is wasteful.
        // Instead, output each expression independently.
        
        // can't use endTemplateLine because it increments the
        // output line number
        if ( inTemplateLine() )
        {
            outputRaw("\");\n");
            super.endTemplateLine(false);
        }

        increaseProgramIndentation();
        startProgramLine();
        outputRaw("out.write(");

        outputRaw("String.valueOf(");
        outputProgram(value.trim());
        outputRaw(")");

        outputRaw(");");
        endProgramLine();
        startProgramLine();
        outputRaw("out.write(\"");
        inProgramLine(false);
        inTemplateLine(true);
        decreaseProgramIndentation();

//        outputRaw("\" + ( ");
//        increaseProgramIndentation();
//        inProgramLine(true);
//        outputProgram(value.trim());
//        outputRaw(" ) + \"");
//        decreaseProgramIndentation();
//        inTemplateLine(true);
    }
    
    public
    void
    increaseProgramIndentation()
    {
        programIndent_ += "    ";
    }

    public
    void
    decreaseProgramIndentation()
    {
        programIndent_ = programIndent_.substring(4);
    }
    
    public
    void
    start(
        boolean produceGenerationComment
        )
    {
        if ( produceGenerationComment )
        {
            outputRaw("// generated by Dynamator ");
            outputRaw(new Date().toString());
            outputRaw('\n');
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
        if ( inTemplateLine() )
        {
            endTemplateLine(newLine);
        }

        String ifExpression = "if ( " + expression + " ) ";

        startProgramLine();
        outputRaw(ifExpression);
        nextProgramLine();
        outputRaw("{");
        endProgramLine();
        increaseProgramIndentation();

        return ifExpression;
    }
    
    public
    void
    endIfBlock(
        Object ifExpression,
        boolean newLine
        )
    {
        decreaseProgramIndentation();
        conditionallyEndTemplateLine(newLine);
        startProgramLine();
        outputRaw("}");
//        outputRaw("} /* " + ifExpression.toString() + " */");
        endProgramLine();
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

        String keyType = null;          // only used for Dictionaries
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
            if ( collectionTypeString.startsWith("Vector") )
            {
                int last = collectionTypeString.length()-1;
                if ( last < 8
                    || collectionTypeString.charAt(6) != '['
                    || collectionTypeString.charAt(last) != ']' )
                {
                    error(
                        "Invalid foreach type: " + collectionTypeString
                        );
                    return result;
                }

                elementType = collectionTypeString.substring(7, last);
                collectionDeclaredType = "java.util.Vector";
                collectionType = CollectionType.VECTOR;
            }
            else
            if ( collectionTypeString.startsWith("Enumeration") )
            {
                int last = collectionTypeString.length()-1;
                if ( last < 13
                    || collectionTypeString.charAt(11) != '['
                    || collectionTypeString.charAt(last) != ']' )
                {
                    error(
                        "Invalid foreach type: " + collectionTypeString
                        );
                    return result;
                }

                elementType = collectionTypeString.substring(12, last);
                collectionDeclaredType = "java.util.Enumeration";
                collectionType = CollectionType.ENUMERATION;
            }
            else
            if ( collectionTypeString.startsWith("Iterator") )
            {
                int last = collectionTypeString.length()-1;
                if ( last < 10
                    || collectionTypeString.charAt(8) != '['
                    || collectionTypeString.charAt(last) != ']' )
                {
                    error(
                        "Invalid foreach type: " + collectionTypeString
                        );
                    return result;
                }

                elementType = collectionTypeString.substring(9, last);
                collectionDeclaredType = "java.util.Iterator";
                collectionType = CollectionType.ITERATOR;
            }
            else
            if ( collectionTypeString.startsWith("Dictionary") )
            {
                int last = collectionTypeString.length()-1;
                if ( last < 14
                    || collectionTypeString.charAt(10) != '['
                    || collectionTypeString.charAt(last) != ']' )
                {
                    error(
                        "Invalid foreach type: " + collectionTypeString
                        );
                    return result;
                }

                String types = collectionTypeString.substring(11, last);
                int pComma = types.indexOf(',');
                if ( pComma == -1 )
                {
                    error(
                        "Invalid foreach type: " + collectionTypeString
                        + " (should be Dictionary[keytype,valuetype])"
                        );
                    return result;
                }
                keyType = types.substring(0, pComma).trim();
                elementType = types.substring(pComma+1).trim();
                collectionDeclaredType = "java.util.Dictionary";
                collectionType = CollectionType.DICTIONARY;
                keySuffix = "Key";
            }
            else
            if ( collectionTypeString.startsWith("Map") )
            {
                int last = collectionTypeString.length()-1;
                if ( last < 7
                    || collectionTypeString.charAt(3) != '['
                    || collectionTypeString.charAt(last) != ']' )
                {
                    error(
                        "Invalid foreach type: " + collectionTypeString
                        );
                    return result;
                }

                String types = collectionTypeString.substring(4, last);
                int pComma = types.indexOf(',');
                if ( pComma == -1 )
                {
                    error(
                        "Invalid foreach type: " + collectionTypeString
                        + " (should be Map[keytype,valuetype])"
                        );
                    return result;
                }
                elementType = "java.util.Map.Entry";
                keyType = types.substring(0, pComma).trim();
                valueType = types.substring(pComma+1).trim();
                collectionDeclaredType = "java.util.Map";
                collectionType = CollectionType.MAP;
                keySuffix = "Key";
                valueSuffix = "Value";
            }
            else
            if ( collectionTypeString.equals("Properties") )
            {
                keyType = "String";
                elementType = "String";
                keySuffix = "Name";
                collectionDeclaredType = "java.util.Properties";
                collectionType = CollectionType.PROPERTIES;
            }
            else
            {
                error(
                    "Invalid foreach type: " + collectionTypeString
                    );
                return result;
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
            ( collectionType == CollectionType.VECTOR
                || collectionType == CollectionType.ENUMERATION
                || collectionType == CollectionType.DICTIONARY
                || collectionType == CollectionType.PROPERTIES
                || collectionType == CollectionType.ITERATOR
                || collectionType == CollectionType.MAP
            )
           )
        {
            elementName = collectionName + "Element";
        }
        
        /*
            output java code to begin an iteration block.
            The following variables are declared within the block:
            - limXxx: the total number of elements in the indexed property
            - iXxx:   the offset of the element currently being
                      output at runtime 
        */

        conditionallyEndTemplateLine();
        startProgramLine();
        outputRaw("{");
        increaseProgramIndentation();

        nextProgramLine();
        outputRaw(
            "final " + collectionDeclaredType + " " 
            + collectionName
            + " = " + collectionExpression + ";"
            );

        if ( collectionType == CollectionType.ARRAY 
            || collectionType == CollectionType.VECTOR )
        {
            nextProgramLine();
            outputRaw(
                "final int " + limVarName
                + " = " + lengthExpression 
                + ( collectionType == CollectionType.VECTOR
                    ? ".size();"
                    : ".length;" )
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
                "final java.util.Enumeration " 
                + collectionName + "Keys = " 
                + collectionName + ".keys();"
                );
        }
        else
        if ( collectionType == CollectionType.MAP )
        {
            nextProgramLine();
            outputRaw(
                "final java.util.Iterator " 
                + collectionName + "Entries = " 
                + collectionName + ".entrySet().iterator();"
                );
        }
        else
        if ( collectionType == CollectionType.PROPERTIES )
        {
            nextProgramLine();
            outputRaw(
                "final java.util.Enumeration " 
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
        if ( collectionType == CollectionType.ITERATOR )
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
        increaseProgramIndentation();
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
            if ( collectionType == CollectionType.ITERATOR )
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
            if ( collectionType == CollectionType.VECTOR )
            {
                nextProgramLine();
                outputRaw(
                    elementName + " = (" + elementType + ") "
                    + collectionName + ".elementAt(" + iVarName + ");"
                    );
            }
            else
            {
                nextProgramLine();
                outputRaw(
                    elementName + " = " + collectionName + "[" + iVarName + "];"
                    );
            }
        }

        endProgramLine();

        return result;
    }
    
    public
    void
    endCollectionIterationBlock(
        Object foreachExpression
        )
    {
        decreaseProgramIndentation();
        conditionallyEndTemplateLine();
        nextProgramLine();
        outputRaw("}");
//        outputRaw("}  /* foreach " + foreachExpression.toString() + " */");
        decreaseProgramIndentation();
        nextProgramLine();
        outputRaw("}");
        endProgramLine();
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

        conditionallyEndTemplateLine();
        startProgramLine();

        outputRaw(
            "for ( " + first + "; " + last + "; " + step + " )"
            );
        
        nextProgramLine();
        outputRaw("{");
        increaseProgramIndentation();
        endProgramLine();

        return result;
    }
    
    public
    void
    endSequencedIterationBlock(
        Object nothing
        )
    {
        decreaseProgramIndentation();
        conditionallyEndTemplateLine();
        nextProgramLine();
        outputRaw("}  // for");
        endProgramLine();
    }

    public
    Object
    startForBlock(
        String expression
        )
    {
        conditionallyEndTemplateLine();
        startProgramLine();

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
        increaseProgramIndentation();
        endProgramLine();

        return null;
    }
    
    public
    void
    endForBlock(
        Object nothing
        )
    {
        decreaseProgramIndentation();
        conditionallyEndTemplateLine();
        nextProgramLine();
        outputRaw("}  // for");
        endProgramLine();
    }
}
