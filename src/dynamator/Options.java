/***********************************************************************
*   Copyright 2000-2004 by Jay Dunning.
*   All rights reserved.
*
*   Redistribution and use in source and binary forms, with or without
*   modification, are permitted provided that the following conditions 
*   are met:
*
*   1.  Redistributions of source code must retain the above copyright
*       Comment, this list of conditions, and the following disclaimer.
*
*   2.  Redistributions in binary form must reproduce the above
*       copyright Comment, this list of conditions, and the following
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
import java.io.IOException;
import java.io.PrintStream;

/**
    Execution options container. 
    Contents controlled by program arguments or by output language
    adapter. 
**/
public
class Options
implements Cloneable
{
//    private static final String rcsID_ = 
//        "$Id: Options.java,v 1.12 2004/03/30 06:04:57 jaydunning Exp $";

    public static
    class TemplateType
    {
        private TemplateType() {};
        public static final TemplateType HTML = new TemplateType();
        public static final TemplateType XML  = new TemplateType();
    }
    
    // Controlled by program arguments
    public TemplateType coerceTemplateTypeTo = null;
    public String  destDirectory = null;
    public String  altDirectory = null;
    public String  templateDirectory = null;
    public String  traceFile = null;
    public String  encoding = "ASCII";
    public String  annotationsName = null;
    public boolean doTrace = false;
    public boolean validate = true;
    public boolean bodyOnlyHtml = false;
    public boolean stripComments = false;
    public boolean alwaysConvertHtmlToXml = false;
    public boolean produceGenerationComment = true;
    // in reverse order of priority
    public IncludeResolver includeResolver = new IncludeResolver();

    public String  indentString = "  ";
    public Indentation indentation = new Indentation(indentString);

    // Controlled by annotations file
    public String defaultTagsWithoutEnd = "";
    public String defaultInclusiveIterationTags = "";    // not used
    public String defaultExclusiveIterationTags = "";
    public String defaultPreformattedTags = "";
    public String defaultInlineTags = "";
    public String defaultOptionalInlineTags = "";
    public String defaultHuggingEndTags = "";
    public String defaultFlagAttributes = "";
    
        // really only for language="none" 
        // and only for generation comment, 
        // but probably should be language-specific and apply
        // to any comment
    public String commentStart = "<!--";
    public String commentEnd   = "-->";

    // Controlled programmatically
    public PrintStream errorStream = System.err;
    
    // Populated by Command.
    // Used between invocations of Command.process
    // Type is 'Object' in order to prevent cyclic dependency between 
    // this class and Annotations. If command ends up having any more
    // values cached here, this should be turned into an opaque
    // command-options object.
    public Object annotations = null;
    public File annotationsFile = null;

    public 
    Object 
    clone()
    {
        Options result;
        
        try
        {
            result = (Options) super.clone();
        }
        catch(CloneNotSupportedException x)
        {
            throw new FatalError(
                "Options could not be cloned: " + x.toString());
        }

        result.indentation = new Indentation(indentString);
        return result;
    }
    
    public
    boolean
    handleArgument(
        String arg,
        ArgumentParser parser
        )
    {
        boolean result = true;

        if ( arg.charAt(0) == '-' )
        {
            switch( arg.charAt(1) )
            {
                case 'a':
                {
                    alwaysConvertHtmlToXml = true;
                }
    
                break;
                case 'B':
                {
                    bodyOnlyHtml = true;
                }
    
                break;
                case 'C':
                {
                    stripComments = true;
                }
    
                break;
                case 'd':
                {
                    destDirectory = parser.flagArgumentValue();
                }
    
                break;
                case 'e':
                {
                    encoding = parser.flagArgumentValue();
                }
    
                break;
                case 'f':
                {
                    String prev = altDirectory;
                    altDirectory = parser.flagArgumentValue();
                    if ( ! altDirectory.equals(prev) )
                    {
                        annotationsName = null;
                        annotations = null;
                        annotationsFile = null;
                    }
                }
    
                break;
                case 'F':
                {
                    String prev = annotationsName;
                    annotationsName = parser.flagArgumentValue();
                    if ( ! annotationsName.equals(prev) )
                    {
                        annotations = null;
                        annotationsFile = null;
                    }
                }
    
                break;
                case 'G':
                {
                    produceGenerationComment = false;
                }
    
                break;
                case 'H':
                {
                    bodyOnlyHtml = false;
                    coerceTemplateTypeTo = 
                        Options.TemplateType.HTML;
                }
    
                break;
                case 'I':
                {
                    String dir = parser.flagArgumentValue();
                    if ( dir == null )
                    {
                        // legacy: -N used to be -I
                        indentString = "";
//                        indentString = null;
                        indentation.set(indentString);
                        errorStream.println(
                            "Warning: -I without argument.  "
                            + "Assumed you meant -N");
                    }
                    else
                    {
                        if ( ! dir.endsWith("/")
                            && ! dir.endsWith("\\") )
                        {
                            dir += "/";
                        }

                        try
                        {
                            includeResolver.addDirectory(dir);
                        }
                        catch(IOException x)
                        {
                            errorStream.println(
                                "Warning: " 
                                + x.toString()
                                );
                        }
                    }
                }
    
                break;
                case 'N':
                {
                    // see also -I
                    indentString = "";
//                    indentString = null;
                    indentation.set(indentString);
                }
    
                break;
                case 't':
                {
                    templateDirectory = parser.flagArgumentValue();
                }

                break;
                case 'T':
                {
                    Trace.on = doTrace = true;
                    if ( arg.length() > 2 )
                    {
                        try
                        {
                            traceFile = arg.substring(2);
                            Trace.open(traceFile);
                        }
                        catch(IOException x)
                        {
                            throw new FatalError(x);
                        }
                    }
                }

                break;
                case 'v':
                {
                    errorStream.println(
                        "Dynamator version " + Version.version()
                        + ", build " + Version.build());
                    errorStream.println(Version.copyright());
                }
    
                break;
                case 'V':
                {
                    validate = false;
                }
    
                break;
                case 'X':
                {
                    bodyOnlyHtml = false;
                    coerceTemplateTypeTo = 
                        Options.TemplateType.XML;
                }
    
                break;
                default:
                {
                    errorStream.println(
                        "Unrecognized argument: '" + arg + "'");
                    result = false;
                }
            }
        }
        else
        {
            result = false;
        }

        return result;
    }
    
    public
    void
    printOptions()
    {
        errorStream.print(""
          + "  [-a]       -- always convert HTML to XML\n"
          + "  [-B]       -- only output the content of the HTML body\n"
          + "  [-C]       -- remove all comments from template\n"
          + "  [-d dir]   -- place output in [dir]\n"
          + "  [-e enc]   -- use Java encoding 'enc'\n"
          + "  [-f dir]   -- obtain Dynamator files from [dir]\n"
          + "  [-F file]  -- apply Dynamator [file] to following templates\n"
          + "  [-G]       -- don't output generation comment\n"
          + "  [-H]       -- treat following files as HTML\n"
          + "  [-I dir]*  -- look in [dir] for includes\n"
          + "                (default and last resort is .dyn dir)\n"
          + "  [-N]       -- don't indent output\n"
          + "  [-t dir]   -- look for templates in dir\n"
          + "  [-T{file}] -- trace execution to {file} or STDERR\n"
          + "  [-v]       -- display dynamator version\n"
          + "  [-V]       -- don't validate Dynamator files\n"
          + "  [-X]       -- treat following files as XML\n"
          );
    }

    public
    String
    toString()
    {
        return 
            "destDirectory=" + destDirectory 
            + ','
            + "altDirectory=" + altDirectory 
            + ','
            + "templateDirectory=" + templateDirectory 
            + ','
            + "encoding=" + encoding
            + ','
            + "traceFile=" + traceFile
            + ','
            + "doTrace=" + doTrace
            + ','
            + "bodyOnlyHtml=" + bodyOnlyHtml
            + ','
            + "indentString='" + indentString + "'"
            + ','
            + "stripComments=" + stripComments
            + ','
            + "includeDirectories=" + includeResolver.toString()
            + ','
            + "alwaysConvertHtmlToXml=" + alwaysConvertHtmlToXml
            + ','
            + "produceGenerationComment=" + produceGenerationComment
            ;
    }

    protected
    String
    toString(
        File[] files
        )
    {
        StringBuffer buf = new StringBuffer();
        int i = files.length;
        while ( --i >= 0 )
        {
            buf.append(',');
            buf.append(String.valueOf(files[i]));
        }
        buf.setCharAt(0, '[');
        buf.append(']');
        return buf.toString();
    }
}
