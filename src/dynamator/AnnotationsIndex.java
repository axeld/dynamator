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
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
    An index to a Dynamator (annotations) file.
    <p>
    An object of this class is created for each annotations file, 
    and holds all the content of the file.  Annotations accesses
    this object to obtain content.
    <p>
    This class was introduced purely for performance reasons.
    <p>
    The previous Annotations design was quite flexible and
    extensible.  
    Annotations was more of a generic container with very little
    knowledge of element roles.   
    Annotations also had hooks that allowed Dynamator files to be
    target-language-specific, but this capability gradually became
    unnecessary.
    Since the language has been relatively stable for several years,
    flexibility and extensibility have become less important than
    performance. 
    <p>
    This design was derived from Patch 896127, Chris Felaco.
**/
public
interface AnnotationsIndex
{
//    private static final String rcsID_ = 
//        "$Id: AnnotationsIndex.java,v 1.4 2004/03/28 21:03:18 jaydunning Exp $";

    /**
        How to obtain an AnnotationsIndex.
        The default AnnotationsIndex is {@link AnnotationsIndexImpl}.
        This may be overridden by specifying the implementation class
        name as the value of system property
        'dynamator.AnnotationsIndex'.
    **/
    class Factory
    {
        private static final Constructor ctor_ = initialize_();
        
        private static
        Constructor
        initialize_()
        {
            Class[] ctorParams = new Class[]
            {
                Document.class,
                String.class,
                AnnotationsLoader.class
            };
            
            String concreteName = System.getProperty(
                "dynamator.AnnotationsIndex", 
                "dynamator.AnnotationsIndexImpl"
                );
            try
            {
                return Class.forName(concreteName)
                    .getConstructor(ctorParams);
            }
            catch(Exception x)
            {
                throw new FatalError(
                    "Could not initialize AnnotationsIndex constructor",
                    x);
            }
        }
            
        /* package */ static
        AnnotationsIndex
        obtain(
            Document document,
            String filename,
            AnnotationsLoader loader
            )
        throws IOException
        {
            Object[] args = 
            {
                document,
                filename,
                loader
            };
            try
            {
                return (AnnotationsIndex) ctor_.newInstance(args);
            }
            catch(InvocationTargetException x)
            {
                if ( x.getTargetException() instanceof IOException )
                {
                    throw (IOException) x.getTargetException();
                }
                throw new FatalError(x.getTargetException());
            }
            catch(ExceptionInInitializerError x)
            {
                if ( x.getException() instanceof IOException )
                {
                    throw (IOException) x.getException();
                }
                throw new FatalError(x.getException());
            }
            catch(Throwable x)
            {
                throw new FatalError(x);
            }
        }
    }

    Factory factory = new Factory();
    
    /**
        Obtain an attribute of the root element.
    **/
    String
    getRootAttribute(
        String name
        );
    
    /**
        Obtain the root element.
    **/
    Element
    getRootElement();
    
    /**
        Obtain the content of each prolog element, in the order
        encountered. 
    **/
    String[]
    getPrologs();

    /**
        Obtain the content of each epilog element, in the order
        encountered. 
    **/
    String[]
    getEpilogs();
    
    /**
        Obtain all class elements for a specified class name.
    **/
    Element[]
    getClassElements(
        String name
        );
        
    /**
        Obtain all id elements for a specified id name.
    **/
    Element[]
    getIdElements(
        String name
        );
        
    /**
        Obtain all tag elements for a specified tag name, 
        including tag elements that apply to any tag name.
    **/
    Element[]
    getTagElements(
        String name
        );
        
    /**
        Obtain the content of a configuration element.
    **/
    String
    getConfigurable(
        String name
        );

    /**
        Output the AnnotationsIndex for debugging.
    **/
    void
    dump(
        PrintWriter writer
        );
}

