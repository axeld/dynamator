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

import java.util.Vector;

/**
    Manages indentation.
    <p>
    Output is produced by both Generator and Annotations.  Both may
    emit newlines, and occasionally it is pleasant to properly indent
    the Annotations output.  This class isolates indentation policy and
    state, allowing access from both Generator and Annotations.
**/
public
class Indentation
{
//    private static final String rcsID_ = 
//        "$Id: Indentation.java,v 1.7 2004/03/23 07:36:49 jaydunning Exp $";

    private String string_;
    private String current_ = "";
    private boolean on_ = true;
    private int level_ = 0;
    private final Vector indents_ = new Vector();

    public
    Indentation(
        String indentString
        )
    {
        set(indentString);
        indents_.addElement("");
    }
    
    /**
        The indentation string for one indent level.

        @return
            a sequence of spaces,
            or null if indentation is off.
    **/
    public
    String
    string()
    {
        return string_;
    }
    
    /**
        Set the indentation string for one indent level.
    **/
    public
    void
    set(
        String indentString           // null to turn indent off
        )
    {
        if ( level_ != 0 )
        {
            throw new IllegalStateException(
                "Cannot set indentation when indentation is in effect");
        }
        
        string_ = indentString;
        indents_.setSize(0);
    }
    
    /**
        Increase the size of the indentation string by one indent level.
    **/
    public
    void
    increase()
    {
        if ( string_ != null )
        {
            ++level_;
            while ( indents_.size() < level_ + 1)
            {
                indents_.addElement(
                    string_ + indents_.lastElement().toString());
            }
            current_ = indents_.elementAt(level_).toString();
        }
    }
    
    /**
        Decrease the size of the indentation string by one indent level.
    **/
    public
    void
    decrease()
    {
        if ( string_ != null )
        {
            --level_;
            if ( level_ < 0 )
            {
                level_ = 0;
            }
            current_ = indents_.elementAt(level_).toString();
        }
    }
    
    /**
        The current indentation string.  (Always a sequence of space
        characters; may be zero characters in length.)
    **/
    public
    String
    current()
    {
        return on_ ? current_ : "";
    }

    /**
        Turn indentation off temporarily.
    **/
    public
    void
    off()
    {
        on_ = false;
    }

    /**
        Turn indentation back on.
    **/
    public
    void
    on()
    {
        on_ = true;
    }
}
