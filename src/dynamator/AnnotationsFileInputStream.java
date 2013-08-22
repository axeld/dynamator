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

/**
    Input stream for reading an annotations file.
    In addition to processing performed by FakeoutSaxInputStream, this
    class adds location information to each element for better error
    messagess, and wraps the content of program elements in a CDATA
    block so that developers don't have to perform XML translation 
    (e.g. 'x &lt; 5' to 'x &amp;lt; 5').
    <p>
    The content of the following elements are wrapped in CDATA blocks:
    <ul>
      <li>after
      <li>after-content
      <li>after-extracts
      <li>before
      <li>before-content
      <li>before-extracts
      <li>content
      <li>epilog
      <li>extract
      <li>for
      <li>foreach
      <li>if
      <li>prolog
      <li>raw-content
    </ul>
**/
public abstract
class AnnotationsFileInputStream
extends FakeoutSaxInputStream
{
    public static
    class Default
    extends AnnotationsFileInputStream
    {
        public
        Default(
            File file
            )
        throws IOException
        {
            super(file);
        }
    
        public
        Default(
            File file,
            String[] cdataContentTags
            )
        throws IOException
        {
            super(file, cdataContentTags);
        }
    }
    
//    private static final String rcsID_ = 
//        "$Id: AnnotationsFileInputStream.java,v 1.8 2004/03/21 22:16:56 jaydunning Exp $";

    private static final String CDATA_START = "<![CDATA[";
    private static final String CDATA_END   = "]]>";
    
    // can't be final in JDK 1.1
    private String[] cdataContentTags_; 

    private static final String[] defaultCdataContentTags_ = 
        new String[]
        {
            // These must be in sort order!!
            "after",
            "after-content",
            "after-extracts",
            "before",
            "before-content",
            "before-extracts",
            "content",
            "epilog",
            "extract",
            "for",
            "foreach",
            "if",
            "prolog",
            "raw-attrs",
            "raw-content"
        };

    private static final String[] dynamatorCopyTags_ = 
        new String[]
        {
            // These must be in sort order!!
            "after",
            "after-content",
            "before",
            "before-content",
            "content",
            "epilog",
            "prolog",
            "raw-content"
        };

    private boolean dynamatorTagEncountered_ = false;
    
    protected
    AnnotationsFileInputStream(
        File file
        )
    throws IOException
    {
        super(file);
        cdataContentTags_ = defaultCdataContentTags_;
    }

    protected
    AnnotationsFileInputStream(
        File file,
        String[] cdataContentTags
        )
    throws IOException
    {
        super(file);
        cdataContentTags_ = 
            Utils.join(defaultCdataContentTags_, cdataContentTags);
        Utils.sort(cdataContentTags_);
    }

    /**
        Wraps the content of dynamator program-containing elements with 
        CDATA, and adds to all elements a location attribute for better
        error messages.
    **/
    protected
    StringBuffer
    handleTagStart()
    throws IOException
    {
        // This method is very similar to the one in
        // XmlFileInputStream. 

        StringBuffer result = new StringBuffer(64);

        int c;
        while ( (c = rawRead()) != -1
            && ! Character.isWhitespace((char)c)
            && c != '>' 
            && c != '/')
        {
            result.append((char) c);
        }

        String tagname = result.toString().substring(1);
        if ( tagname.length() == 0 
            && c == '/' )
        {
            result.append((char)c);
            return result;
        }
        
        if ( ! dynamatorTagEncountered_ )
        {
            dynamatorTagEncountered_ = tagname.equals("dynamator");
        }
        
        result.append(" _dyn_loc_=\"");
        result.append(fileName());
        result.append(':');
        result.append(lineNumber());
        result.append("\"");

        result.append((char)c);
        
        if ( Utils.binarySearch(cdataContentTags_, tagname) )
        {
            int lineNumber = lineNumber();
            if ( c != '>' )
            {
                result.append(String.valueOf(accumulateTag()));
            }
            if ( result.charAt(result.length()-2) != '/' )
            {
                result.append(CDATA_START);
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
                            result.insert(p-2, CDATA_END);
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

                if ( Utils.binarySearch(dynamatorCopyTags_, tagname) )
                {
                    int offset = 0;
                    while ( 0 <= (offset = Utils.indexOf(
                        result, "<dynamator:copy", offset)) )
                    {
                        result.insert(offset, CDATA_END);
                        offset = Utils.indexOf(
                            result, '>', offset+CDATA_END.length());
                        if ( offset < 0 )
                        {
                            throw new FatalError(
                                fileName() + '(' + lineNumber + "): " 
                                + "End not found for <dynamator:copy");
                        }
                        ++offset;
                        result.insert(offset, CDATA_START);
                    }
                }
                else
                if ( "foreach".equals(tagname) )
                {
                    int p = Utils.indexOf(result, "<if");
                    if ( p >= 0 )
                    {
                        result.insert(p, CDATA_END);
                        p = Utils.indexOf(result, '>', p+CDATA_END.length());
                        if ( p < 0 )
                        {
                            throw new FatalError(
                                fileName() + '(' + lineNumber + "): " 
                                + "End not found for <if");
                        }
                        result.insert(p+1, CDATA_START);

                        p = Utils.indexOf(
                            result, "</if", p+CDATA_START.length()+1);
                        if ( p < 0 )
                        {
                            throw new FatalError(
                                fileName() + '(' + lineNumber + "): " 
                                + "</if> not found");
                        }
                        result.insert(p, CDATA_END);
                        p = Utils.indexOf(result, '>', p+CDATA_END.length());
                        if ( p < 0 )
                        {
                            throw new FatalError(
                                fileName() + '(' + lineNumber + "): " 
                                + "End not found for <if");
                        }
                        result.insert(p+1, CDATA_START);
                    }
                }
            }
        }        

        return result;
    }

    protected
    boolean
    piShouldBeTreatedAsData(
        StringBuffer pi
        )
    {
        return dynamatorTagEncountered_;
    }
    
    /**
        Test driver.  Places output on System.out.
        <p>
        Usage: java dynamator.AnnotationsFileInputStream <i>filename</i>
    **/
    public static void main(String[] args)
    throws Exception
    {
        AnnotationsFileInputStream s = 
            new AnnotationsFileInputStream.Default(new File(args[0]));
        int c;
        while ( (c = s.read()) != -1 )
        {
            System.out.print((char)c);
        }
    }
}
    
