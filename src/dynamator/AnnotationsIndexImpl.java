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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
class AnnotationsIndexImpl
implements AnnotationsIndex
{
//    private static final String rcsID_ = 
//        "$Id: AnnotationsIndexImpl.java,v 1.1 2004/03/21 22:20:49 jaydunning Exp $";

    /**
        A cached DOM document.  
        <p>
        All included Dynamator files are cached, since included files
        are usually included more than once and DOM parsing is
        expensive.
        <p>
        Cached files are not indexed, because indexing incorporates
        included files.
        <p>
        Adapted from Patch 896127, Chris Felaco.
    **/
    private static
    class CachedDocument
    {
        private static final Hashtable cache_ = new Hashtable();
        
        private Document document_;
        private long lastModified_;
        
        private
        CachedDocument(
            Document document,
            long lastModified
            )
        {
            document_ = document;
            lastModified_ = lastModified;
        }
    
        private static
        Document
        obtain(
            File file,
            AnnotationsLoader loader
            )
        throws IOException
        {
            CachedDocument result = null;
            String fullname = file.getCanonicalPath();
            synchronized( cache_ )
            {
                result = (CachedDocument) cache_.get(fullname);
                long lastModified = file.lastModified();
                if ( result == null 
                    || result.lastModified_ != lastModified )
                {
                    if ( Trace.on )
                    {
                        Trace.enter("[caching file " + fullname + "]");
                    }
                    result = new CachedDocument(
                        loader.load(file, fullname),
                        lastModified);
    
                    if ( Trace.on )
                    {
                        Trace.leave();
                    }
                    
                    cache_.put(fullname, result);
                }
            }
    
            return result.document_;
        }

        public static
        void
        clear()
        {
            cache_.clear();
        }
    }

    private static
    class Accumulator
    {
        public Vector prologs = new Vector();
        public Vector epilogs = new Vector();
    }

    private static final Element[] emptyElementArray_ = {};

    private Document document_;
    
    private Element root_;
    public  Element getRootElement() { return root_; }

    public
    String
    getRootAttribute(
        String name
        )
    {
        return root_.getAttribute(name);
    }

    private Hashtable configuration_ = new Hashtable();

    public
    String
    getConfigurable(
        String name
        )
    {
        return (String) configuration_.get(name);
    }

    private String[] prologs_ = {};
    public  String[] getPrologs() { return prologs_; }
    
    private String[] epilogs_ = {};
    public  String[] getEpilogs() { return epilogs_; }
    
    private Hashtable classes_ = new Hashtable();

    public 
    Element[]
    getClassElements(
        String name
        )
    {
        Element[] result = (Element[]) classes_.get(name);
        if ( result == null )
        {
            result = emptyElementArray_;
        }
        return result;
    }
    
    private Hashtable ids_ = new Hashtable();

    public 
    Element[]
    getIdElements(
        String name
        )
    {
        Element[] result = (Element[]) ids_.get(name);
        if ( result == null )
        {
            result = emptyElementArray_;
        }
        return result;
    }

    private Hashtable tags_ = new Hashtable();

    public 
    Element[]
    getTagElements(
        String name
        )
    {
        Element[] result = (Element[]) tags_.get(name);
        if ( result == null )
        {
            result = (Element[]) tags_.get("*");
        }
        if ( result == null )
        {
            result = emptyElementArray_;
        }

        return result;
    }

    public
    AnnotationsIndexImpl(
        File file,
        AnnotationsLoader loader
        )
    throws IOException
    {
        this(
            loader.load(file), 
            file.getCanonicalPath(),
            loader
            );
    }

    public
    AnnotationsIndexImpl(
        Document document,
        String filename,
        AnnotationsLoader loader
        )
    throws IOException
    {
        document_ = document;
        root_ = document.getDocumentElement();

        Accumulator accumulator = new Accumulator();
        parse(document, filename, loader, accumulator);
        
        prologs_ = new String[accumulator.prologs.size()];
        accumulator.prologs.copyInto(prologs_);
        
        epilogs_ = new String[accumulator.epilogs.size()];
        accumulator.epilogs.copyInto(epilogs_);
    }

    private
    void
    parse(
        Document document,
        String filename,
        AnnotationsLoader loader,
        Accumulator accumulator
        )
    throws IOException
    {
        Node child;
        Element element;
        String nodeName;

        NodeList children = 
            document.getDocumentElement().getChildNodes();
        int lim = children.getLength();
        int i = -1;
        while ( ++i < lim )
        {
            child = children.item(i);
            if ( child.getNodeType() == Node.ELEMENT_NODE )
            {
                element = (Element) child;
                nodeName = element.getNodeName();
                if ( "id".equals(nodeName) )
                {
                    addMultiEntry_(
                        ids_, element.getAttribute("name"), element);
                }
                else
                if ( "class".equals(nodeName) )
                {
                    addMultiEntry_(
                        classes_, element.getAttribute("name"), element);
                }
                else
                if ( "tag".equals(nodeName) )
                {
                    addTagEntry_(
                        tags_, 
                        Utils.defaultIfEmpty(
                            element.getAttribute("tag"), 
                            "*"),
                        element
                        );
                }
                else
                if ( "include".equals(nodeName) )
                {
                    include(
                        element.getAttribute("file"), 
                        filename,
                        loader,
                        accumulator
                        );
                }
                else
                if ( "prolog".equals(nodeName) )
                {
                    accumulator.prologs.addElement(
                        ElementUtils.getContent(element, false));
                }
                else
                if ( "epilog".equals(nodeName) )
                {
                    accumulator.epilogs.addElement(
                        ElementUtils.getContent(element, false));
                }
                else
                {
                    configuration_.put(nodeName, 
                        ElementUtils.getContent(element, false));
                }
            }
        }
    }

    /**
        Include a file into this document index.
        Included files are cached.
    **/
    private
    void
    include(
        String includeName,
        String includingName,
        AnnotationsLoader loader,
        Accumulator accumulator
        )
    throws IOException
    {
        File includeFile = loader.resolveInclude(includeName);
        if ( includeFile == null )
        {
            loader.errorHandler().error(
                "include file not found: " + includeName,
                includingName
                );
        }
        else
        {
            Document includedDocument = 
                CachedDocument.obtain(includeFile, loader);
            parse(includedDocument, includeName, loader, accumulator);
        }
    }
    
    // A collection class would provide more efficient insertion,
    // but less efficient access. 
    // It would also use a lot more memory.
    // Assumption: there is usually only one Dynamator file element
    // per id or class name.
    
    /**
        Append an element to the end of an array.
        
        @return
            a new array.
    **/
    private static
    Element[]
    append(
        Element[] array,
        Element item
        )
    {
        Element[] result = new Element[1 + array.length];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = item;
        return result;
    }
    
    /**
        Add an entry to an element index table by appending the
        element to the value array.
    **/
    private static
    void
    addMultiEntry_(
        Hashtable dictionary,
        Object key,
        Element element
        )
    {
        Element[] entry = (Element[]) dictionary.get(key);
        if ( entry == null )
        {
            entry = new Element[1];
            entry[0] = element;
            dictionary.put(key, entry);
        }
        else
        {
            dictionary.put(key, append(entry, element));
        }
    }

    /**
        Add an entry to the tag index table.
        This is like addMultiEntry_, except that tags potentially
        applying to all elements are merged into all other tag arrays.
        This is useful because tag matching is expensive, so it's much
        more performant to use keyed access.  It is necessary because
        tags are applied in document order.
    **/
    private static
    void
    addTagEntry_(
        Hashtable dictionary,
        Object tagname,
        Element element
        )
    {
        if ( "*".equals(tagname) )
        {
            // Add to all existing tag entries
            Enumeration e = dictionary.keys();
            Object key;
            while ( e.hasMoreElements() )
            {
                key = e.nextElement();
                Element[] entry = (Element[]) dictionary.get(key);
                dictionary.put(key, append(entry, element));
            }

            // Add to '*' entry
            if ( dictionary.get(tagname) == null )
            {
                Element[] entry = new Element[1];
                entry[0] = element;
                dictionary.put(tagname, entry);
            }
            else
            {
                Element[] entry = (Element[]) dictionary.get(tagname);
                dictionary.put(tagname, append(entry, element));
            }
        }
        else
        {
            Element[] allEntry = (Element[]) dictionary.get("*");
            if ( allEntry == null )
            {
                addMultiEntry_(dictionary, tagname, element);
            }
            else
            {
                Element[] entry = (Element[]) dictionary.get(tagname);
                if ( entry == null )
                {
                    entry = allEntry;
                }
                dictionary.put(tagname, append(entry, element));
            }
        }
    }

    public
    void
    dump(
        PrintWriter writer
        )
    {
        String key;
        Enumeration e;
        int i;

        writer.print("<");
        writer.print(root_.getNodeName());
        NamedNodeMap attrs = root_.getAttributes();
        i = attrs.getLength();
        while ( --i >= 0 )
        {
            Node attr = attrs.item(i);
            if ( ! attr.getNodeName().startsWith("_") )
            {
                writer.print(' ');
                writer.print(attr.getNodeName());
                writer.print('=');
                writer.print('"');
                writer.print(attr.getNodeValue());
                writer.print('"');
            }
        }
        writer.println('>');
        
        e = configuration_.keys();
        while ( e.hasMoreElements() )
        {
            key = (String) e.nextElement();
            writer.print("<");
            writer.print(key);
            writer.print(">");
            writer.print(configuration_.get(key));
            writer.print("</");
            writer.print(key);
            writer.println(">");
        }

        i = -1;
        while ( ++i < prologs_.length )
        {
            writer.print("<");
            writer.print("prolog");
            writer.print(">");
            writer.print(prologs_[i]);
            writer.print("</");
            writer.print("prolog");
            writer.println(">");
        }

        i = -1;
        while ( ++i < epilogs_.length )
        {
            writer.print("<");
            writer.print("epilog");
            writer.print(">");
            writer.print(epilogs_[i]);
            writer.print("</");
            writer.print("epilog");
            writer.println(">");
        }

        e = ids_.keys();
        while ( e.hasMoreElements() )
        {
            Element[] elements = (Element[]) ids_.get(e.nextElement());
            i = -1;
            while ( ++i < elements.length )
            {
                ElementUtils.print(elements[i], writer, false);
            }
            writer.println();
        }

        e = classes_.keys();
        while ( e.hasMoreElements() )
        {
            Element[] elements = (Element[]) classes_.get(e.nextElement());
            i = -1;
            while ( ++i < elements.length )
            {
                ElementUtils.print(elements[i], writer, false);
            }
            writer.println();
        }

        e = tags_.keys();
        while ( e.hasMoreElements() )
        {
            Element[] elements = (Element[]) tags_.get(e.nextElement());
            i = -1;
            while ( ++i < elements.length )
            {
                ElementUtils.print(elements[i], writer, false);
                writer.println();
            }
        }

        writer.print("</");
        writer.print(root_.getNodeName());
        writer.println('>');
    }
}

