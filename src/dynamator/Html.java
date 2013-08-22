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

import java.io.File;
import java.io.PrintStream;
import java.io.IOException;

/**
    Dynamator adapter for HTML documents.  
    Preprocesses the HTML document to create an XML document, and sets
    Dynamator options for XML.
**/
public
class Html
{
    // The previous design implemented a Decorator pattern; 
    // this design is less OO, but somehow better.
    
//    private static final String rcsID_ = 
//        "$Id: Html.java,v 1.5 2004/03/28 21:10:29 jaydunning Exp $";

    // html tags that shouldn't have a corresponding end tag
    private static final String defaultTagsWithoutEnd_ = 
        "|hr|input|base|isindex|link|meta|area|basefont|br|img|param|";

    // html tags that are associated with iterations...
    // ... the ones that repeat when iterated over
    private static final String defaultInclusiveIterationTags_ = 
        "|tr|option|li|";
    // ... the ones that repeat their contents when iterated over
    private static final String defaultExclusiveIterationTags_ = 
        "|dl|";

    // html tags that require strict adherance to input whitespace
    private static final String defaultPreformattedTags_ = 
        "|pre|script|";

// 20010128: browser whitespace bug
//           (although looking at this list, maybe this functionality
//           should have been here from the beginning)
    // html tags that should not generate a line break
    private static final String defaultInlineTags_ = 
        "|a|b|i|u|tt|s|strike|q|br|img|object|applet"
        + "|big|small|sub|sup|em|strong|dfn|code|samp|kbd|var|cite"
        + "|abbr|acronym|span|blink|nobr|wbr"
        + "|layer|nolayer|ilayer|map|label|input|select|textarea"
        + "|legend|button|basefont|font|bdo|spacer"
        + "|embed|noembed|script|noscript|ins|del|";
    
// 20010128: browser whitespace bug
    // html tags that should generate a line break only if preceded
    // by whitespace (these must also be in defaultInlineTags)
    private static final String defaultOptionalInlineTags_ = 
        "|input|select|textarea|";

// 20010128: browser whitespace bug
    // html block tags that must not have white space before their
    // end tag
    private static final String defaultHuggingEndTags_ = 
        "|td|th|textarea|";

    // html attributes that do not have values
    private static final String defaultFlagAttributes_ = 
          "|selected|checked|border|compact|declare|disabled|ismap"
        + "|multiple|nohref|noresize|noshade|nowrap|readonly"
        + "|showgrid|showgridx|showgridy|usemap|";

    public static
    void
    setOptions(
        Options options
        )
    {
        options.defaultTagsWithoutEnd         = defaultTagsWithoutEnd_;
        options.defaultInclusiveIterationTags = defaultInclusiveIterationTags_;
        options.defaultExclusiveIterationTags = defaultExclusiveIterationTags_;
        options.defaultPreformattedTags       = defaultPreformattedTags_;
        options.defaultFlagAttributes         = defaultFlagAttributes_;
        options.defaultInlineTags             = defaultInlineTags_;
        options.defaultOptionalInlineTags     = defaultOptionalInlineTags_;
        options.defaultHuggingEndTags         = defaultHuggingEndTags_;
    }
        
    protected static
    String
    removeSuffix(
        String filename
        )
    {
        String result = filename;
        int p = result.lastIndexOf('.');
        if ( p != -1 )
        {
            result = result.substring(0,p);
        }
        return result;
    }
    
    /**
        Convert an HTML file to XML.

        @returns
            XML file name.
    **/
    public static
    File
    convertToXml(
        File htmlFile,
        PrintStream errorStream,
        boolean alwaysConvert
        )
    throws IOException, FatalError
    {
        return convertToXml(htmlFile, errorStream, alwaysConvert, null);
    }
    
    /**
        Convert an HTML file to XML.

        @returns
            XML file name.
    **/
    public static
    File
    convertToXml(
        File htmlFile,
        PrintStream errorStream,
        boolean alwaysConvert,
        String encoding
        )
    throws IOException, FatalError
    {
        //!TODO: allow work directory to be specified as an option
        // so that user doesn't require write access to HTML directory
        String htmlFilename = htmlFile.toString();
        String outputFilename = removeSuffix(htmlFilename) + ".asxml";
        File outputFile = new File(outputFilename);

        if ( ! alwaysConvert && outputFile.exists() )
        {
            // don't bother converting to XML if the output file is
            // already there
            if ( htmlFile.lastModified() < outputFile.lastModified() )
            {
                return outputFile;
            }
        }

        if ( encoding != null )
        {
            HtmlToXml.instance.convert(
                htmlFilename, outputFilename, errorStream, encoding);
        }
        else
        {
            HtmlToXml.instance.convert(
                htmlFilename, outputFilename, errorStream);
        }

        return outputFile;
    }
}
