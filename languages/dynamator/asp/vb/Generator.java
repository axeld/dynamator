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

package dynamator.asp.vb;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.PrintWriter;
import java.util.Date;

import dynamator.asp.DosOutputStreamWriter;

public
class Generator
extends dynamator.Generator
{
//    private static final String rcsID_ = 
//        "$Id: Generator.java,v 1.6 2004/02/24 07:47:04 jaydunning Exp $";
    
    protected
    PrintWriter
    outputWriter(
        OutputStream rawOutputStream,
        String encoding
        )
    throws UnsupportedEncodingException
    {
        return new PrintWriter(
            new BufferedWriter(
                new DosOutputStreamWriter(
                    new OutputStreamWriter(
                        rawOutputStream,
                        encoding
                        )
                    )
                )
            );
    }
    
//    private
//    String
//    convertToVariable(
//        String reference
//        )
//    {
//        StringBuffer buf = new StringBuffer(reference.length());
//        boolean capitalizeNextCharacter = false;
//        char c;
//        int lim = reference.length();
//        int i = -1;
//        while ( ++i < lim )
//        {
//            c = reference.charAt(i);
//            if ( Character.isLetter(c) )
//            {
//                if ( buf.length() == 1 )
//                {
//                    buf.append(Character.toLowerCase(c));
//                    capitalizeNextCharacter = false;
//                }
//                else
//                if ( capitalizeNextCharacter )
//                {
//                    buf.append(Character.toUpperCase(c));
//                    capitalizeNextCharacter = false;
//                }
//                else
//                {
//                    buf.append(c);
//                }
//            }
//            else
//            {
//                capitalizeNextCharacter = true;
//            }
//        }
//
//        buf.append("__");
//        return buf.toString();
//    }

    public
    void
    outputDynamicValueExpression(
        String value
        )
    {
        value = value.trim();
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
        if ( newLine )
        {
            startProgramLine();
        }
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
            outputRaw("\r\n");
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
            outputRaw("<% ' generated by Dynamator " + new Date() + "\r\n%>");
        }
    }
    
    public
    Object          // if-expression
    startIfBlock(
        String expression,
        boolean newLine
        )
    {
        startScriptlet(newLine);
        String ifExpression = 
            "if " + expression + " then";
        outputRaw(ifExpression);
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
        outputRaw("end if");
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
        startProgramLine();
        startScriptlet(true);
        outputRaw("For Each " + elementName + " in " + collectionExpression);
        endScriptlet(true);
        indentation_.increase();

        return elementName;
    }
    
    public
    void
    endCollectionIterationBlock(
        Object foreachElement
        )
    {
        indentation_.decrease();
        startScriptlet(true);
        outputRaw("Next");
        endScriptlet(true);
    }

    public
    Object
    startSequencedIterationBlock(
        String i,
        String first,
        String last, 
        String step
        )
    {
        startProgramLine();
        startScriptlet(true);
        outputRaw("For " + i + " = " + first + " To " + last);
        if ( step != null )
        {
            outputRaw(" Step " + step);
        }
        endScriptlet(true);
        indentation_.increase();

        return i;
    }
    
    public
    void
    endSequencedIterationBlock(
        Object i
        )
    {
        indentation_.decrease();
        startScriptlet(true);
        outputRaw("Next");
        endScriptlet(true);
    }

    public
    Object
    startForBlock(
        String expression
        )
    {
        startProgramLine();
        startScriptlet(true);

        outputRaw("For ");
        outputRaw(expression);

        endScriptlet(true);
        indentation_.increase();

        return null;
    }
    
    public
    void
    endForBlock(
        Object i
        )
    {
        indentation_.decrease();
        startScriptlet(true);
        outputRaw("Next");
        endScriptlet(true);
    }
}
