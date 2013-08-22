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
                    
/**
    Enumerator for command-line arguments.
**/
public
class ArgumentParser
{
//    private static final String rcsID_ = 
//        "$Id: ArgumentParser.java,v 1.3 2004/02/21 18:02:03 jaydunning Exp $";

    private final String[] args_;
    private int iArg_;

    public
    ArgumentParser(
        String[] args
        )
    {
        args_ = args;
        iArg_ = -1;
    }
    
    public
    boolean
    hasMoreArguments()
    {
        return iArg_ + 1 < args_.length;
    }
    
    public
    String
    nextArgument()
    {
        return args_[++iArg_];
    }
    
    /**
        Obtains the value associated with a flag that takes a value; 
        e.g. for '-xArgument' or '-x Argument', returns 'Argument'.
        <p>
        If the flag is not followed by an argument, returns null.
        <p>
        Precondition: 'nextArgument()' must have returned a flag; i.e.
        an argument that starts with '-'.
        Postcondition: 'nextArgument()' returns the argument after the
        flag argument value.
    **/
    public
    String
    flagArgumentValue()
    {
        String result = null;
        String arg = args_[iArg_];
        if ( arg.length() == 2 )
        {
            if ( hasMoreArguments() )
            {
                arg = nextArgument();
                if ( arg.charAt(0) != '-' )
                {
                    result = arg;
                }
                else
                {
                    --iArg_;
                }
            }
        }
        else
        {
            result = arg.substring(2);
        }
        return result;
    }    
}
