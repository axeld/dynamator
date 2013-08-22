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

package dynamator;

import java.io.File;
import java.io.IOException;

/**
    Default input stream for XML and HTML files.
    This input stream wraps the content of &lt;script&gt; elements in a
    CDATA tag so that Javascript expressions don't mess up Sax.
**/
public
class XmlFileInputStream
extends FakeoutSaxInputStream
{
//    private static final String rcsID_ = 
//        "$Id: XmlFileInputStream.java,v 1.6 2004/02/24 07:47:09 jaydunning Exp $";

    public
    XmlFileInputStream(
        File file
        )
    throws IOException
    {
        super(file);
    }

    /**
        Wraps the content of script elements with CDATA so that
        Xml-active character sequences do not cause trouble.
        &lt;span&gt; elements within a script are not wrapped so that 
        DynamateXML may process them if necessary.
    **/
    protected
    StringBuffer
    handleTagStart()
    throws IOException
    {
        // This method is very similar to the one in
        // AnnotationsFileInputStream. 

        // The script escape behavior probably belongs in an 
        // HTML-specific class, but this will do for now.

        StringBuffer result = new StringBuffer(64);

        int c;
        while ( (c = rawRead()) != -1
            && ! Character.isWhitespace((char)c)
            && c != '>' )
        {
            result.append((char) c);
        }

        String tagname = result.toString().substring(1);
        
        result.append((char)c);
        
        if ( tagname.equalsIgnoreCase("script") )
        {
            int lineNumber = lineNumber();
            if ( c != '>' )
            {
                result.append(accumulateTag());
            }
            if ( result.charAt(result.length()-2) != '/' )
            {
                result.append("<![CDATA[");
                while ( (c = rawRead()) != -1 )
                {
                    // continue until matching </tag> is found
                    // ASSUMES no whitespace before '>', e.g. "</tag >" 
                    if ( c == '>' )
                    {
                        int p = result.length() - tagname.length();
                        if ( result.charAt(p-2) == '<'
                            && result.charAt(p-1) == '/'
                            && tagname.equals(
                                Utils.substring(result,p,result.length())) )
                        {
                            result.insert(p-2, "]]>");
                            result.append((char)c);
                            break;
                        }
                        else
                        {
                            result.append((char)c);
                        }
                    }
                    else
                    {
                        result.append((char)c);
                    }
                }
                if ( c == -1 )
                {
                    throw new FatalError(
                        fileName() + '(' + lineNumber + "): "
                        + "End tag not found for <" + tagname + ">");
                }
            }

            /*
                According to the HTML spec, the character sequence "</"
                is invalid within a script, and must be escaped.  For
                example, document.write("<em>x</em>") is invalid, and 
                should be written as document.write("<em>x<\/em>").
                Well, except for VBScript, where it would be
                "<em>x" & Chr(47) & "em>".

                Within a script element with language="javascript",
                Tidy changes all </ character sequences (inside or
                outside quoted strings) to <\/.  
                
                However, no-content elements (<xxx/>) are valid within
                a script, and Tidy doesn't change them.
                
                There are three things that people might want to do with
                a script:
                - remove it
                - replace it
                - set a variable value within it
                
                The first two are already supported at the <script>
                element level.  The last one can be supported using 
                the pattern described in the tutorial:
                    variable = "<span id='variable-name'/>"

                Therefore this routine only affects the no-content form
                of span elements found within script elements.
            */
            
            // replace <span .../> with ]]><span .../><![CDATA[
            unescapeScriptEmbeds(result, "span");
        }        

        return result;
    }

    private static final String endCDATA_   = "]]>";
    private static final String startCDATA_ = "<![CDATA[";

    private
    void
    unescapeScriptEmbeds(
        StringBuffer buf,
        String tagname
        )
    {
        String startTag = "<" + tagname;
        int skip = startTag.length();
        int iStart = -1;
        int iEnd;
        while ( (iStart = Utils.indexOf(buf, startTag, ++iStart)) >= 0 )
        {
            iEnd = Utils.indexOf(buf, '>', iStart + skip);
            if ( iEnd == -1 )
            {
                // error, no close for tag
                // leave as-is
                break;
            }
            if ( buf.charAt(iEnd-1) == '/' )
            {
                buf.insert(iEnd+1, startCDATA_);
                iEnd += startCDATA_.length();
                buf.insert(iStart, endCDATA_);
                iEnd += endCDATA_.length();
            }

            iStart = iEnd;
        }
    }
    
    /**
        Test driver.  Places output on System.out.
        <p>
        Usage: java dynamator.XmlFileInputStream <i>filename</i>
    **/
    public static void main(String[] args)
    throws Exception
    {
        XmlFileInputStream input = 
            new XmlFileInputStream(new File(args[0]));
        int c;
        while ( (c = input.read()) != -1 )
        {
            System.out.print((char)c);
        }
    }
}
    

