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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
    DOM Element utilities.
**/
public
class ElementUtils
{
//    private static final String rcsID_ = 
//        "$Id: ElementUtils.java,v 1.3 2004/03/23 07:36:49 jaydunning Exp $";

    private static
    class SimpleNodeList
    implements NodeList
    {
        Vector data_ = new Vector();
        
        public
        int
        getLength()
        {
            return data_.size();
        }

        public
        Node
        item(
            int index
            )
        {
            return (Node) data_.elementAt(index);
        }

        public
        void
        put(
            Node item
            )
        {
            data_.addElement(item);
        }
    }
    
    /**
        Print a node list.
    **/
    private static
    void
    print(
        NodeList nodes,
        PrintWriter writer,
        boolean trim
        )
    {
        int nNodes = nodes.getLength();
        int i = -1;
        while ( ++i < nNodes )
        {
            print(nodes.item(i), writer, trim);
        }
    }
    
    /**
        Print an individual node.
    **/
    private static
    void
    print(
        Node node,
        PrintWriter writer,
        boolean trim
        )
    {
/*
    FIXME: 
    'trim' is implemented incorrectly.
    Consider the following XML:
        <abc> a b c  <![CDATA[  d  ]]>  e </abc>
    The desired result:  "a b c d e"
    The actual result:   "a b cde"
    I don't think anyone is likely to encounter this, because
    CDATA found here is likely to have been inserted
    by Dynamator.
*/
        short nodeType = node.getNodeType();
        if ( nodeType == Node.ELEMENT_NODE )
        {
            print((Element)node, writer, trim);
        }
        else
        if ( nodeType == Node.CDATA_SECTION_NODE )
        {
            writer.print(node.getNodeValue());
        }
        else
        if ( nodeType == Node.TEXT_NODE )
        {
            String text = node.getNodeValue();
            if ( trim )
            {
                // erase leading and trailing newlines and whitespace 
                // between tags
                text = text.trim();
            }
            writer.print(text);
        }
        else
        if ( nodeType == Node.COMMENT_NODE )
        {
            String text = node.getNodeValue();
            if ( trim )
            {
                // erase leading and trailing newlines and whitespace
                // between tags
                text = text.trim();
            }
            writer.print("<!-- ");
            writer.print(text);
            writer.print(" -->");
        }
        else
        {
            throw new FatalError(
                "Unexpected node type in annotations file: "
                + node.getNodeValue()
                );
        }
    }

    /**
        Print the end of an element start tag.
    **/
    private static
    void
    printNodeEnd(
        PrintWriter writer,
        String nodeName,
        NodeList children,
        boolean trim
        )
    {
        if ( children == null || children.getLength() == 0 )
        {
            writer.print("/>");
        }
        else
        {
            writer.print(">");

            print(children, writer, trim);
            
            writer.print("</");
            writer.print(nodeName);
            writer.print(">");
        }
    }
    
    /**
        Print an element.
    **/
    public static
    void
    print(
        Element element,
        PrintWriter writer,
        boolean trim
        )
    {
        String nodeName = element.getNodeName();

        writer.print('<');
        writer.print(nodeName);

        NamedNodeMap attributes = element.getAttributes();
        if (attributes != null) 
        {
            int nAttributes = attributes.getLength();
            int i = -1;
            while ( ++i < nAttributes )
            {
                Attr attribute = (Attr)attributes.item(i);
                if ( "_dyn_loc_".equals(attribute.getNodeName()) )
                {
                    continue;
                }
                writer.print(' ');
                writer.print(attribute.getNodeName());
                writer.print("=\"");
                writer.print(attribute.getNodeValue());
                writer.print('"');
            }
        }

        printNodeEnd(writer, nodeName, element.getChildNodes(), trim);
    }

    /**
        Print the contents of a file.
    **/
    private static
    void
    printFileContents(
        String filename,
        PrintWriter writer
        )
    {
        // filename was set to absolute path and validated by 
        // Annotations.DocumentIndex.fixupDynamatorCopyElements.
        File file = new File(filename);
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(file));
            char[] buf = new char[255];
            int i;
            while ( (i = reader.read(buf, 0, 255)) != -1 )
            {
                writer.write(buf, 0, i);
            }
        }
        catch (IOException x)
        {
            throw new FatalError(x.toString());
        }
        finally
        {
            if ( reader != null )
            {
                try
                {
                    reader.close();
                }
                catch(IOException x)
                {}
            }
        }
    }
    
    /**
        Access method to obtain the content of an element.  

        @param element
            The element whose content is desired.
        @return
            If the element has no children, the value is an empty
            string.  Otherwise, the value is the string representation
            of all the children, with leading and trailing whitespace
            removed. 
    **/
    public static
    String
    getContent(
        Element element
        )
    {
        return getContent(element, true);
    }
    
    private static
    boolean
    isCDataOrText(
        int nodeType
        )
    {
        return Node.CDATA_SECTION_NODE == nodeType
            || Node.TEXT_NODE  == nodeType;
    }
    
    /**
        Access method to obtain the content of an element.  

        @param element
            The element whose content is desired.
        @param trim
            Whether leading and trailing whitespace should be removed.
        @return
            If the element has no children, the value is an empty
            string.  Otherwise, the value is the string representation
            of all the children.
    **/
    public static
    String
    getContent(
        Element element,
        boolean trim
        )
    {
        String result = null;
        
        NodeList elements = element.getChildNodes();
        if ( elements.getLength() == 0 )
        {
            result = "";
        }
        else
        if ( elements.getLength() == 1 
            && isCDataOrText(elements.item(0).getNodeType()) )
        {
            result = elements.item(0).getNodeValue();
            if ( trim )
            {
                result = result.trim();
            }
        }
        else
        {
            StringWriter buf = new StringWriter();
            PrintWriter writer = new PrintWriter(buf, true);
            print(elements, writer, trim);
            writer.flush();
            result = buf.toString();
            if ( trim )
            {
                result = result.trim();
            }
        }

        return result;
    }
        
    /**
        Obtain the text content of an element.  

        @param element
            The element whose text content is desired.
        @return
            If the element has no text or CDATA children, the value is
            an empty string.  Otherwise, the value is the string
            representation of all the text and CDATA children, with
            leading and trailing whitespace removed. 
    **/
    public static
    String
    getTextContent(
        Element element
        )
    {
        return getTextContent(element, true);
    }
    
    /**
        Obtain the text content of an element.  

        @param element
            The element whose text content is desired.
        @param trim
            Whether leading and trailing whitespace should be removed.
        @return
            If the element has no text or CDATA children, the value is
            an empty string.  Otherwise, the value is the string
            representation of all the text and CDATA children.
    **/
    public static
    String
    getTextContent(
        Element element,
        boolean trim
        )
    {
        String result = null;
        
        NodeList nodes = element.getChildNodes();
        if ( nodes.getLength() == 0 )
        {
            result = "";
        }
        else
        {
            StringBuffer buf = new StringBuffer(64);
            Node node;

            int nNodes = nodes.getLength();
            int i = -1;
            while ( ++i < nNodes )
            {
                node = nodes.item(i);
                short nodeType = node.getNodeType();
                if ( nodeType == Node.CDATA_SECTION_NODE 
                    || nodeType == Node.TEXT_NODE )
                {
                    String text = node.getNodeValue();
                    buf.append(text);
                }
            }
            result = buf.toString();
            if ( trim )
            {
                result = result.trim();
            }
        }

        return result;
    }
        
    /**
        Obtain the content of the first child element.
    
        @param parent
            The element containing the child element whose content is
            desired. 
        @param tagName
            The name of the child element whose content is desired.
        @return
            The trimmed content of the first child element of 'parent'
            that has the name 'tagName', or null if no such element is
            found. If an element is found but has no content, an empty
            string is returned.
    **/
    public static
    String
    getSingleContent(
        Element parent,
        String tagName
        )
        // get the contents of the <tagName> tag
        // within <parent>
    {
        return getSingleContent(parent, tagName, true);
    }

    /**
        Obtain the content of a child element.
    
        @param parent
            The element containing the child element whose content is
            desired. 
        @param tagName
            The name of the child element whose content is desired.
        @param trim
            Whether leading and trailing whitespace should be removed.
        @return
            The content of the first child element of 'parent' that has
            the name 'tagName', or null if no such element is found.
            If an element is found but has no content, an empty string
            is returned.
    **/
    public static
    String
    getSingleContent(
        Element parent,
        String tagName,
        boolean trim
        )
        // get the contents of the <tagName> tag
        // within <parent>
    {
        String result = null;
        Element element = getSingleElement(parent, tagName);
        if ( element != null )
        {
            result = getContent(element, trim);
        }

        return result;
    }
    
    /**
        Obtain a child element with a specific name.
        <p>
        This method returns the first child element with the requested
        name.  Subsequent elements with the same name are ignored.

        @param parent
            The element containing the child element desired.
        @param tagName
            The name of the child element desired.
        @return
            The first child element within 'parent' that has the tag
            name 'tagName', or null if none are found.
    **/
    public static
    Element
    getSingleElement(
        Element parent,
        String tagName
        )
    {
        Node child = parent.getFirstChild();
        while ( child != null )
        {
            if ( Node.ELEMENT_NODE == child.getNodeType()
                && tagName.equals(child.getNodeName()) )
            {
                return (Element) child;
            }
            child = child.getNextSibling();
        }

        return null;
    }
    
    /**
        Whether a set of attributes contains any attribute named
        in a set of attribute names.
        
        @param names
            A space-separated list of attribute names.
        @param attributes
            Element attributes.
    **/
    public static
    boolean
    hasAnyAttribute(
        String names,           // space-separated
        Attributes attributes
        )
    {
        boolean result = false;
        
        // in most cases, only one attribute name will be
        // specified
        if ( names.indexOf(' ') < 0 )
        {
            return attributes.getIndex(names) >= 0;
        }
        
        StringTokenizer tokenizer = 
            new StringTokenizer(names);
        String token;
        while ( tokenizer.hasMoreTokens() )
        {
            token = tokenizer.nextToken();
            if ( attributes.getIndex(token) >= 0 )
            {
                result = true;
                break;
            }
        }

        return result;
    }
    
    /**
        Whether a set of attributes contains each attribute named 
        in a set of attribute names.
        
        @param names
            A space-separated list of attribute names.
        @param attributes
            Element attributes.
    **/
    public static
    boolean
    hasAllAttributes(
        String names,           // space-separated
        Attributes attributes
        )
    {
        boolean result = true;
        
        // in most cases, only one attribute name will be
        // specified
        if ( names.indexOf(' ') < 0 )
        {
            return attributes.getIndex(names) >= 0;
        }
        
        StringTokenizer tokenizer = 
            new StringTokenizer(names);
        String token;
        while ( tokenizer.hasMoreTokens() )
        {
            token = tokenizer.nextToken();
            if ( attributes.getIndex(token) < 0 )
            {
                result = false;
                break;
            }
        }

        return result;
    }
        
    /**
        Obtain all child elements with a specific name.
        
        <pre>
            &lt;name...&gt;
        </pre>

        @param parent
            The element containing the child elements desired. 
        @param tagName
            The name of the child elements desired.
        @return
            all matching elements in document order, or an empty array
            if none are found. 
    **/
    public static
    Element[]
    getChildElements(
        Element parent,
        String tagName
        )
    {
        NodeList nodes = getChildrenByTagName(parent, tagName);
        int i = nodes.getLength();
        Element[] result = new Element[i];
        while ( --i >= 0 )
        {
            result[i] = (Element) nodes.item(i);
        }

        return result;
    }

    /**
        Obtain all immediate children of an element with a specific tag
        name.  Like Element.getElementsByTagName, but only returns
        immediate children. 

        @param parent
            The element containing the child elements desired. 
        @param tagName
            The name of the child elements desired.
    **/
    public static
    NodeList
    getChildrenByTagName(
        Element parent,
        String tagName
        )
    {
        SimpleNodeList result = new SimpleNodeList();
        
        NodeList children = parent.getChildNodes();
        Node child;
        Element childElement;
        int lim = children.getLength();
        int i = -1;
        while ( ++i < lim )
        {
            child = children.item(i);
            if ( child.getNodeType() == Node.ELEMENT_NODE )
            {
                childElement = (Element) child;
                if ( tagName.equals(childElement.getNodeName()) )
                {
                    result.put(child);
                }
            }
        }

        return result;
    }

    /**
        Parse a file, creating an XML document object.
    **/
    public static
    Document
    parse(
        InputStream input,
        String filename,
        DocumentBuilder builder
        )
    throws IOException
    {
        InputSource source = new InputSource(input);
        source.setPublicId(filename.replace('\\','/'));

        try
        {
            return builder.parse(source);
        }
        catch (SAXException saxException)
        {
            try
            {
                input.close();
            }
            catch(Exception x)
            {}
            
            Exception x = saxException.getException();

            if ( x == null )
            {
                x = saxException;
            }
            else
            if ( FatalError.class.isAssignableFrom(x.getClass()) )
            {
                throw (FatalError) x;
            }

            throw new FatalError(
                filename + ": Errors in parse",
                x
                );
        }
    }
}

