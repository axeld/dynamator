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

import java.io.IOException;
import java.io.PrintStream;

/**
    Highest-level command entry.  Dynamically determines whether to
    invoke HTML or XML versions of Dynamator.
**/
public
class
dynamate
{
//    private static final String rcsID_ = 
//        "$Id: dynamate.java,v 1.8 2004/03/23 07:36:04 jaydunning Exp $";

    public static
    class Arguments
    {
        private ArgumentParser parser_;
        protected Options options_;

        public
        Arguments(
            String[] args,
            Options options
            )
        {
            parser_ = new ArgumentParser(args);
            options_ = options;
        }

        public
        void
        parse()
        throws IOException
        {
            if ( ! parser_.hasMoreArguments() )
            {
                usage();
                return;
            }
            
            String arg;
            while ( parser_.hasMoreArguments() )
            {
                arg = parser_.nextArgument();
                if ( ! handleArgument(arg) )
                {
                    usage();
                    return;
                }
            }
        }

        protected
        boolean
        handleArgument(
            String arg
            )
        throws IOException
        {
            if ( arg.length() == 1 )
            {
                return false;
            }
    
            if ( arg.charAt(0) == '-' )
            {
                return options_.handleArgument(arg, parser_);
            }

            Command.process(options_, arg);
            return true;
        }

        public
        void
        usage()
        {
            options_.errorStream.println(
                "Usage: java dynamate [options] file ..."
                );
    
            options_.errorStream.println(
                "Options: \n"
                );

            options_.printOptions();
        }
    }
    
    /**
        Invoke programmatically with program-specified options,
        propagating exceptions to caller.
    **/
    public static 
    void
    invoke(
        String[] args,
        Options options
        )
    throws IOException
    {
        Arguments arguments = new Arguments(args, options);
        try
        {
            arguments.parse();
        }
        finally
        {
            Trace.flush();
            
            // only close Trace if set via options...
            // it might have been set from Ant
            if ( options.doTrace )
            {
                Trace.close();
            }
        }
    }

    /**
        Invoke programmatically,
        propagating exceptions to caller.
    **/
    public static 
    void
    invoke(
        String[] args
        )
    throws IOException
    {
        invoke(args, new Options());
    }

    /**
        Invoke programmatically,
        reporting errors and exceptions to 'errorStream'.
    **/
    public static 
    boolean
    invoke(
        String[] args,
        PrintStream errorStream
        )
    {
        Options options = new Options();
        options.errorStream = errorStream;
        try
        {
            invoke(args, options);
        }
        catch(FatalError x)
        {
            if ( x.getMessage() != null
                && x.getCause() == null )
            {
                errorStream.println("Dynamator: " + x.getMessage());
            }
            else
            if ( x.getCause() != null )
            {
                x.printStackTrace(errorStream);
            }
            return false;
        }
        catch(Throwable x)
        {
            x.printStackTrace(errorStream);
            return false;
        }
        finally
        {
            Trace.flush();

            // only close Trace if set via options...
            // it might have been set from Ant
            if ( options.doTrace )
            {
                Trace.close();
            }
        }
        return true;
    }

    /**
        For command-line invocation only (calls System.exit if 
        error encountered).
    **/
    public static
    void
    main(
        String[] args
        )
    {
        boolean result = invoke(args, System.err);
        if ( ! result )
        {
            System.exit(-1);
        }
    }
}
