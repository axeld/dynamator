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

import java.io.PrintWriter;
import java.io.PrintStream;

/**
    An exception containing a message for the user.
    The toString method returns only the message.
    <p>
    To terminate execution immediately and display an error message
    to the user, throw an exception of this type, constructed with
    an error message.
    <p>
    To terminate execution immediately without an error message,
    throw an exception of this type, constructed with the default
    constructor.  (This may be done if other error messages were
    already generated.) 
**/
public 
class FatalError
extends RuntimeException
{
//    private static final String rcsID_ = 
//        "$Id: FatalError.java,v 1.6 2004/03/28 21:08:28 jaydunning Exp $";

    private Throwable cause_ = null;
    
    public
    FatalError()
    {
        super();
    }

    public
    FatalError(
        String message
        )
    {
        super(message);
    }

    public
    FatalError(
        Throwable cause
        )
    {
        super(cause.getMessage());
        cause_ = cause;
    }

    public
    FatalError(
        String message,
        Throwable cause
        )
    {
        super(
            cause.getMessage() == null
            ? message
            : message + ":\nCaused by " 
                + cause.toString()
            );
        cause_ = cause;
    }

    public
    void
    printStackTrace()
    {
        if ( cause_ == null )
        {
            super.printStackTrace();
        }
        else
        {
            cause_.printStackTrace();
        }
    }

    public
    void
    printStackTrace(
        PrintStream s
        )
    {
        if ( cause_ == null )
        {
            super.printStackTrace(s);
        }
        else
        {
            String m = super.getMessage();
            if ( m != null )
            {
                s.println(m);
            }
            cause_.printStackTrace(s);
        }
    }
        
    public
    void
    printStackTrace(
        PrintWriter s
        )
    {
        if ( cause_ == null )
        {
            super.printStackTrace(s);
        }
        else
        {
            String m = super.getMessage();
            if ( m != null )
            {
                s.println(m);
            }
            cause_.printStackTrace(s);
        }
    }

    public
    Throwable
    getCause()
    {
        return cause_;
    }
    
    public
    String
    toString()
    {
        return getMessage();
    }
}
