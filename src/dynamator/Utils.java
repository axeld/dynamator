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
                    
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
    Utility functions.
**/
public 
class Utils
{
//    private static final String rcsID_ = 
//        "$Id: Utils.java,v 1.9 2004/03/28 21:13:10 jaydunning Exp $";

    public static
    boolean
    onlyWhitespace(
        char[] buf,
        int offset, 
        int len
        ) 
    {
        int i = offset + len;
        while ( --i >= offset )
        {
            if ( ! Character.isWhitespace(buf[i]) )
            {
                return false;
            }
        }
        return true;
    }        
    
    public static
    boolean
    onlyWhitespace(
        String s
        )
    {
        int i = s.length();
        while ( --i >= 0 )
        {
            if ( ! Character.isWhitespace(s.charAt(i)) )
            {
                return false;
            }
        }
        return true;
    }
    
    public static
    boolean
    contains(
        char[] buf,
        int offset, 
        int len,
        char sought
        )
    {
        int i = offset + len;
        while ( --i >= offset )
        {
            if ( buf[i] == sought )
            {
                return true;
            }
        }
        return false;
    }
    
    public static
    boolean
    containsWord(
        String s,
        String sought
        )
    {
        int p = -1;
        boolean startIsBoundary = false;
        boolean endIsBoundary = false;

        while ( ! startIsBoundary && ! endIsBoundary
            && 0 <= (p = s.indexOf(sought, p+1)) )
        {
            startIsBoundary = 
                ( p == 0 
                || Character.isWhitespace(s.charAt(p-1)) );
            endIsBoundary = 
                ( p + sought.length() == s.length()
                || Character.isWhitespace(s.charAt(p + sought.length())) );
        }

        return startIsBoundary && endIsBoundary;
    }
    
    public static
    String
    replace(
        String source,
        String from,
        String to
        )
    {
        // another method that belongs in String

        if ( source.indexOf(from) < 0 )
        {
            return source;
        }
        
        StringBuffer buf = new StringBuffer(source.length());
        int p;
        int len = from.length();
        int start = 0;
        while ( (p = source.indexOf(from, start)) >= 0 )
        {
            buf.append(source.substring(start, p));
            buf.append(to);
            start = p + len;
        }
        buf.append(source.substring(start));
        return buf.toString();
    }

    /**
        First word in a string.
        
        @return
            empty string if no word found (s is null, empty, or
            contains only whitespace);
            otherwise a string consisting of the first sequence of
            non-whitespace characters in s. 
    **/
    public static
    String
    firstWord(
        String s
        )
    {
        if ( s == null || s.length() == 0 )
        {
            return "";
        }
        int pStart = -1;
        int lim = s.length();
        while ( ++pStart < lim 
            && Character.isWhitespace(s.charAt(pStart)) )
        {}

        if ( pStart == s.length() )
        {
            return "";
        }
        
        int pEnd = pStart;
        while ( ++pEnd < lim 
            && ! Character.isWhitespace(s.charAt(pEnd)) )
        {}

        return s.substring(pStart, pEnd);
    }
    
    /**
        First word in a character array.
        
        @param buf
            Must not be null.
        @param offset
            <code>0 &lt;= offset &lt; buf.length</code>
        @param len
            <code>offset + len &lt;= buf.length</code>

        @return
            empty string if no word found (len == 0 or 
            buf[offset..(offset+length-1)] contains only whitespace)
            otherwise first sequence of non-whitespace characters in 
            buf[offset..(offset+length-1)].
    **/
    public static
    String
    firstWord(
        char[] buf,
        int offset,
        int len
        )
    {
        int lim = offset + len;

        int pStart = offset - 1;
        while ( ++pStart < lim 
            && Character.isWhitespace(buf[pStart]) )
        {}

        if ( pStart == lim )
        {
            return "";
        }
        
        int pEnd = pStart;
        while ( ++pEnd < lim 
            && ! Character.isWhitespace(buf[pEnd]) )
        {}

        return new String(buf, pStart, pEnd-pStart);
    }
    
    public static final
    String
    nullIfEmpty(
        String value
        )
    {
        return defaultIfEmpty(value, null);
    }

    public static final
    String
    defaultIfEmpty(
        String value,
        String defaultValue
        )
    {
        return ( value != null 
                && value.length() > 0 
                && ! onlyWhitespace(value) )
            ? value
            : defaultValue;
    }
    
    public static
    String
    pathTo(
        String filename
        )
    {
        String result = "";
        int p = filename.lastIndexOf('/');
        if ( p >= 0 )
        {
            result = filename.substring(0, p);
        }
        return result;
    }

    public static
    String
    basename(
        String filename
        )
    {
        String result = filename;
        int p = result.lastIndexOf('/');
        if ( p >= 0 )
        {
            result = result.substring(p+1);
        }
        p = result.lastIndexOf('.');
        if ( p >= 0 )
        {
            result = result.substring(0,p);
        }
        return result;
    }

    public static
    String[]
    join(
        String[] a1,
        String[] a2
        )
    {
        String[] result = new String[a1.length + a2.length];
        System.arraycopy(a1, 0, result, 0, a1.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }
    
    /**
        Sort a string array in place using Quicksort with 
        natural ordering.
    **/
    public static
    void
    sort(
        String[]    data
        )
    {
        if ( data.length > 0 )
        {
            sort(data, 0, data.length-1);
        }
    }

    /**
        Sort a portion of a string array in place using Quicksort
        with natural ordering.
    **/
    public static
    void
    sort(
        String[]    data,
        int         left,
        int         right
        )
    {
        // This could have been implemented using the 
        // client-supplied ordering version, but it would 
        // have been a little less efficient.
        String mid, tmp;
        int original_left = left;
        int original_right = right;
        mid = data[(left+right)/2];
        do
        {
            while ( data[left].compareTo(mid) < 0 )
            {
                ++left;
            }
            while ( mid.compareTo(data[right]) < 0 )
            {
                --right;
            }
            if ( left <= right )
            {
                tmp = data[left];
                data[left]  = data[right];
                data[right] = tmp;
                ++left;
                --right;
            }
        } while ( left <= right );

        if ( original_left < right )
        {
            sort(data, original_left, right);
        }
        if ( left < original_right )
        {
            sort(data, left, original_right);
        }
    }

    public
    interface StringCompare
    {
        boolean 
        lessThan(
            String x,
            String y
            );
    }
    
    /**
        Sort a string array in place using Quicksort with 
        client-supplied ordering.
    **/
    public static
    void
    sort(
        String[]      data,
        StringCompare compare
        )
    {
        if ( data.length > 0 )
        {
            sort(data, 0, data.length-1, compare);
        }
    }

    /**
        Sort a portion of a string array in place using Quicksort
        with client-supplied ordering.
    **/
    public static
    void
    sort(
        String[]      data,
        int           left,
        int           right,
        StringCompare compare
        )
    {
        String mid, tmp;
        int original_left = left;
        int original_right = right;
        mid = data[(left+right)/2];
        do
        {
            while ( compare.lessThan(data[left],mid) )
            {
                ++left;
            }
            while ( compare.lessThan(mid, data[right]) )
            {
                --right;
            }
            if ( left <= right )
            {
                tmp = data[left];
                data[left]  = data[right];
                data[right] = tmp;
                ++left;
                --right;
            }
        } while ( left <= right );

        if ( original_left < right )
        {
            sort(data, original_left, right, compare);
        }
        if ( left < original_right )
        {
            sort(data, left, original_right, compare);
        }
    }

    public static
    boolean                 // true if 'sought' is in 'array'
    binarySearch(
        String[] array,     // must be sorted in ascending order
        String sought
        )
    {
        int first = 0;
        int len = array.length;
        int half, middle;
        int compare;

        while ( len > 0 )
        {
            half = len/2;
            middle = first + half;
            compare = array[middle].compareTo(sought);
            if ( compare < 0 )
            {
                first = middle + 1;
                len -= half + 1;
            }
            else
            if ( compare > 0 )
            {
                len = half;
            }
            else
            {
                return true;
            }
        }
        return false;
    }

    public static
    String
    capitalizeFirstCharacter(
        String s
        )
    {
        if ( s != null )
        {
            if ( s.length() > 1 )
            {
                s = Character.toUpperCase(s.charAt(0))
                    + s.substring(1);
            }
            else
            {
                s = s.toUpperCase();
            }
        }
        return s;
    }

    // Wasn't in JDK 1.1
    public static
    String
    substring(
        StringBuffer buf,
        int start,
        int lim
        )
    {
        int len = lim - start;
        char[] result = new char[len];
        buf.getChars(start, lim, result, 0);
        return String.valueOf(result);
    }

    public static
    boolean
    endsWith(
        StringBuffer buf,
        String s
        )
    {
        int iBuf = buf.length();
        int iS   = s.length();
        
        while ( --iS >= 0 && --iBuf >= 0
            && buf.charAt(iBuf) == s.charAt(iS) )
        {}

        return iS < 0;
    }

    public static
    int
    indexOf(
        StringBuffer buf,
        char c
        )
    {
        return indexOf(buf, c, 0);
    }

    public static
    int
    indexOf(
        StringBuffer buf,
        char c,
        int offset
        )
    {
        int i = offset - 1;
        int lim = buf.length();

        while ( ++i < lim )
        {
            if ( buf.charAt(i) == c )
            {
                return i;
            }
        }

        return -1;
    }
    
    public static
    int
    indexOf(
        StringBuffer buf,
        String s
        )
    {
        return indexOf(buf, s, 0);
    }

    public static
    int
    indexOf(
        StringBuffer buf,
        String s,
        int offset
        )
    {
        int i = offset - 1;
        int iS, iO;

        if ( s.length() == 0 )
        {
            return offset;
        }

        char firstChar = s.charAt(0);
        int sLength = s.length();
        int lim = buf.length() - sLength + 1;

nextI:        
        while ( ++i < lim )
        {
            if ( buf.charAt(i) == firstChar )
            {
                iS = sLength;
                iO = i + sLength;
                while ( --iS >= 0 )
                {
                    if ( s.charAt(iS) != buf.charAt(--iO) )
                    {
                        continue nextI;
                    }
                }
                return i;
            }
        }

        return -1;
    }
    
    public static
    int
    indexOf(
        char[] buf, 
        int offset, 
        int len,
        char c
        )
    {
        int i = offset - 1;
        int lim = offset + len;

        while ( ++i < lim )
        {
            if ( buf[i] == c )
            {
                return i;
            }
        }

        return -1;
    }
    
    /**
        Convert attribute text to a dictionary containing
        name/attribute values. 
    **/
    public static
    Dictionary
    parseAttributes(
        String buf
        )
    {
        Dictionary result = new Hashtable();
        
        String token = "";
        String name = null;
        String value;
        StringTokenizer tokenizer = 
            new StringTokenizer(buf, "= \n\t", true);

        while ( tokenizer.hasMoreTokens() )
        {
            if ( token.length() > 0 )
            {
                name = token;
            }
            token = tokenizer.nextToken().trim();
            if ( token.equals("=") )
            {
                value = "";
                while ( value.length() == 0 
                        && tokenizer.hasMoreTokens() )
                {
                    value = tokenizer.nextToken().trim();
                }
    
                if ( ( value.startsWith("\"")
                       && value.endsWith("\"") )
                     || 
                     ( value.startsWith("'")
                       && value.endsWith("'") ) )
                {
                    value = 
                        value.substring(1, value.length()-1);
                }
//                else
//                {
//                    throw new FatalError(
//                        "ill-formed tag attribute: "
//                        + name + token + value
//                        );
//                }
                result.put(name, value);                                    
            }
        }

        return result;
    }

    public static
    boolean
    filenameIsAbsolute(
        String filename
        )
    {
        return ( filename.startsWith("/")
                || ( filename.length() > 3
                    && filename.charAt(1) == ':'
                    && ( filename.charAt(2) == '/'
                        || filename.charAt(2) == '\\' ) ) );
    }
}
