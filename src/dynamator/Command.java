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
import java.io.FileOutputStream;
import java.io.IOException;

/**
    Highest-level command entry.  Dynamically determines whether to
    invoke HTML or XML versions of Dynamator.
**/
public
class Command
{
//    private static final String rcsID_ = 
//        "$Id: Command.java,v 1.10 2004/03/30 06:04:45 jaydunning Exp $";

    private static
    String
    suffix(
        File file
        )
    {
        String filename = file.toString();
        int p = filename.lastIndexOf('.');
        if ( p > 0 )
        {
            return filename.substring(p+1);
        }
        return "";
    }
    
    private static
    String
    normalizeDirectoryPrefix(
        String prefix
        )
    {
        if ( prefix.length() > 0  
            && ! prefix.endsWith("/") 
            && ! prefix.endsWith("\\") )
        {
            prefix += "/";
        }
        return prefix.replace('\\','/');
    }
    
    /**
        Processes a single file.
        
        @param options
            The current options object.  (Reflects the options
            specified before this filename was encountered on the
            command line.)

        @param filename
            The file to be processed.  This may be either an
            annotations file or a template file.  The normal case is to
            specify a template file; file 'x.y' is assumed to have
            corresponding annotations file 'x.dyn'.  
            File 'x.dyn' is assumed to specify the name of the
            corresponding template file through the dynamator tag
            attribute named "template".
    **/
    public static
    void
    process(
        Options options,
        String filename
        )
    throws IOException
    {
        options = (Options) options.clone();
        
        filename = filename.replace('\\','/');
        File argumentFile = new File(filename);
        if ( ! argumentFile.exists() )
        {
            throw new FatalError(
                "File not found: " + argumentFile.getAbsolutePath()
                );
        }
    
        String basename = Utils.basename(filename);
        String altDirectory = normalizeDirectoryPrefix(
            ( options.altDirectory == null )
            ? System.getProperty("user.dir")
            : options.altDirectory
            );
        String destDirectory = normalizeDirectoryPrefix(
            ( options.destDirectory == null )
            ? System.getProperty("user.dir")
            : options.destDirectory
            );

        /*  20010505: Simplification of target determination
            
            Previously, target language and target file suffix were
            determined by file suffix of annotations file.
            DynamatorSuffixes.properties contained a list of supported
            suffixes and corresponding factories.  This would ultimately
            have become too confusing, and chances for filetype conflict
            would have increased as suffixes increased.
            
            Now a single suffix is employed for annotations files
            (.dyn), and the target language and suffix are encoded
            within the annotations file.  This makes it easier to
            find files, and more flexible for users.  It also moves the
            interface closer to supporting something like a macro
            language for definition of input/output file properties.
            The downside is that a user can't automatically generate to
            multiple target languages by having multiple annotations
            filetypes in the same directory, but that's not an
            important feature.

            A major impact of this change is that parsing of the
            annotations file now has to be done before the target is
            determined.  This means that the file has to be opened
            twice; the first time just to determine the target
            language; the second time by the DOM parser.  For my
            convenience, target determination is performed by a simple
            scan: I look for "<dynamator" then check the presence of
            the next two attributes. 
            
            I still haven't figured out the best way to enable third-party
            plugins.   I think it's simpler now, though.  A third-party
            can create a package 'dynamator.xxx' that can be installed
            in the classpath.

            20040217:
            Chris Felaco's patch 896127 caused me to realize that
            annotations file parsing has evolved to the point where
            its no longer language-dependent, and I can't think of 
            any reason it should be.  Therefore the redundant parsing
            has been removed; the language and suffix are obtained from
            the Annotations object.  If the need should arise for
            language-dependent Annotations objects, the patch has a
            better way to do that than my previous implementation.
        */
            
        File annotationsFile;
        
        if ( argumentFile.toString().endsWith(".dyn") )
        {
            annotationsFile = argumentFile;
        }
        else
        if ( options.annotationsName != null )
        {
            annotationsFile = options.annotationsFile;
            if ( annotationsFile == null )
            {
                annotationsFile =
                    ( Utils.filenameIsAbsolute(options.annotationsName) )
                    ? new File(options.annotationsName)
                    : new File(altDirectory + options.annotationsName);
                if ( ! annotationsFile.exists() )
                {
                    throw new FatalError(
                        "Dynamator file not found: " 
                        + annotationsFile.getAbsolutePath());
                }
            }
        }
        else
        {
            annotationsFile = new File(
                altDirectory + basename + ".dyn");

            if ( ! annotationsFile.exists() )
            {
                throw new FatalError(
                    "Dynamator file not found: " 
                    + annotationsFile.getAbsolutePath());
            }
        }

        if ( options.annotationsFile == null )
        {
            annotationsFile = 
                new File(annotationsFile.getCanonicalPath());

            if ( options.annotationsName != null )
            {
                options.annotationsFile = annotationsFile;
            }
        }
        
        Annotations annotations = 
            options.annotations != null
            ? (Annotations) options.annotations
            : new Annotations(annotationsFile, options);
            
        if ( options.annotationsName != null 
            && options.annotations == null )
        {
            options.annotations = annotations;
        }

        String suffix = annotations.getSuffix();
        String template = annotations.getTemplate();
        String outputFileName = annotations.getFilename();
        String language = annotations.getLanguage();

        /*
                    has value?          result    result
            language  suffix  filename  language  filename
            --------  ------  --------  --------  --------
            N         N       N         none      argbasename.templatesuffix
            N         N       Y         none      filename
            N         Y       N         none      basename.suffix
            N         Y       Y         none      filename
            Y         N       N         language  basename.language
            Y         N       Y         language  filename
            Y         Y       N         language  basename.suffix
            Y         Y       Y         language  filename

        */
        
        File templateFile;
        
        if ( argumentFile.toString().endsWith(".dyn") )
        {
            if ( template == null || template.length() == 0 )
            {
                throw new FatalError(
                    "dynamator tag must specify template attribute "
                    + " when argument file suffix is .dyn");
            }

            String templateDirectory = options.templateDirectory;
            if ( templateDirectory == null )
            {
                templateDirectory = altDirectory;
            }
            else
            {
                templateDirectory = 
                    normalizeDirectoryPrefix(templateDirectory);
            }
            templateFile = new File(templateDirectory + template);
            if ( ! templateFile.exists() )
            {
                throw new FatalError(
                    "Template file not found: " 
                    + templateFile.getAbsolutePath());
            }
        }
        else
        {
            templateFile = argumentFile;
        }

        String templateSuffix = suffix(templateFile);
        
        if ( outputFileName != null && outputFileName.length() > 0 )
        {
            if ( suffix != null && suffix.length() > 0 )
            {
                options.errorStream.println(
                    "Warning: suffix and filename both specified; "
                    + "ignoring suffix");
            }
        }
        else
        {
            if ( suffix == null || suffix.length() == 0 )
            {
                if ( "none".equals(language) )
                {
                    suffix = templateSuffix;
                }
                else
                if ( language.indexOf('.') >= 0 )
                {
                    suffix = language.substring(0, language.indexOf('.'));
                }
                else
                {
                    suffix = language;
                }
            }

            outputFileName = destDirectory + basename + '.' + suffix;
        }

        File outputFile = new File(outputFileName);
        String outputFileCanonical = outputFile.getCanonicalPath();
        if ( outputFileCanonical.equals(
            templateFile.getCanonicalPath()) )
        {
            throw new FatalError(
                "Output file would overwrite template file");
        }
        if ( outputFileCanonical.equals(
            annotationsFile.getPath()) )
        {
            throw new FatalError(
                "Output file would overwrite Dynamator file");
        }

        Options.TemplateType templateType =
            options.coerceTemplateTypeTo;
        
        if ( templateType == null )
        {
            if ( "html".equalsIgnoreCase(templateSuffix)
                || "htm".equalsIgnoreCase(templateSuffix) )
            {
                templateType = Options.TemplateType.HTML;
            }
        }
        
        if ( templateType == Options.TemplateType.HTML 
            || options.bodyOnlyHtml )
        {
            Html.setOptions(options);
            if ( templateType != Options.TemplateType.XML
                && ! "xhtml".equalsIgnoreCase(templateSuffix) )
            {
                templateFile = 
                    Html.convertToXml(
                        templateFile, 
                        options.errorStream,
                        options.alwaysConvertHtmlToXml,
                        options.encoding
                        );
            }
        }
        else
        if ( templateType == null
            && "xhtml".equalsIgnoreCase(templateSuffix) )
        {
            Html.setOptions(options);
        }
        else
        if ( templateType == Options.TemplateType.XML
            && ( "html".equalsIgnoreCase(templateSuffix)
                || "htm".equalsIgnoreCase(templateSuffix) ) )
        {
            Html.setOptions(options);
        }

        Generator generator = Generator.makeGenerator(
            language,
            new FileOutputStream(outputFile), 
            outputFileName,
            options
            );
        DynamateXml.process(
            options, templateFile, annotations, generator);
    }
}
