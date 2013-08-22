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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
    An input stream that allows XML markup to be ignored by an XML parser.
    Dynamator uses this to read both the HTML/XML input file and the
    annotations file.
    <p>
    Makes sure SAX doesn't see any of the following:
    <ul>
    <li>XML declarations (like &lt;!DOCTYPE...)
    <li>XML entities (like &amp;nbsp;)
    <li>XML processing instructions (like &lt;?xml ... &gt;)
    <li>... and handles code blocks correctly without requiring
        CDATA...
    </ul>
    <p>
    ... and that everything gets output in its original form.
    <p>
    This requires a bit of cooperation with the client in the form of
    special processing instructions added to the input stream here
    and removed from the output stream by the client:
    <dl>
    <dt>&lt;?fakeout_passthru ... ?&gt;
    <dd>Encloses any XML content such as DOCTYPE, NOTATION.
    <dt>&lt;?fakeout_pi ... ?&gt;
    <dd>Encloses an XML processing instruction.  (The &lt;? and &gt;?
        are not included in the content.)
    </dl>
**/
public abstract
class FakeoutSaxInputStream
extends InputStream
{
//    private static final String rcsID_ = 
//        "$Id: FakeoutSaxInputStream.java,v 1.11 2004/03/23 07:36:49 jaydunning Exp $";

    private StringBuffer buf_ = new StringBuffer(256);
    private int pBuf_ = -1;
    private String fileName_;
    private int lineNumber_ = 1;

    private PushbackInputStream input_;

    protected
    FakeoutSaxInputStream(
        File file
        )
    throws IOException
    {
        fileName_ = file.toString();

        int bufsize = (int) Math.min(8192L, file.length());
        
        input_ = 
            new PushbackInputStream(
                new BufferedInputStream(
                    new FileInputStream(file),
                    bufsize)
                , 2);
    }

    /**
        Accumulate the contents of a tag (not the same as the content
        of an element).
        Result buffer contains all subsequent text up to and including
        the next unquoted '&gt;'.  Input stream is positioned so that
        the next character read will be the character immediately after
        the '&gt;'.
        <p>
        An unquoted '[' is matched only by a subsequent unquoted ']'.
        This allows this method to accumulate an entire DOCTYPE 
        declaration.
        <p>
        This method may be called when the input stream position is
        anywhere except within an attribute value.
    **/
    protected
    StringBuffer
    accumulateTag()
    throws IOException
    {
        int lineNumber = lineNumber_;
        StringBuffer buf = new StringBuffer(64);
        int c;
        final int NONE = 0;
        int quote = NONE;
        boolean inBracket = false;
        while ( (c = rawRead()) != -1 )
        {
            buf.append((char)c);
            if ( quote == NONE )
            {
                if ( ! inBracket && c == '>' )
                {
                    break;
                }

                if ( c == '\'' || c == '"' )
                {
                    quote = c;
                }
                else
                if ( c == '[' )
                {
                    inBracket = true;
                }
                else
                if ( c == ']' && inBracket )
                {
                    inBracket = false;
                }
            }
            else
            if ( c == quote )
            {
                quote = NONE;
            }
        }

        if ( c == -1 )
        {
            // didn't find an end
            String tag = buf.toString().substring(0, 20);
            int pSpace = tag.indexOf(' ');
            if ( pSpace >= 0 )
            {
                tag = tag.substring(0, pSpace);
            }
            throw new IOException(
                fileName_ + '(' + lineNumber + "): "
                + "Tag not terminated: " + tag);
        }

        return buf;
    }
    
    /**
        Accumulate the contents of a CDATA section.
    **/
    protected
    StringBuffer
    accumulateCDATA()
    throws IOException
    {
        int lineNumber = lineNumber_;
        StringBuffer buf = new StringBuffer(64);
        int c;
        while ( (c = rawRead()) != -1 )
        {
            buf.append((char)c);
            if ( c == '>' && Utils.endsWith(buf,"]]>") )
            {
                break;
            }
        }

        if ( c == -1 )
        {
            // didn't find an end
            throw new IOException(
                fileName_ + '(' + lineNumber + "): "
                + "CDATA section not terminated");
        }

        return buf;
    }
    
    
    /**
        Accumulate the contents of a comment.
    **/
    protected
    StringBuffer
    accumulateComment()
    throws IOException
    {
        int lineNumber = lineNumber_;
        StringBuffer buf = new StringBuffer(64);
        int c;
        while ( (c = rawRead()) != -1 )
        {
            buf.append((char)c);
            if ( c == '>' && Utils.endsWith(buf,"-->") )
            {
                break;
            }
        }

        if ( c == -1 )
        {
            // didn't find an end
            throw new IOException(
                fileName_ + '(' + lineNumber + "): "
                + "Comment not terminated");
        }

        return buf;
    }
    
    protected
    int
    rawRead()
    throws IOException
    {
        int result = input_.read();
        if ( result == '\n' )
        {
            ++lineNumber_;
        }
        return result;
    }
    
    public
    int
    read()
    throws IOException
    {
        int result;
        if ( buf_.length() > 0 )
        {
            if ( ++pBuf_ >= buf_.length() )
            {
                buf_.setLength(0);
                pBuf_ = -1;
            }
            else
            {
                return buf_.charAt(pBuf_);
            }
        }

        result = rawRead();
        if ( result == '&' )
        {
            buf_.append("amp;");
        }
        else
        if ( result == '<' )
        {
            // --------------------------------------------------
            // By default, the '<' is returned by this function.
            // What goes into buf_ is returned by subsequent calls.
            // --------------------------------------------------
            int lookahead = rawRead();
            if ( lookahead == '!' )
            {
                lookahead = rawRead();
                if ( lookahead == '-' )
                {
                    // comment (<!-- ... -->); 
                    // SAX could handle except that the comment
                    // might contain something that might be 
                    // parsed by this routine and cause a problem, 
                    // like a start tag with no end tag
                    buf_.append('!');
                    buf_.append((char)lookahead);
                    buf_.append(accumulateComment());
                }
                else
                if ( lookahead == '[' )
                {
                    // CDATA or conditional
                    // SAX has CDATA callbacks, but this is actually easier

                    buf_.append("?fakeout_cdata ");
                    buf_.append("<!");
                    buf_.append((char)lookahead);
                    buf_.append(accumulateCDATA());
                    buf_.append(" ?>");
                }
                else
                {
                    // <!DOCTYPE, <!ELEMENT, <!ATTLIST, <!ENTITY, <!NOTATION
                    // (only DOCTYPE and NOTATION would be handled
                    // here, the rest would be handled under DOCTYPE)
                    
                    // CDATA can't occur before root node, 
                    // but <!DOCTYPE etc can.

                    buf_.append("?fakeout_passthru ");
                    buf_.append("<!");
                    buf_.append((char)lookahead);
                    buf_.append(accumulateTag());
                    buf_.append(" ?>");
                }
            }
            else
            if ( lookahead == '?' )
            {
                StringBuffer tag = accumulateTag();
                if ( piShouldBeTreatedAsData(tag) )
                {
                    buf_.append("?fakeout_pi ");
                }
                else
                {
                    buf_.append('?');
                }
                buf_.append(tag);
            }
            else
            {
                // The pushback stream is used for convenience of 
                // subclasses in handleTagStart.  
                // It allows them to look at the entire tag, 
                // possibly inserting characters in front of the tag.
                input_.unread(lookahead);
                input_.unread('<');
                StringBuffer s = handleTagStart();
                if ( s == null )
                {
                    rawRead();
                }
                else
                {
                    result = s.charAt(0);
                    // StringBuffer.substring not in JDK 1.1
                    char[] dest = new char[s.length()-1];
                    s.getChars(1,s.length(),dest,0);
                    buf_.append(dest);
                }
            }
        }

        return result;
    }

    /**
        Called when a start tag is encountered to allow a subclass to
        modify the text associated with the tag.  This function may be
        used to convert non-XML-compliant tags (such as those used by
        JSP and ASP) to XML-compliant form.
        <p>
        If the return value is null, no changes are made.  If the
        return value is non-null, the returned value is inserted into
        the stream returned by 'read()'.
        <p>
        When this method is called, the next character read from the
        input stream will be the '&lt;' at the beginning of the tag.  
        <p>
        Subclasses may read the original input stream using rawRead().
        Any characters read by this method are ignored, and must be
        transferred to the return value to be seen by this object's
        client. 
        
        @return
            The text to be inserted into the output stream, or null if
            no text is to be changed.
    **/
    protected
    StringBuffer
    handleTagStart()
    throws IOException
    {
        return null;
    }
    
    /**
        Called when an XML processing instruction is encountered to
        determine how it should be represented on the input stream.
        If this method returns true (the default), the pi will be
        prepended with '&lt;?fakeout_pi'.  If this method returns
        false, the pi will be passed to SAX as-is.

        @param pi
            The processing instruction, minus the leading '&lt;?', but
            including the trailing '?&gt;'.
        @return
            true if the processing instruction should be passed through
            to the output file;
            false if the processing instruction should be processed by
            the SAX parser.
    **/
    protected
    boolean
    piShouldBeTreatedAsData(
        StringBuffer pi
        )
    {
        return true;
    }
    
    public
    int 
    read(
        byte[] b
        )
    throws IOException
    {
        return read(b, 0, b.length);
    }

    public
    int 
    read(
        byte[] b, 
        int offset, 
        int length
        )
    throws IOException
    {
        int c;
        int i = -1;
        while ( ++i < length )
        {
            c = read();
            if ( c == -1 )
            {
                return i == 0 ? -1 : i;
            }
            b[offset + i] = (byte) c;
        }

        return i;
    }

    public
    String
    fileName()
    {
        return fileName_;
    }

    public
    int
    lineNumber()
    {
        return lineNumber_;
    }

    public
    void
    close()
    throws IOException
    {
        input_.close();
    }
}
    
