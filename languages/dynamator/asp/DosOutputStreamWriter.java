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

package dynamator.asp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
    Ensures each line feed ('\n') is preceded by a carriage return ('\r').
**/
public
class DosOutputStreamWriter
extends java.io.Writer
{
//    private static final String rcsID_ = 
//        "$Id: DosOutputStreamWriter.java,v 1.4 2004/02/24 07:47:03 jaydunning Exp $";

    private Writer writer_;
    
    public
    DosOutputStreamWriter(
        OutputStream out
        )
    {
        writer_ = new OutputStreamWriter(out);
    }
    
    public
    DosOutputStreamWriter(
        OutputStream out,
        String encoding
        )
    throws UnsupportedEncodingException
    {
        writer_ = new OutputStreamWriter(out, encoding);
    }
    
    public
    DosOutputStreamWriter(
        Writer out
        )
    {
        writer_ = out;
    }
    
    private int prevC = -1;
    
    public
    void
    write(
        int c
        )
    throws IOException
    {
        if ( c == '\n' 
            && prevC != '\r' )
        {
            writer_.write('\r');
        }
        prevC = c;
        writer_.write(c);
    }
    
    public
    void
    write(
        char[] buf,
        int offset,
        int length
        )
    throws IOException
    {
        --offset;
        while ( ++offset < length )
        {
            write((int) buf[offset]);
        }
    }

    public
    void
    write(
        String s,
        int offset,
        int length
        )
    throws IOException
    {
        write(s.toCharArray(), offset, length);
    }

    public
    void
    flush()
    throws IOException
    {
        writer_.flush();
    }

    public
    void
    close()
    throws IOException
    {
        writer_.close();
    }
}
