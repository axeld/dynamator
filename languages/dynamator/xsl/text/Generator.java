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

package dynamator.xsl.text;

import java.util.Date;

public
class Generator
extends dynamator.Generator
{
    protected
    void
    init()
    {
        options_.indentation.set(null);
    }
    
// For some time during development of this feature, text was 
// wrapped in CDATA to escape special XML characters (specifically,
// '&').  This didn't seem to add enough value to justify the extra
// noise in the generated template.

//    private boolean inCDATA_ = false;
    
    private
    void
    startCDATA()
    {
//        if ( ! inCDATA_ )
//        {
//            outputRaw("<![CDATA[");
//        }
//        inCDATA_ = true;
    }

    private
    void
    endCDATA()
    {
//        if ( inCDATA_ )
//        {
//            outputRaw("]]>");
//            inCDATA_ = false;
//        }
    }

    public
    void
    outputTemplate(
        char c
        )
    {
        // must output optional text because it might be 
        // between two template text blocks.
        outputOptionalText();
        startCDATA();
        outputRaw(c);
    }
       
    public
    void
    outputTemplate(
        char[] buf,
        int offset,
        int length
        )
    {
        // must output optional text because it might be 
        // between two template text blocks.
        outputOptionalText();
        startCDATA();
        outputRaw(buf, offset, length);        
    }
       
    public
    void
    outputTemplate(
        String s
        )
    {
        // must output optional text because it might be 
        // between two template text blocks.
        outputOptionalText();
        startCDATA();
        outputRaw(s);
    }
       
    public
    void
    endProgramLine()
    {
        inProgramLine(false);
    }
    
    public
    void
    outputProgram(  
        char[] buf,
        int offset,
        int length
        )
    {
//        outputOptionalText();
        optionalText_ = null;
        endCDATA();
        while ( --length >= 0 )
        {
            outputRaw(buf[offset++]);
        }
    }
    
    private String optionalText_ = null;
    
    public
    boolean // contents were output
    outputWhitespace(
        char[] buf,
        int offset,
        int length
        )
    {
        optionalText_ = new String(buf, offset, length);
        return true;
    }
    
    private
    void
    outputOptionalText()
    {
        /*
            optional text is any text between tags that is 
            all whitespace.  Since the generator doesn't have
            access to the DOM or even an XML interpretation of the 
            generated code this is problematic.
            
            The problem is, the generator can't tell the 
            difference between
            --
                <choose>
                  <when...>
            --
            and
            --
                <datatype/>
                <accessor-name/>();
            --
                 
            Whitespace may be ignored in the first example, but
            must be explicitly specified in the second:
            --
                <xsl:choose>
                  <xsl:when>
            --
                <xsl:value-of select="@datatype"/><xsl:text>
                </xsl:text><xsl:value-of select="@accessor-name"/();
            --

            There may be a better way (dnl?).  But for now, 
            elements that don't allow text content must not have
            text content (i.e. whitespace becomes significant):
                <choose
                  ><when-x>
                    some text</when-x
                  ><when-y>
                    some other text</when-y
                ></choose>
        */
        
        if ( optionalText_ != null )
        {
            endCDATA();
            outputRaw("<xsl:text>");
            outputRaw(optionalText_);
            outputRaw("</xsl:text>");
            optionalText_ = null;
        }
    }
    
    public
    void
    outputDynamicValueExpression(
        String value
        )
    {
        outputOptionalText();
        outputProgram(value.trim());
    }
    
    public
    void
    outputDynamicAttributeValue(
        String value
        )
    {
        outputRaw(value.trim());
    }

    public
    void
    start(
        boolean produceGenerationComment
        )
    {
        if ( produceGenerationComment )
        {
            outputRaw("<!-- generated by Dynamator " + new Date() + " -->");
        }
    }
    
    /**
        Output an XML element beginning with '&lt;!'.
    */
    public
    void
    outputXmlEscape(
        String elementText
        )
    {
        endCDATA();
        outputRaw("<xsl:text disable-output-escaping=\"yes\">");
        outputRaw(elementText);
        outputRaw("</xsl:text>");
    }
    
    /**
        Output an XML processing instruction (syntax &lt;?...?&gt;)
        This variant adds some text around the PI to prevent XSL from
        eating it.

        @param elementText 
        The content of the processing instruction tag, not including
        the &lt;? or ?&gt;
    */
    public
    void
    outputProcessingInstruction(
        String elementText
        )
    {
        endCDATA();
        outputRaw("<xsl:text disable-output-escaping=\"yes\"><![CDATA[<?");
        outputRaw(elementText);
        outputRaw("?>]]></xsl:text>");
    }
    
    /**
        Output a CDATA block.

        @param sectionText 
        The content of the CDATA block, not including the CDATA.
    */
    public
    void
    outputCDATA(
        String sectionText
        )
    {
        endCDATA();
        outputRaw("<xsl:text disable-output-escaping=\"yes\"><![CDATA[");
        outputRaw(sectionText);
        outputRaw("]</xsl:text>");
        outputRaw("<xsl:text disable-output-escaping=\"yes\">");
        outputRaw("]></xsl:text>");
    }

    /**
        True if template line was ended.
    **/
    public
    boolean
    conditionallyEndTemplateLine(
        boolean newline
        )
    {
        outputOptionalText();
        endCDATA();
        return false;
    }

    public
    Object          // if-expression
    startIfBlock(
        String expression,
        boolean newLine
        )
    {
        expression = expression.trim();     // just in case
        String condition = 
            expression.charAt(0) == '!'
            ? expression.substring(1).trim()
            : expression;
        String not =
            expression.charAt(0) == '!'
            ? "not("
            : "";
        String notEnd =
            expression.charAt(0) == '!'
            ? ")"
            : "";

        String ifExpression = 
            not
            + condition
            + notEnd;
        outputOptionalText();
        outputProgram("<xsl:if test=\"" + ifExpression + "\">");

        return ifExpression;
    }
    
    public
    void
    endIfBlock(
        Object startResult,
        boolean newLine
        )
    {
        outputProgram("</xsl:if>");
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
        // We only care about collectionExpression.
        // We might like to care about iName, except for xsl's silly 
        // requirement that all variables are const.

//        outputOptionalText();
        outputProgram("<xsl:for-each select=\"" + collectionExpression + "\">");

        return collectionExpression;
    }
    
    public
    void
    endCollectionIterationBlock(
        Object foreachExpression
        )
    {
        outputProgram("</xsl:for-each>");
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
        error(
            "Sequenced iteration block not available for xsl");
        return null;
    }
    
    public
    void
    endSequencedIterationBlock(
        Object nothing
        )
    {}

    public
    Object
    startForBlock(
        String expression
        )
    {
        error(
            "<for> not available for xsl");
        return null;
    }
    
    public
    void
    endForBlock(
        Object nothing
        )
    {}
}
