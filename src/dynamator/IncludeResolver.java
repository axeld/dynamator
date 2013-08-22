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
import java.io.FileNotFoundException;
import java.io.IOException;

/**
    Helper class to resolve include files.
**/
public
class IncludeResolver
implements Cloneable
{
//    private static final String rcsID_ = 
//        "$Id: IncludeResolver.java,v 1.4 2004/03/28 21:11:33 jaydunning Exp $";

    private File[] includeDirectories_ = {};

    /**
        Construct an include resolver.
    **/
    public
    IncludeResolver()
    {}
    
    public
    Object
    clone()
    {
        try
        {
            IncludeResolver result = (IncludeResolver) super.clone();
            result.includeDirectories_ = (File[]) includeDirectories_.clone();
            return result;
        }
        catch(CloneNotSupportedException x)
        {
            throw new FatalError(x);
        }
    }
    
    /**
        Add a directory to the include path.
        The directory will be searched last.

        @throws IOException
            If the directory is not found or cannot be read
    **/
    public
    void
    addDirectory(
        String dir
        )
    throws IOException
    {
        addDirectory(new File(dir));
    }
    
    /**
        Add a directory to the include path.
        The directory will be searched last.

        @throws IOException
            If the directory is not found or cannot be read
    **/
    public
    void
    addDirectory(
        File dir
        )
    throws IOException
    {
        if ( ! dir.exists() )
        {
            throw new FileNotFoundException(dir.getAbsolutePath());
        }

        synchronized(includeDirectories_)
        {
            File[] newArray = new File[1 + includeDirectories_.length];
            System.arraycopy(includeDirectories_, 0, 
                newArray, 1, includeDirectories_.length);
            newArray[0] = dir;
            includeDirectories_ = newArray;
        }
    }
    
    /**
        Determine the specific file to be included, given an
        include file name.  
        If the requested file name is absolute, it is used as-is.
        If the requested file name is relative, the include directories
        are searched in precedence order (from last to first).  If 
        the file is not found in an include directory, and a default
        directory is specified, the default directory is searched.

        @param filename
            Relative or absolute name of the file to be included.
        @param defaultDirectory
            Optional directory to be searched if includeDirectories do
            not contain included files.

        @return
            null if file not found.
    **/
    public
    File
    resolve(
        String filename
        )
    {
        return resolve(filename, null);
    }
    
    /**
        Determine the specific file to be included, given an
        include file name.  
        If the requested file name is absolute, it is used as-is.
        If the requested file name is relative, the include directories
        are searched in precedence order (from last to first).  If 
        the file is not found in an include directory, and a default
        directory is specified, the default directory is searched.

        @param filename
            Relative or absolute name of the file to be included.
        @param defaultDirectory
            Optional directory to be searched if includeDirectories do
            not contain included files.

        @return
            null if file not found.
    **/
    public
    File
    resolve(
        String filename,
        File defaultDirectory
        )
    {
        File result;

        if ( Utils.filenameIsAbsolute(filename) )
        {
            result = new File(filename);
            return result.exists()
                ? result
                : null;
        }

        int i = includeDirectories_.length;
        while ( --i >= 0 )
        {
            result = new File(includeDirectories_[i], filename);
            if ( result.exists() )
            {
                return result;
            }
        }

        if ( defaultDirectory != null )
        {
            result = new File(defaultDirectory, filename);
            return result.exists()
                ? result
                : null;
        }
        
        return null;
    }

    public
    String
    toString()
    {
        StringBuffer buf = new StringBuffer();
        int i = includeDirectories_.length;
        while ( --i >= 0 )
        {
            buf.append(',');
            buf.append(String.valueOf(includeDirectories_[i]));
        }
        buf.setCharAt(0, '[');
        buf.append(']');
        return buf.toString();
    }
}
