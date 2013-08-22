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

import java.io.PrintStream;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
    Error handler for SAX parse errors and other errors.
**/
public
class ErrorHandler
implements org.xml.sax.ErrorHandler
{
//    private static final String rcsID_ = 
//        "$Id: ErrorHandler.java,v 1.8 2004/02/24 07:47:08 jaydunning Exp $";

    protected int nErrors_ = 0;
    protected PrintStream errorStream_;

    public
    ErrorHandler(
        PrintStream errorStream
        )
    {
        errorStream_ = errorStream;
    }
    
    /**
        True if any errors have been reported.
    **/
    public
    boolean
    foundErrors()
    {
        return nErrors_ > 0;
    }
    
    /**
        Reports a warning message; execution continues and output is 
        assumed to be acceptable.
    **/
    public 
    void 
    warning(
        SAXParseException x
        ) 
    {
        message(x, "Warning");
    }

    /**
        Reports an error message; execution continues but output is 
        assumed to be unacceptable.
    **/
    public 
    void 
    error(
        SAXParseException x
        ) 
    {
        ++nErrors_;
        message(x, "Error");
    }

    /**
        Reports an error message; execution terminates immediately.
    **/
    public 
    void 
    fatalError(
        SAXParseException x
        ) 
    throws SAXException 
    {
        message(x, "Fatal");
        throw new FatalError();
    }

    /**
        Reports a warning message; execution continues and output is 
        assumed to be acceptable.
    **/
    public
    void
    warning(
        String message,
        Locator locator
        )
    {
        message(message, "Warning", locator);
    }
    
    /**
        Reports an error message; execution continues but output is 
        assumed to be unacceptable.
    **/
    public
    void
    error(
        String message,
        Locator locator
        )
    {
        ++nErrors_;
        message(message, "Error", locator);
    }
    
    /**
        Reports an error message; execution terminates immediately.
    **/
    public
    void
    fatal(
        String message,
        Locator locator
        )
    {
        message(message, "Fatal", locator);
        throw new FatalError();
    }
    
    /**
        Reports a warning message; execution continues and output is 
        assumed to be acceptable.
        <p>
        This method should be used only if the location of the error is
        unknown.
    **/
    public
    void
    warning(
        String message,
        String fileName
        )
    {
        message(message, "Warning", fileName);
    }
    
    /**
        Reports an error message; execution continues but output is 
        assumed to be unacceptable.
        <p>
        This method should be used only if the location of the error is
        unknown.
    **/
    public
    void
    error(
        String message,
        String fileName
        )
    {
        ++nErrors_;
        message(message, "Error", fileName);
    }
    
    /**
        Reports an error message; execution terminates immediately.
        <p>
        This method should be used only if the location of the error is
        unknown.
    **/
    public
    void
    fatal(
        String message,
        String fileName
        )
    {
        message(message, "Fatal", fileName);
        throw new FatalError();
    }
    
    protected
    void
    message(
        SAXParseException x,
        String errorLevel
        )
    {
        errorStream_.println(
            location(
                x.getSystemId(), 
                x.getPublicId(),
                x.getLineNumber(),
                x.getColumnNumber()
                )
            + errorLevel + ": "
            + x.getMessage()
            );
    }
    
    protected
    void
    message(
        String message,
        String errorLevel,
        Locator locator
        )
    {
        errorStream_.println(
            location(
                locator.getSystemId(), 
                locator.getPublicId(),
                locator.getLineNumber(),
                locator.getColumnNumber()
                )
            + errorLevel + ": "
            + message
            );
    }

    protected
    void
    message(
        String message,
        String errorLevel,
        String fileName
        )
    {
        errorStream_.println(
            fileName + ": "
            + errorLevel + ": "
            + message
            );
    }

    private
    String
    location(
        String systemID,
        String publicID,
        int lineNumber,
        int columnNumber
        )
    {
        StringBuffer buf = new StringBuffer(
            (systemID != null ? systemID.length() : 0)
            + (publicID != null ? publicID.length() : 0)
            + 21);
        if ( systemID != null )
        {
            buf.append(systemID);
        }
        if ( publicID != null )
        {
            if ( buf.length() != 0 )
            {
                buf.append("::");
            }
            buf.append(publicID);
        }

        buf.append('(');
        buf.append(lineNumber);
        buf.append("): ");
        if ( columnNumber != -1 )
        {
            buf.append("(column ");
            buf.append(columnNumber);
            buf.append(") ");
        }

        return buf.toString();
    }
}
