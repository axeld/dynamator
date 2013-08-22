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
import java.io.IOException;

/**
    Interface for services that convert an HTML document to XML.
**/
public
interface HtmlToXml
{
//    private static final String rcsID_ = 
//        "$Id: HtmlToXml.java,v 1.4 2004/03/28 21:10:29 jaydunning Exp $";

    class Instance
    {
        public static
        HtmlToXml
        obtain()
        {
            String concreteName = System.getProperty(
                "dynamator.HtmlToXml",
                "dynamator.HtmlToXml_Tidy"
                );
            try
            {
                return (HtmlToXml)
                    Class.forName(concreteName).newInstance();
            }
            catch (ClassNotFoundException x)
            {
                throw new RuntimeException(
                    x.toString()
                    + "(java.class.path="
                    + System.getProperty("java.class.path")
                    + ')'
                    );
            }
            catch (NoClassDefFoundError x)
            {
                throw new RuntimeException(
                    x.toString()
                    + "(java.class.path="
                    + System.getProperty("java.class.path")
                    + ')'
                    );
            }
            catch (Throwable x)
            {
                throw new RuntimeException(
                    x.toString());
            }
        }
    }

    public HtmlToXml instance = Instance.obtain();
    
    /**
        Convert an HTML file to XML using standard system encoding.
    **/
    void
    convert(
        String inputFilename,
        String outputFilename,
        PrintStream errorStream
        )
    throws IOException;

    /**
        Convert an HTML file to XML using specified Java encoding.
    **/
    void
    convert(
        String inputFilename,
        String outputFilename,
        PrintStream errorStream,
        String encoding
        )
    throws IOException;
}
