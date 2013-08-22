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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
    Limited global trace utility.
**/
public 
class Trace
    // NOT THREAD SAFE
{
//    private static final String rcsID_ = 
//        "$Id: Trace.java,v 1.9 2004/03/21 22:16:56 jaydunning Exp $";

    public static char parmFlag = '#';

    public static boolean on = false;

//    private static final String prefixElement_ = "  | ";
    private static final String prefixElement_ = "    ";
    private static final String callPrefix_    = "  ";
    private static final String returnPrefix_  = "  ";

    private static PrintWriter defaultWriter_ = 
        new PrintWriter(System.err, true);
    private static PrintWriter writer_ = defaultWriter_;

    private static StringBuffer stackPrefix_ = new StringBuffer();

    private static Object prefix_ = "";
    
    public static
    void
    setPrefix(
        Object value
        )
    {
        prefix_ = value;
    }
    
    public static
    void
    clearPrefix()
    {
        prefix_ = "";
    }
    
    public static
    void
    display(
        String msg
        )
    {
        if ( on )
        {
            printLine(msg);
        }
    }

    public static
    void
    enter(
        String functionName
        )
    {
        if (on)
        {
            printLine(callPrefix_ + functionName);
            stackPrefix_.append(prefixElement_);
        }
    }

    public static
    void
    leave()
    {
        leave("");
    }    

    public static
    void
    leave(
        String s
        )
    {
        if ( on )
        {
            if ( stackPrefix_.length() > 0 )
            {
                stackPrefix_.setLength(
                    stackPrefix_.length() - prefixElement_.length());
            }

            printLine(returnPrefix_ + s);
        }
    }

    public static
    void
    open(
        String filename
        )
    throws IOException
    {
        if ( writer_ != defaultWriter_ )
        {
            writer_.close();
        }

        writer_ = 
            new PrintWriter(
                new BufferedOutputStream(
                    new FileOutputStream(filename)), 
                true);
    }
    
    public static
    void
    flush()
    {
        if ( writer_ != defaultWriter_ )
        {
            writer_.flush();
        }
    }

    public static
    void
    close()
    {
        if ( writer_ != defaultWriter_ )
        {
            writer_.close();
            writer_ = defaultWriter_;
        }
    }
    
    private static
    void
    printLine(
        String message
        )
    {
        writer_.print(prefix_.toString());
        writer_.print(stackPrefix_);
        writer_.println(message);
    }
}
