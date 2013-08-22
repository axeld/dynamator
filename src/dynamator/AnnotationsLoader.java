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

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

/**
    Loads the DOM for a Dynamator (annotations) file.
    <p>
    An AnnotationsLoader is created for each root annotations file
    loaded. It is used to load both the root file and any included
    files. 
**/
public
class AnnotationsLoader
{
//    private static final String rcsID_ = 
//        "$Id: AnnotationsLoader.java,v 1.1 2004/03/21 22:20:49 jaydunning Exp $";

    private static final DocumentBuilderFactory builderFactory_ =
        initializeDocumentBuilderFactory();

    private static
    DocumentBuilderFactory
    initializeDocumentBuilderFactory()
    {
        try
        {
            return DocumentBuilderFactory.newInstance();
        }
        catch(FactoryConfigurationError x)
        {
            throw new FatalError(
                "XML parser could not be configured"
                + x.toString());
        }
    }
        
    private IncludeResolver includeResolver_;
    private ErrorHandler errorHandler_;
    private DocumentBuilder builder_;
    private boolean validate_;

    /**
        Create an AnnotationsLoader.
        <p>
        Even though Options contains an IncludeResolver, it is obtained
        independently so that the directory containing the root
        document can be added.
    **/
    public
    AnnotationsLoader(
        Options options,
        IncludeResolver includeResolver
        )
    {
        includeResolver_ = includeResolver;
        validate_ = options.validate;

        builder_ = null;
        try
        {
            synchronized( builderFactory_ )
            {
                builder_ = builderFactory_.newDocumentBuilder();
            }
        }
        catch(ParserConfigurationException x)
        {
            throw new FatalError(
                "Could not configure Dynamator file parser: "
                + x.toString());
        }

        errorHandler_ = new ErrorHandler(options.errorStream);
        builder_.setErrorHandler(errorHandler_);
    }
    
    /**
        Create an InputStream for a file.
    **/
    private static
    InputStream
    inputStreamFor(
        File file
        )
    throws IOException
    {
        return new AnnotationsFileInputStream.Default(file);
    }

    /**
        Load a Dynamator file as a DOM document, validating it unless
        the options specify otherwise. To determine if errors were 
        encountered during loading or validation, call 
        {@link #foundErrors foundErrors}.
    **/
    public
    Document
    load(
        File file
        )
    throws IOException
    {
        return load(file, file.getCanonicalPath());
    }

    /**
        Load a Dynamator file as a DOM document, validating it unless
        the options specify otherwise. To determine if errors were 
        encountered during loading or validation, call 
        {@link #foundErrors foundErrors}.
        <p>
        This is identical to the simpler form, but allows the 
        canonical path to be supplied for performance.
    **/
    public
    Document
    load(
        File file,
        String canonicalPath
        )
    throws IOException
    {
        Document result = ElementUtils.parse(
            inputStreamFor(file), canonicalPath, builder_);

        if ( validate_ && ! foundErrors() )
        {
            XMLValidator.validate(
                result.getDocumentElement(), errorHandler_);
        }

        return result;
    }

    /**
        Whether any errors were found on any document loaded through 
        this loader.
    **/
    public
    boolean
    foundErrors()
    {
        return errorHandler_.foundErrors();
    }

    /**
        The error handler for this loader.
    **/
    public
    ErrorHandler
    errorHandler()
    {
        return errorHandler_;
    }
    
    /**
        Resolve an included file
    **/
    public
    File
    resolveInclude(
        String filename
        )
    throws IOException
    {
        return includeResolver_.resolve(filename);
    }
}

