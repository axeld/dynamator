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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import org.w3c.dom.Element;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.ext.LexicalHandler;

/**
    Dynamator workhorse.  Parses an XML file and applies annotations to
    generate a file in the target programming language.
**/
public 
class DynamateXml
extends DefaultHandler 
implements LexicalHandler
{
//    private static final String rcsID_ = 
//        "$Id: DynamateXml.java,v 1.18 2004/08/07 18:17:44 jaydunning Exp $";
    
    /**
        XML element stack information.
    **/
    private
    class NodeInformation
    {
        // The only attribute that is set by the client
        public int        outputLineNumber;
        
        // ... all the rest are logically final
        public String     tagName;
        public String     finalTagName;
        public String     delimitedTagName;
        
        public Attributes attrs;
        public String[]   classNames = {};
        public String     idName;
        public boolean    hidesSelf                = false;
        public boolean    hidesChildren            = false;
        public boolean    hiddenByParent           = false;

        public Element    contentElement           = null;
        public Element    rawContentElement        = null;

        public boolean    isManuallyIndentedBefore        = false;
        public boolean    isManuallyIndented              = false;
        public boolean    programIsManuallyIndentedBefore = false;
        public boolean    programIsManuallyIndented       = false;

        public Element    extractElement           = null;
        
        public Element    beforeElement            = null;
        public Element    beforeContentElement     = null;
        public Element    afterContentElement      = null;
        public Element    afterElement             = null;

        public Element    rawAttrsElement          = null;

        public Element    ifElement                = null;
        public Object     ifResult                 = null;
        public boolean    hasIfBlock               = false;

        public Element    foreachElement           = null;
        public char       foreachBlockType         = FOREACH_TYPE_NONE;
        public Object     foreachExpression        = null;
        public boolean    enclosesForeachTag       = false;
        public String     foreachIfExpression      = null;
        public Object     foreachIfResult          = null;

        // All overrides from the annotations file that apply to this
        // node, in order: all <id>s, then all <class>es, then all <tag>s.
        public Element[]  overrides = {};

        public
        NodeInformation(
            String name,
            Attributes attributes,
            NodeInformation currentNode
            )
        {
            String classValue = attributes.getValue(classAttributeName_);

            this.tagName = this.finalTagName = name;
            this.attrs = attributes;

            if ( currentNode != null && currentNode.hidesChildren )
            {
                hiddenByParent = true;
                this.hidesSelf = true;
                this.hidesChildren = true;
            }

            if ( classValue != null && classValue.length() > 0 )
            {
                StringTokenizer tokenizer = new StringTokenizer(classValue);
                classNames = new String[tokenizer.countTokens()];
                int iClassName = -1;
                String className;
                while ( tokenizer.hasMoreTokens() )
                {
                    className = tokenizer.nextToken();
                    classNames[++iClassName] = className;
                    if ( className.equals(discardNodeClassName_) )
                    {
                        hidesSelf = true;
                    }
                    else
                    if ( className.equals(
                            discardNodeAndChildrenClassName_) )
                    {
                        hidesSelf = true;
                        hidesChildren = true;
                    }
                }
            }

            // don't bother to check overrides if this element is 
            // part of discarded content
            if ( ! hiddenByParent )
            {
                overrides = collectOverrides(name, attributes);
                checkOverrides();
            }

            delimitedTagName = "|" + finalTagName + "|";
        }
        
        // All overrides from the annotations file that apply to this
        // node, in order: all <id>s, then all <class>es, then all <tag>s.
        private 
        Element[]
        collectOverrides(
            String name,
            Attributes attributes
            )
        {
            Element[]   idElements    = {};
            Element[][] classElements = {};
            Element[]   tagElements   = {};
            int totalLength = 0;

            String idValue = attributes.getValue(idAttributeName_);
            if ( idValue == null || idValue.length() == 0 )
            {
                idName = null;
            }
            else
            {
                idName = idValue;
                idElements = annotations_.getIdElements(idName);
                totalLength += idElements.length;
            }

            int iClass = classNames.length;
            classElements = new Element[iClass][];
            while ( --iClass >= 0 )
            {
                classElements[iClass] = 
                    annotations_.getClassElements(classNames[iClass]);
                totalLength += classElements[iClass].length;
            }

            {
                tagElements = 
                    annotations_.getMatchingTagElements(name, attributes);
                totalLength += tagElements.length;
            }

            Element[] result = new Element[totalLength];
            int iResult = 0;

            System.arraycopy(
                idElements, 0, result, iResult, idElements.length);
            iResult += idElements.length;
            
            Element[] temp;
            iClass = classElements.length;
            while ( --iClass >= 0 )
            {
                temp = classElements[iClass];
                System.arraycopy(temp, 0, result, iResult, temp.length);
                iResult += temp.length;
            }

            System.arraycopy(
                tagElements, 0, result, iResult, tagElements.length);

            return result;
        }

        public
        String
        overrideValueFor(
            String modifier
            )
        {
            String result = null;
            Element element = overrideElementFor(modifier);
            if ( element != null )
            {
                result = ElementUtils.getContent(element, true);
            }
            return result;
        }
        
        public
        Element
        overrideElementFor(
            String modifier
            )
        {
            Element result = null;

            int i = -1;
            while ( result == null
                && ++i < overrides.length )
            {
                result = 
                    ElementUtils.getSingleElement(
                        overrides[i], modifier);
            }

            if ( Trace.on
                && result != null )
            {
                String overrideType = overrides[i].getTagName();
                String overrideName = 
                    overrideType.equals("tag")
                    ?  overrides[i].getAttribute("tag")
                    :  overrides[i].getAttribute("name");
                Trace.display(
                    "[found " + modifier + " override for "
                    + overrideType + " "
                    + overrideName + "]");
            }

            return result;
        }
        
        private
        void
        checkOverrides()
        {
            if ( overrideElementFor("discard") != null )
            {
                hidesSelf = true;
                hidesChildren = true;
            }
            else
            if ( overrideElementFor("discard-tag") != null )
            {
                hidesSelf = true;
            }

            extractElement = overrideElementFor("extract");
            beforeElement  = overrideElementFor("before");
            afterElement   = overrideElementFor("after");

            ifElement      = overrideElementFor("if");

            foreachElement = overrideElementFor("foreach");
            if ( foreachElement == null )
            {
                foreachElement = overrideElementFor("for");
            }

            rawAttrsElement = overrideElementFor("raw-attrs");

            Element tagNameElement = overrideElementFor("rename");
            if ( tagNameElement != null )
            {
                String newName = tagNameElement.getAttribute("to");
                if ( newName != null && newName.length() != 0 )
                {
                    finalTagName = newName;
                }
            }

            if ( ! hidesChildren )
            {
                // no point in looking for these if they aren't going
                // to be applied
                beforeContentElement = overrideElementFor("before-content");
                contentElement       = overrideElementFor("content");
                rawContentElement    = overrideElementFor("raw-content");
                afterContentElement  = overrideElementFor("after-content");
            }

            if ( contentElement != null
                || rawContentElement != null )
            {
                hidesChildren = true;

                if ( contentElement != null 
                    && rawContentElement != null )
                {
                    errorHandler_.warning(
                        "Found both 'content' and 'raw-content' modifiers",
                        locator_);
                }
            }
        }
        
        public
        AttributeOverride[]
        getAttributeOverrides()
        {
            return annotations_.getAttributeOverrides(overrides);
        }
    }
    
    private Vector nodeStack_ = new Vector();

    public
    NodeInformation
    pushNode(
        String tagName,
        Attributes attrs
        )
    {
        NodeInformation currentNode = 
            nodeStack_.isEmpty()
            ? null
            : (NodeInformation) nodeStack_.lastElement();
        
        NodeInformation result = 
            new NodeInformation(tagName, attrs, currentNode);

        nodeStack_.addElement(result);

        return result;
    }

    public
    NodeInformation
    popNode()
    {
        nodeStack_.removeElementAt(nodeStack_.size()-1);
        return 
            nodeStack_.size() > 0
            ? (NodeInformation) (nodeStack_.lastElement())
            : null;
    }


    protected String defaultObjectType_;

    protected Annotations annotations_;
    protected Generator generator_;
    protected Stack generatorStack_ = new Stack();
    protected Vector extracts_ = new Vector();
    protected Indentation indentation_;
    protected Locator locator_ = null;
    protected ErrorHandler errorHandler_ = null;

    protected boolean stripComments_ = false;

    private NodeInformation currentNode_;

    private static final String defaultIDAttributeName_     = "id";
    private static final String defaultClassAttributeName_  = "class";

    // values that may be specified in the dynamic annotations file
    protected String tagsWithoutEnd_;
    protected String inclusiveIterationTags_;
    protected String exclusiveIterationTags_;
    protected String preformattedTags_;
    protected Dictionary inlineTags_;
    protected String optionalInlineTags_;
    protected String huggingEndTags_;
    protected String flagAttributes_;
    protected String idAttributeName_;
    protected String classAttributeName_;

        // The node and all children of the node, including text,
        // are omitted from the generated page. 
        // Processing may be performed if the node has an id or
        // another class value with a corresponding entry in the 
        // dynamic annotations file.
    protected static final String discardNodeAndChildrenClassName_
        = "Discard";

        // The node is omitted.
    protected static final String discardNodeClassName_ 
        = "DiscardTag";

    private static final char FOREACH_TYPE_NONE       = '\0';
    private static final char FOREACH_TYPE_COLLECTION = 'c';
    private static final char FOREACH_TYPE_SEQUENCE   = 's';
    private static final char FOREACH_TYPE_FOR        = 'f';
        
    private static final SAXParserFactory saxParserFactory_ =
        initializeSAXParserFactory();

    private static
    SAXParserFactory
    initializeSAXParserFactory()
    {
        try
        {
            return SAXParserFactory.newInstance();
        }
        catch(Exception x)
        {
            throw new FatalError(x);
        }
    }
    
    protected String traceWhere_ = "";

    protected InputStream inputStream_;
    protected Options options_;

    private boolean inDTD_ = false;
    private String currentEntity_ = null;
    private boolean inPre_ = false;
// 20010128: browser whitespace bug
    private boolean tagPrecededByWhitespace_ = false;

    private boolean xhtml_ = false;

    protected 
    DynamateXml(
        Options options,
        File file,
        Annotations annotations,
        Generator generator
        ) 
    throws IOException
    {
        options_     = options;
        annotations_ = annotations;
        generator_   = generator;
        indentation_ = options.indentation;

//verbose
//        options.errorStream.println(
//            file.toString()
//            + " + "
//            + annotations_.name()
//            + " ==> "
//            + generator_.fileName()
//            );
//or            
//        options.errorStream.println("template:    " + file.toString());
//        options.errorStream.println("annotations: " + annotations_.name());
//        options.errorStream.println("output:      " + generator_.fileName());
        
        tagsWithoutEnd_ = 
            getTagList("tagsWithoutEnd",
                options.defaultTagsWithoutEnd);
        inclusiveIterationTags_ = 
            getTagList("inclusiveIterationTags", 
                options.defaultInclusiveIterationTags);
        exclusiveIterationTags_ = 
            getTagList("exclusiveIterationTags", 
                options.defaultExclusiveIterationTags);
        preformattedTags_ = 
            getTagList("preformattedTags", 
                options.defaultPreformattedTags);
        inlineTags_ = 
            tagListToDictionary(getTagList("inlineTags", 
                options.defaultInlineTags));
        optionalInlineTags_ = 
            getTagList("optionalInlineTags", 
                options.defaultOptionalInlineTags);
        huggingEndTags_ = 
            getTagList("huggingEndTags", 
                options.defaultHuggingEndTags);
        flagAttributes_ = 
            getTagList("flagAttributes", 
                options.defaultFlagAttributes);

        idAttributeName_ =
            Utils.defaultIfEmpty(
                annotations_.getConfigurable("idAttributeName"),
                defaultIDAttributeName_);
        classAttributeName_ =
            Utils.defaultIfEmpty(
                annotations_.getConfigurable("classAttributeName"),
                defaultClassAttributeName_);

        inputStream_ = new XmlFileInputStream(file);
        errorHandler_ = new ErrorHandler(options.errorStream);

        generator_.setErrorHandler(errorHandler_);
        
        String commentStart = annotations_.getCommentStart();
        if ( commentStart != null )
        {
            options.commentStart = commentStart;
        }

        String commentEnd = annotations_.getCommentEnd();
        if ( commentEnd != null )
        {
            options.commentEnd = commentEnd;
        }

        stripComments_ = options.stripComments;

        if ( Trace.on )
        {
            Trace.clearPrefix();
            Trace.display("processing template file " + file.toString());
        }
    }

    private
    Dictionary
    tagListToDictionary(
        String tagList
        )
    {
        Dictionary result = new Hashtable();
        StringTokenizer tokenizer = new StringTokenizer(tagList, "|");
        while ( tokenizer.hasMoreTokens() )
        {
            result.put(tokenizer.nextToken(), Boolean.TRUE);
        }
        return result;
    }

    private
    String
    getTagList(
        String name,
        String defaultValue
        )
    {
        String result = annotations_.getConfigurable(name);
        if ( result == null 
            || result.length() == 0 
            || Utils.onlyWhitespace(result) )
        {
            result = defaultValue;
        }
        else
        {
            result = normalizeTagList(result);
        }

        return result;
    }
    
    private static
    String
    normalizeTagList(
        String s
        )
    {
        StringTokenizer tokenizer = 
            new StringTokenizer(s, " \t\n\r,|;:");
        StringBuffer buf = new StringBuffer(2+s.length());
        while ( tokenizer.hasMoreTokens() )
        {
            buf.append('|');
            buf.append(tokenizer.nextToken());
        }
        buf.append('|');
        return buf.toString();
    }

    public
    void
    setDocumentLocator(
        Locator locator
        )
    {
        locator_ = locator;
        generator_.setLocator(locator);
        if ( Trace.on )
        {
            Trace.setPrefix(
                new Object()
                {
                    private 
                    String 
                    rjust(String value, int width)
                    {
                        int jwidth = width - value.length();
                        String pad = 
                            ( jwidth < 0 )
                            ? ""
                            : "         ".substring(0, jwidth);

                        return pad + value;
                    }

                    private 
                    String 
                    just(String value, int width)
                    {
                        int jwidth = width - value.length();
                        String pad = 
                            ( jwidth < 0 )
                            ? ""
                            : "         ".substring(0, jwidth);

                        return value + pad;
                    }

                    public 
                    String 
                    toString()
                    {
                        return "["
                            + just(traceWhere_, 7)
                            + " "
                            + rjust(
                                String.valueOf(locator_.getLineNumber()), 5)
                            + ","
                            + rjust(
                                String.valueOf(locator_.getColumnNumber()), 4)
                            + "]";
                    }
                }
            );
        }        
    }
    
    public static 
    void
    process(
        Options options,
        File file,
        Annotations annotations,
        Generator generator
        ) 
    {
        DynamateXml handler = null;
        try
        {
            handler = new DynamateXml(
                options, file, annotations, generator);

            SAXParser saxParser = saxParserFactory_.newSAXParser();
            
            XMLReader parser = saxParser.getXMLReader();
            
//            String parserName = options.parserName;
//
//            XMLReader parser = 
//                (XMLReader) Class.forName(parserName).newInstance();
    
            parser.setProperty(
                "http://xml.org/sax/properties/lexical-handler",
                handler);
    
            parser.setContentHandler(handler);
            parser.setErrorHandler(handler.errorHandler_);
            parser.setEntityResolver(handler);
    
            InputSource input = new InputSource(handler.inputStream_);
            input.setEncoding(options.encoding);
            input.setPublicId(file.toString());
    
            parser.parse(input);
    
            if ( handler.errorHandler_.foundErrors() )
            {
                throw new FatalError();
            }
        }
        catch(FatalError fatal)
        {
            throw fatal;
        }
        catch(SAXException saxException)
        {
            if ( handler != null
                && handler.inputStream_ != null )
            {
                try
                {
                    handler.inputStream_.close();
                }
                catch(Exception x)
                {}
            }

            Exception x = saxException.getException();
            if ( x == null )
            {
                x = saxException;
            }

            if ( FatalError.class.isAssignableFrom(x.getClass()) )
            {
                throw (FatalError) x;
            }

            throw new FatalError("Errors in parse", x);
        }
        catch(Exception x)
        {
            if ( handler != null 
                && handler.inputStream_ != null )
            {
                try
                {
                    handler.inputStream_.close();
                }
                catch(Exception ignore)
                {}
            }

            throw new FatalError(x);
        }
        finally
        {
            if ( handler != null 
                && handler.generator_ != null )
            {
                try
                {
                    handler.generator_.close();
                }
                catch(Exception x)
                {}
            }
        }
    }
    
    private
    boolean
    outputting()
    {
        return 
            currentNode_ == null
            ? ! options_.bodyOnlyHtml
            : ! currentNode_.hidesChildren;
    }
    
    /**
        Handle an XML processing instruction.
        <p>
        The following processing instructions are inserted by Dynamator
        input stream processing and treated specially here:
        <dl>
          <dt>fakeout_passthru</dt>
          <dd>A block of XML to be emitted as-is.
          <dt>fakeout_cdata</dt>
          <dd>A wrapped CDATA section.
          <dt>fakeout_pi</dt>
          <dd>A wrapped processing instruction.
        </dl>
        <p>
        Any other PI that reaches this code is emitted as-is, as a PI.
    **/
    public 
    void 
    processingInstruction(
        String target, 
        String data
        ) 
    {
        // These PIs are inserted by FakeoutSaxInputStream
        if ( target.equals("fakeout_passthru") )
        {
            // a <!DOCTYPE declaration or similar
            // ... inserted by FakeoutSaxInputStream
            
            // XHTML requires minimized syntax for tags without content
            // (eg. <br />)
    
            if ( data.startsWith("<!DOCTYPE") 
                && data.indexOf("-//W3C//DTD XHTML") > 0 )
            {
                xhtml_ = true;
            }

            if ( ! outputting() )
            {
                if ( Trace.on )
                {
                    Trace.display("discarding " 
                        + Utils.firstWord(data) + "...");
                }
            }
            else
            {
                if ( Trace.on )
                {
                    Trace.display(Utils.firstWord(data) + "...");
                }

                generator_.outputXmlEscape(data.trim());
            }
        }
        else
        if ( ! outputting() )
        {
            if ( Trace.on )
            {
                Trace.display("discarding " 
                    + Utils.firstWord(data) + "...");
            }

            return;
        }
        else
        if ( target.equals("fakeout_pi") )
        {
            // a processing instruction
            // ... inserted by FakeoutSaxInputStream
            
            if ( Trace.on )
            {
                Trace.display("<?" + Utils.firstWord(data) + "... ?>");
            }

            generator_.outputProcessingInstruction(data.trim());
        }
        else
        if ( target.equals("fakeout_cdata") )
        {
            // a cdata section
            // ... inserted by FakeoutSaxInputStream
            
            generator_.outputCDATA(data.trim());
        }
        else
        {
            // A template PI
            if ( Trace.on ) 
            {
                Trace.display("<?" + target + "... ?>");
            }

            generator_.outputTemplate("<?");
            generator_.outputTemplate(target);
            if (data != null && data.length() > 0) 
            {
                generator_.outputTemplate(' ');
                generator_.outputTemplate(data);
            }
            generator_.outputTemplate("?>");
            generator_.endTemplateLine(false);
        }
        if ( Trace.on ) 
        {
            Trace.leave();
        }
    }

    public 
    void 
    startDocument() 
    {
        generator_.start(options_.produceGenerationComment);

        String[] prologs = annotations_.getPrologs();
        int i = -1;
        int lim = prologs.length;
        while ( ++i < lim )
        {
            generator_.outputRaw(prologs[i]);
        }
    }
    
    public
    void
    endDocument()
    {
        generator_.conditionallyEndTemplateLine(true);
        generator_.conditionallyEndProgramLine();

        String beforeExtracts = 
            annotations_.getConfigurable("before-extracts");
        if ( beforeExtracts != null && beforeExtracts.length() != 0 )
        {
            generator_.outputRaw(
                trimAnnotation(beforeExtracts));
        }
        
        generator_.flush();
        
        Enumeration e = extracts_.elements();
        try
        {
            while ( e.hasMoreElements() )
            {
                ((ByteArrayOutputStream) e.nextElement()).writeTo(
                    generator_.rawOutputStream());
            }
        }
        catch(IOException x)
        {
            throw new FatalError(
                "Could not output extracts: " 
                + x.getMessage());
        }

        String afterExtracts = 
            annotations_.getConfigurable("after-extracts");
        if ( afterExtracts != null && afterExtracts.length() != 0 )
        {
            generator_.outputRaw(
                trimAnnotation(afterExtracts));
        }
        
        String[] epilogs = annotations_.getEpilogs();
        int i = -1;
        int lim = epilogs.length;
        while ( ++i < lim )
        {
            generator_.outputRaw(epilogs[i]);
        }

        generator_.end(options_.produceGenerationComment);
    }
    
    // Lexical Handler interface

    // CDATA sections are wrapped by FakeoutSaxInputStream, so 
    // CDATA callbacks are not used.
    public
    void
    startCDATA()
    {
        if ( Trace.on )
        {
            traceWhere_ = "CDATA";
            Trace.enter("CDATA");
        }
    }
    
    public
    void endCDATA()
    {
        if ( Trace.on )
        {
            traceWhere_ = "CDATA";
            Trace.leave("CDATA");
        }
    }
    
    public
    void
    startDTD(
        String name, 
        String publicID, 
        String systemID
        )
    {
        if ( Trace.on )
        {
            traceWhere_ = "DTD";
            Trace.enter("<!DTD " + name);
        }
        
        inDTD_ = true;
    }
    
    public
    void
    endDTD()
    {
        if ( Trace.on )
        {
            traceWhere_ = "DTD";
            Trace.leave();
        }
        
        inDTD_ = false;
    }
    
    private static final String dynamatorDivTagName_    = "div";
    private static final String dynamatorEndDivTagName_ = "/div";
    private static final String dynamatorDivTagMarker_  = "dyn:div";
    
    public
    void
    comment(
        char[] buf, 
        int offset, 
        int len
        )
    {
        if ( Trace.on )
        {
            traceWhere_ = "comment";
        }

        // <!-- div id="xxx" class="xxx" -->
        // ...
        // <!-- /div -->
        // is treated as a real div

        String firstWord = Utils.firstWord(buf, offset, len);

        boolean commentDiv = false;
        AttributesImpl attributes = null;
        boolean discardComment = stripComments_ || ! outputting();

        /*
            Comment div processing
            
            A comment div causes 2 nodes to be pushed on the node
            stack. The first node is pushed in this method just to
            determine whether the comment should be output.  
            The second node is pushed in startElement, as if it were a
            real 'div' element. It is given a 'DiscardTag' class to 
            prevent it from being output.
        */
        if ( dynamatorEndDivTagName_.equalsIgnoreCase(firstWord) )
        {
            if ( currentNode_.tagName.equals(dynamatorDivTagMarker_) )
            {
                commentDiv = true;
                currentNode_.tagName = dynamatorDivTagName_;
                endElement("", "", firstWord.substring(1));
                discardComment = currentNode_.hidesSelf;
                currentNode_ = popNode();
            }
            else
            {
                generator_.warning(
                    "Ignoring unmatched <!-- /div -->");
            }
        }
        else
        if ( dynamatorDivTagName_.equalsIgnoreCase(firstWord) )
        {
            commentDiv = true;
            String comment = new String(buf, offset, len);
            int p = comment.indexOf(dynamatorDivTagName_)
                + dynamatorDivTagName_.length();
            Dictionary attributeDict = Utils.parseAttributes(
                comment.substring(p));
            
            // Only the 'id' and 'class' attributes matter.
            
            attributes = new AttributesImpl();
            String value = (String) attributeDict.get("id"); 
            if ( value != null )
            {
                attributes.addAttribute(
                    "",         // uri
                    "id",       // localName
                    "id",       // qName
                    "ID",       // type
                    value
                    );
            }

            value = (String) attributeDict.get("class");

            // save the original attributes on the stack
            // ... this more overhead than I'd prefer just to 
            //     remember whether the /div should be output
            attributes.addAttribute(
                "",         // uri
                "class",    // localName
                "class",    // qName
                "CDATA",    // type
                value
                );
            currentNode_ = pushNode("div", attributes);
            discardComment = currentNode_.hidesSelf;

            // Prevent comment div node from obstructing
            // content overrides
            if ( currentNode_.hidesChildren 
                && ! currentNode_.hiddenByParent 
                && ( currentNode_.contentElement != null 
                    || currentNode_.rawContentElement != null 
                    || currentNode_.beforeContentElement != null 
                    || currentNode_.afterContentElement != null 
                    ) )
            {
                currentNode_.hidesChildren = false;
            }

            // Prevent output of simulated div element.
            if ( value == null )
            {
                value = discardNodeClassName_;
            }
            else
            {
                if ( ! Utils.containsWord(value, 
                        discardNodeAndChildrenClassName_)
                    && ! Utils.containsWord(value, 
                        discardNodeClassName_) )
                {
                    value += " " + discardNodeClassName_;
                }
            }
            
            attributes.setAttribute(
                attributes.getLength()-1,
                "",         // uri
                "class",    // localName
                "class",    // qName
                "CDATA",    // type
                value
                );
        }
        
        if ( options_.bodyOnlyHtml
            && nodeStack_.size() < 2 )
        {
            discardComment = true;
        }
        
        if ( discardComment )
        {
            // e.g. <style class="Discard">
            //        <!-- stylesheet -->
            //      </style>

            if ( Trace.on ) 
            {
                Trace.display(
                    "[discarding comment <!--" 
                    + firstWord
                    + "... -->]");
            }
        }
        else
        if ( commentDiv 
            || currentNode_ == null 
            || (currentNode_.contentElement == null 
                && currentNode_.rawContentElement == null ) )
        {
            if ( Trace.on ) 
            {
                Trace.display("<--" + firstWord + "... -->");
            }
    
//20040215// 20030726: browser whitespace bug
//20040215            if ( tagPrecededByWhitespace_ )
//20040215            {
//20040215                generator_.outputTemplate(' ');
//20040215            }
            generator_.outputTemplate("<!--");
            generator_.outputTemplate(buf, offset, len);
            generator_.outputTemplate("-->");
// 20020809: browser whitespace bug
// 20040130: removed line altogether:
//           this reset the generator state, causing subsequent
//           whitespace to be ignored
//            generator_.endTemplateLine(false);
        }

        if ( dynamatorDivTagName_.equalsIgnoreCase(firstWord) )
        {
            startElement("", "", firstWord, attributes);
            currentNode_.tagName = dynamatorDivTagMarker_;
        }

// 20010128: browser whitespace bug
// 20030726: moved to here from top to allow comment to be preceded
// by whitespace if necessary
        tagPrecededByWhitespace_ = false;
    }
    
    public
    void
    startEntity(
        String name
        )
    {
        // I don't completely understand entities.
        // This works for HTML entity references only.
        // Dynamator doesn't handle entity references, it just 
        // passes them through.  FakeoutSaxInputStream converts
        // entity reference &x; into &amp;x; -- see characters().

        if ( ! inDTD_ )
        {
            currentEntity_ = name;
        }
    }

    public
    void
    endEntity(String a)
    {
// 20010128: browser whitespace bug
        tagPrecededByWhitespace_ = false;

        if ( ! inDTD_ )
        {
            currentEntity_ = null;
        }
    }

    public 
    void 
    characters(
        char[] buf, 
        int offset, 
        int len
        ) 
    {
        if ( Trace.on )
        {
            traceWhere_ = "text";
        }

// 20010128: browser whitespace bug
        tagPrecededByWhitespace_ = false;
        
        if ( currentNode_.hidesChildren )
        {
            // e.g. <option class="Discard">sample option</option>
            if ( Trace.on ) 
            {
                Trace.display(
                    "[discarding text '" 
                    + Utils.firstWord(buf, offset, len) 
                    + "...']");
            }
            return;
        }
        
        if ( currentNode_.contentElement == null 
            && currentNode_.rawContentElement == null )
        {
            if ( currentEntity_ != null )
            {
                generator_.outputTemplate('&');
                if ( ! currentEntity_.equals("amp") )
                {
                    generator_.outputTemplate(currentEntity_);
                    generator_.outputTemplate(';');
                }
            }
            else
            if ( ! inPre_ 
                && Utils.onlyWhitespace(buf, offset, len) )
            {
// 20031103: xsl text: need to retain whitespace
                tagPrecededByWhitespace_ = false;
                generator_.outputWhitespace(buf, offset, len);

// 20010128: browser whitespace bug
//                if ( Utils.contains(buf, offset, len, '\n') 
//                    || Utils.contains(buf, offset, len, '\r') )
//                {
//                    generator_.conditionallyEndTemplateLine(true);
//                }
//                else
//                {
//                    tagPrecededByWhitespace_ = true;
//                }
            }
            else
            {
                if ( Trace.on 
                    && ! Utils.onlyWhitespace(buf, offset, len) ) 
                {
                    Trace.display(
                        Utils.firstWord(buf, offset, len) + "...");
                }

                generator_.outputTemplate(buf, offset, len);
            }
        }
    }

    public 
    void 
    ignorableWhitespace(
        char[] buf,
        int offset,
        int len
        ) 
    {
        generator_.outputWhitespace(buf, offset, len);
    }

    public 
    void 
    startElement(
        String uri, 
        String local, 
        String tagName, 
        Attributes attrs
        ) 
    {
        if ( Trace.on )
        {
            traceWhere_ = "element";
            Trace.enter(
                "<" + tagName + ">"
                + ( outputting()
                    ? ""
                    : " (discarded)")
                );
        }

        currentNode_ = pushNode(tagName, attrs);

        maybeDoOverride();
            
        if ( ! currentNode_.hidesSelf
            && preformattedTags_.indexOf(
                "|" + tagName + "|") >= 0 )
        {
            generator_.setFormatting(false);
            inPre_ = true;
        }
    }

    public 
    void 
    endElement(
        String uri, 
        String local, 
        String tagName
        ) 
    {
        if ( ! tagName.equals(currentNode_.tagName) )
        {
            generator_.fatal(
                "Start/end tags don't match: "
                + "<" + currentNode_.tagName + "> ... "
                + "</" + tagName + ">");
        }
        
        outputEndTag(tagName);

        if ( ! currentNode_.hidesSelf 
            && preformattedTags_.indexOf("|" + tagName + "|") >= 0 )
        {
            generator_.setFormatting(true);
            inPre_ = false;
        }

        currentNode_ = popNode();
    }

    // end of LexicalHandler interface    
    
    private static final String valueSubstitutionFlagStart_         = "[[";
    private static final String valueSubstitutionFlagEnd_           = "]]";
    private static final char   valueSubstitutionContinuationFlag_  = '+';

    private static final int lengthOfValueSubstitutionFlagStart_ = 
        valueSubstitutionFlagStart_.length();
    private static final int lengthOfValueSubstitutionFlagEnd_ = 
        valueSubstitutionFlagEnd_.length();

    private
    String
    substituteAttributeValue(
        String value,
        String originalValue
        )
    {
        if ( originalValue == null )
        {
            originalValue = "";
        }
        
        // For now this is very simple.  
        // We can get more sophisticated later if necessary.
        // [[@]]            ==> original attribute value
        // [[@/x/y/]]       ==> original attribute value, substituting
        //                      y for x 
        //                      ('/' is the first character after the '@',
        //                      so long as it's not alphanumeric)
        //                      (last / is optional)
        // [[@/a/b/+/c/d/]] ==> multiple substitutions, performed in
        //                      order specified; + may be surrounded by
        //                      whitespace
        
        // A good enhancement would be to support '*' like 
        // '%' in GNU make's patsubst.

        String result = value;
        String first, middle, last;
        int pStart, pEnd;
        int pPos = 0;
        while ( 
            (pStart = result.indexOf(valueSubstitutionFlagStart_, pPos)) > -1
            && (pEnd = result.indexOf(valueSubstitutionFlagEnd_, pStart)) > -1
            )
        {
            first = result.substring(0, pStart);
            middle = result.substring(
                pStart + lengthOfValueSubstitutionFlagStart_, pEnd);
            last  = result.substring(
                pEnd + lengthOfValueSubstitutionFlagEnd_);
            if ( "@".equals(middle) )
            {
                middle = originalValue;
            }
            else
            if ( middle.startsWith("@") )
            {
                middle = performAttributeSubstitution(
                    originalValue, middle);
            }
            else
            {
                generator_.error(
                    "Invalid attribute substitution syntax: " 
                    + middle
                    );
                return result;
            }

            result = first + middle + last;
            pPos = first.length() + middle.length();
        }

        return result;
    }
    
    private
    String
    performAttributeSubstitution(
        String value,
        String directive
        )
    {
        String result = value;
        int start = 1;
        int lim = directive.length();

        while ( start < lim )
        {
            char delim = directive.charAt(start);
            if ( Character.isLetterOrDigit(delim) )
            {
                generator_.error(
                    "Invalid attribute substitution syntax: " 
                    + directive
                    );
                return result;
            }
    
            int pDelim = directive.indexOf(delim, 1+start);
            if ( pDelim == -1 || pDelim == 1+start)
            {
                generator_.error(
                    "Invalid attribute substitution syntax: " 
                    + directive
                    );
                return result;
            }
    
            int pEndDelim = directive.indexOf(delim, pDelim+1);
            // backward compatibility: 
            // forgive absence of end delimiter
            if ( pEndDelim == -1 )
            {
                pEndDelim = lim;
            }
            
            String find = directive.substring(1+start, pDelim);
            String replace = directive.substring(1+pDelim, pEndDelim);
            result = Utils.replace(result, find, replace);

            start = pEndDelim;
            while ( ++start < lim
                && Character.isWhitespace(directive.charAt(start)) )
            {}

            if ( start < lim
                && directive.charAt(start) 
                   == valueSubstitutionContinuationFlag_ )
            {
                while ( ++start < lim
                    && Character.isWhitespace(directive.charAt(start)) )
                {}
            }
        }

        return result;
    }
    
    private
    void
    populateAttributeOverrideDictionary(
        AttributeOverride[] overrides,
        Dictionary dictionary,
        Vector attrNames
        )
    {
        if ( overrides != null )
        {
            AttributeOverride override;
            int i = overrides.length;
            while ( --i >= 0 )
            {
                override = overrides[i];
                if ( null == dictionary.put(override.name(), override) )
                {
                    attrNames.addElement(override.name());
                }
            }
        }
    }
    
    private
    void
    outputAttribute(
        Attributes attrs,
        String name,
        Object oValue
        )
    {
        String value;
        if ( oValue.getClass() == String.class )
        {
            value = oValue.toString();
            char quote = 
                value.indexOf('"') > -1
                ? '\''
                : '"';
            generator_.outputTemplate(' ');
            generator_.outputTemplate(name);
            if ( (value != null && ! name.equals(value))
                || flagAttributes_.indexOf("|" + name + "|") 
                   == -1 )
            {
                generator_.outputTemplate('=');
                generator_.outputTemplate(quote);
// not sure why I did this; '&' is illegal in an attribute value 
// except as the start of an entity reference.
                 value = Utils.replace(value, "&amp;", "&");
                generator_.outputTemplate(value);
                generator_.outputTemplate(quote);
            }
        }
        else
        if ( oValue.getClass() == AttributeOverride.class )
        {
            AttributeOverride override = (AttributeOverride) oValue;
            if ( Trace.on )
            {
                Trace.display(
                    "[found attr override for " 
                    + override.keyType()
                    + " '"
                    + override.key()
                    + "' : attr '"
                    + name
                    + "']"
                    );
            }

            if ( override.value() != null )
            {
                if ( Trace.on )
                {
                    Trace.display(
                        "[... overriding value]");
                }
                
                value = override.value();
                if ( value.indexOf(valueSubstitutionFlagStart_) > -1 )
                {
                    value = substituteAttributeValue(value,
                        attrs.getValue(override.name()));
                } 
            }
            else
            {
                value = attrs.getValue(override.name());
            }

            boolean isFlag = 
                override.isFlag()
                || flagAttributes_.indexOf("|" + override.name() + "|") 
                   >= 0;

            if ( ( value != null || isFlag )
                 && ! override.discard() )
            {                    
                Object ifResult = null;
                if ( override.ifExpression() != null )
                {
                    if ( Trace.on )
                    {
                        Trace.display(
                            "[... adding if " 
                            + override.ifExpression() + "]");
                    }

                    ifResult =
                        generator_.startIfBlock(
                            override.ifExpression(), false);
                }

                generator_.outputTemplate(' ');
                generator_.outputTemplate(override.finalName());

                if ( ! isFlag 
                    || (value != null && ! name.equals(value)) )
                {
                    char quote = 
                        override.valueIsRaw() 
                        && value.indexOf('"') > -1
                        ? '\''
                        : '"';

                    generator_.outputTemplate('=');
                    generator_.outputTemplate(quote);

                    if ( override.value() != null )
                    {
                        if ( override.valueIsRaw() )
                        {
                            generator_.outputTemplate(value);
                        }
                        else
                        {
                            generator_.outputDynamicAttributeValue(value);
                        }
                    }
                    else
                    {
                        generator_.outputTemplate(value);
                    }

                    generator_.outputTemplate(quote);
                }
                if ( override.ifExpression() != null )
                {
                    generator_.endIfBlock(ifResult, false);
                }
            }
        }
    }
    
    // about 30% faster than dictionary in my tests (Windows/1.2.2/JIT)
    // strangely, about 20% slower without JIT
    private static
    class AttributeCompare
    implements Utils.StringCompare
    {
        static final String[] importance_ = 
            {
                // in reverse of the order they should appear
                "cols",
                "rows",
                "src",
                "object",
                "href",
                "value",
                "class",
                "id",
                "name",
                "http-equiv",
                "type"
            };

        static final int[] importanceHash_ = new int[importance_.length];
        static
        {
            int i = importance_.length;
            while ( --i >= 0 )
            {
                importanceHash_[i] = importance_[i].hashCode();
            }
        }
        
        private static
        int
        precedence(
            String s
            )
        {
            int i = importance_.length;
            int hash = s.hashCode();
            while ( --i >= 0 )
            {
                if ( hash == importanceHash_[i]
                    && s.compareTo(importance_[i]) == 0 )
                {
                    break;
                }
            }
            return i+1;
        }
        
        public
        boolean
        lessThan(
            String a,
            String b
            )
        {
            int precedenceA = precedence(a);
            int precedenceB = precedence(b);
            return precedenceA + precedenceB == 0
                ? a.compareTo(b) < 0 
                : precedenceA > precedenceB;
        }            
    }

    private static final AttributeCompare attributeCompare =
        new AttributeCompare();
    
    private
    void
    outputTag()
    {
        if ( currentNode_.hidesSelf )
        {
            if ( Trace.on )
            {
                Trace.display(
                    "[discarding tag "
                    + ( currentNode_.hidesChildren 
                        ? "and content of "
                        : "" )
                    + "<" + currentNode_.tagName + ">]");
            }
        
            return;
        }
        
        Hashtable dAttrs = new Hashtable();
        Attributes attrs = currentNode_.attrs;
        Vector vAttrNames = new Vector();

        if ( attrs != null )
        {
            String name;
            int lim = attrs.getLength();
            int iAttrs = -1;
            while ( ++iAttrs < lim )
            {
                name = attrs.getQName(iAttrs);
                dAttrs.put(name, attrs.getValue(iAttrs));
                vAttrNames.addElement(name);
            }
        }

        populateAttributeOverrideDictionary(
            currentNode_.getAttributeOverrides(),
            dAttrs,
            vAttrNames
            );
        
// 20010128: browser whitespace bug
        if ( inlineTags_.get(currentNode_.finalTagName) == null )
        {
            if ( generator_.conditionallyEndTemplateLine(false) )
            {
                tagPrecededByWhitespace_ = false;
            }
        }
            
// 20030726: browser whitespace bug, cont'd:
// ensure spaces between elements:
// </x> <x> shouldn't be rendered </x><x>

        if ( tagPrecededByWhitespace_ )
        {
            if ( generator_.inTemplateLine() )
            {
                generator_.outputTemplate(' ');
            }
            tagPrecededByWhitespace_ = false;
        }

        currentNode_.outputLineNumber = generator_.lineNumber();
        generator_.outputTemplate('<');
        generator_.outputTemplate(currentNode_.finalTagName);
        
        /*
            The purpose of attrNames is to make the regression
            tests work correctly on all platforms.  SAX does not 
            preserve attribute order, so it must be imposed.
            It might be preferable to retain the author's ordering,
            with override attributes following original attributes.
            But this isn't too bad an alternative, since it provides
            some consistency, especially wrt overrides.
        */
        int limAttrNames = vAttrNames.size();
        String[] attrNames = new String[limAttrNames];
        vAttrNames.copyInto(attrNames);
        Utils.sort(attrNames, attributeCompare);
        int i = -1;
        String attrName;
        while ( ++i < limAttrNames )
        {
            attrName = attrNames[i];
            outputAttribute(attrs, attrName, dAttrs.get(attrName));
        }

        Element rawAttrsElement = currentNode_.rawAttrsElement;
        
        if ( rawAttrsElement != null )
        {
            boolean dontAddSpace = 
                "no".equalsIgnoreCase(
                    rawAttrsElement.getAttribute("space"));
            String rawAttributes = 
                ElementUtils.getContent(
                    rawAttrsElement, true);
            if ( rawAttributes != null )
            {
                if ( Trace.on ) 
                {
                    Trace.display("[applying raw-attrs]");
                }

                if ( ! dontAddSpace )
                {
                    generator_.outputTemplate(' ');
                }

                generator_.outputTemplate(rawAttributes);
            }
        }

        if ( xhtml_ )
        {
            if ( tagsWithoutEnd_.indexOf(
                currentNode_.delimitedTagName) >= 0 )
            {
                generator_.outputTemplate(" /");
            }
        }

        generator_.outputTemplate('>');

        indentation_.increase();
    }
    
    private static
    String
    trimAnnotation(
        String value
        )
    {
return value;
// broke java program output
//        int pStart = -1;
//        while ( value.charAt(++pStart) == '\n' 
//            || value.charAt(pStart) == '\r' )
//        {}
//
//        int pEnd = value.length();
//        while ( value.charAt(--pEnd) == ' '
//            || value.charAt(pEnd) == '\t' )
//        {}
//        
//        return value.substring(pStart, pEnd+1);
    }
    
    private
    void
    maybeOverrideBefore()
    {
        Element beforeElement = currentNode_.beforeElement;

        if ( beforeElement == null )
        {
            return;
        }

        if ( Trace.on ) 
        {
            Trace.display("[applying <before>]");
        }

        generator_.conditionallyEndProgramLine();
// 20030830: ensure neutral state for non-template languages
// 20020809: browser whitespace bug
        generator_.conditionallyEndTemplateLine(false);

        generator_.outputRaw(
            trimAnnotation(
                ElementUtils.getContent(beforeElement, false)));
// 20020809: browser whitespace bug
//        generator_.conditionallyEndTemplateLine(false);

        String doIndent = beforeElement.getAttribute("indent");
        if ( doIndent.equalsIgnoreCase("yes") )
        {
            indentation_.increase();
            currentNode_.isManuallyIndentedBefore = true;
        }

        String doProgramIndent = 
            beforeElement.getAttribute("indent-program");
        if ( doProgramIndent.equalsIgnoreCase("yes") )
        {
            generator_.increaseProgramIndentation();
            currentNode_.programIsManuallyIndentedBefore = true;
        }
    }
    
    private
    boolean             // false if node is to be discarded
    maybeOverrideIf()
    {
        Element ifElement = currentNode_.ifElement;

        if ( ifElement == null )
        {
            return true;
        }

        String ifExpression = ElementUtils.getContent(ifElement, true);

        if ( ifExpression.equals("false") )
        {
            currentNode_.hidesSelf = true;
            currentNode_.hidesChildren = true;
            if ( Trace.on ) 
            {
                Trace.display("[discarding tag and content of <" 
                    + currentNode_.tagName +">]");
            }
            return false;
        }
        else
        {
            currentNode_.hasIfBlock = true;
            if ( Trace.on ) 
            {
                Trace.display("[applying <if>" 
                    + ifExpression + "</if>]");
            }
// 20020809: browser whitespace bug
// changed 'true' to 'false'
            currentNode_.ifResult = 
                generator_.startIfBlock(ifExpression, false);
        }

        return true;
    }
    
    private
    void
    maybeOverrideForeach()
    {
        Element foreachElement = currentNode_.foreachElement; 

        if ( foreachElement == null )
        {
            return;
        }

        currentNode_.enclosesForeachTag = 
            exclusiveIterationTags_.indexOf(
                currentNode_.delimitedTagName) >= 0;
        if ( currentNode_.enclosesForeachTag )
        {
            outputTag();
        }

        String collectionExpression =
            Utils.nullIfEmpty(
                ElementUtils.getTextContent(foreachElement, true));

        if ( collectionExpression == null )
        {
            collectionExpression = 
                Utils.nullIfEmpty(
                    ElementUtils.getSingleContent(
                        foreachElement, "collection"));
        }

        String name = foreachElement.getTagName();
        if ( name.equals("for") )
        {
            if ( Trace.on ) 
            {
                Trace.display(
                    "[applying <for>" 
                    + currentNode_.foreachExpression 
                    + "]");
            }
        
            currentNode_.foreachExpression = 
                generator_.startForBlock(
                    collectionExpression);

            currentNode_.foreachBlockType = FOREACH_TYPE_FOR;
        }
        else
        if ( collectionExpression != null )
        {
            String iName = 
                Utils.nullIfEmpty(foreachElement.getAttribute("i"));

            String type = 
                Utils.nullIfEmpty(foreachElement.getAttribute("type"));

            String collectionName =
                Utils.nullIfEmpty(
                    foreachElement.getAttribute("collection"));
            
            String elementName = 
                Utils.nullIfEmpty(
                    foreachElement.getAttribute("element"));
            
            if ( Trace.on ) 
            {
                Trace.display(
                    "[applying <foreach>" 
                    + currentNode_.foreachExpression 
                    + "]");
            }

            currentNode_.foreachExpression = 
                generator_.startCollectionIterationBlock(
                    collectionExpression, type, elementName, iName, 
                    collectionName);

            currentNode_.foreachBlockType = FOREACH_TYPE_COLLECTION;
        }
        else
        {
            String i = 
                Utils.nullIfEmpty(
                    foreachElement.getAttribute("i"));
            String first = 
                Utils.nullIfEmpty(
                    foreachElement.getAttribute("init"));
            if ( first == null )
            {
                first = 
                    Utils.nullIfEmpty(
                        foreachElement.getAttribute("first"));
            }
            String last = 
                Utils.nullIfEmpty(
                    foreachElement.getAttribute("compare"));
            if ( last == null )
            {
                last = 
                    Utils.nullIfEmpty(
                        foreachElement.getAttribute("last"));
            }
            String step = 
                Utils.nullIfEmpty(
                    foreachElement.getAttribute("step"));

            if ( first != null && last != null )
            {
                generator_.warning(
                    "Sequenced foreach is deprecated... use <for>");
                
                if ( Trace.on ) 
                {
                    Trace.display(
                        "[applying <foreach>" 
                        + currentNode_.foreachExpression + "]");
                }

                currentNode_.foreachExpression = 
                    generator_.startSequencedIterationBlock(
                        i, first, last, step);
    
                currentNode_.foreachBlockType = FOREACH_TYPE_SEQUENCE;
            }
            else
            {
                generator_.error(
                    "Incomplete 'foreach': must specify a"
                    + " collection or sequence");
            }
        }

        currentNode_.foreachIfExpression =
            Utils.nullIfEmpty(
                ElementUtils.getSingleContent(foreachElement, "if"));

        if ( currentNode_.foreachIfExpression != null )
        {
            currentNode_.foreachIfResult = 
                generator_.startIfBlock(
                    currentNode_.foreachIfExpression, true);
        }
    }
    
    private
    void
    maybeOverrideBeforeContent()
    {
        Element beforeContentElement = 
            currentNode_.beforeContentElement;

        if ( beforeContentElement == null )
        {
            return;
        }

        if ( Trace.on ) 
        {
            Trace.display("[applying <before-content>]");
        }
        generator_.conditionallyEndProgramLine();
// 20020809: browser whitespace bug
// Why newline == false: Otherwise the client can't eliminate
// all whitespace before the inserted content.
        generator_.conditionallyEndTemplateLine(false);
        generator_.outputRaw(
            trimAnnotation(
                ElementUtils.getContent(
                    beforeContentElement, false)));
        String doIndent = 
            beforeContentElement.getAttribute("indent");
        if ( doIndent.equalsIgnoreCase("yes") )
        {
            indentation_.increase();
            currentNode_.isManuallyIndented = true;
        }

        String doProgramIndent = 
            beforeContentElement.getAttribute("indent-program");
        if ( doProgramIndent.equalsIgnoreCase("yes") )
        {
            generator_.increaseProgramIndentation();
            currentNode_.programIsManuallyIndented = true;
        }
    }
    
    private
    void
    maybeOverrideContent()
    {
        if ( currentNode_.contentElement != null )
        {
            if ( Trace.on ) 
            {
                Trace.display("[applying <content>]");
            }
            generator_.outputDynamicValueExpression(
                ElementUtils.getContent(currentNode_.contentElement, false));
        }
        else
        if ( currentNode_.rawContentElement != null )
        {
            if ( Trace.on ) 
            {
                Trace.display("[applying <raw-content>]");
            }

// 20030830: ensure neutral state for non-template languages
            generator_.conditionallyEndProgramLine();
// 20020809: browser whitespace bug
            generator_.conditionallyEndTemplateLine(false);

            // output Template is not used because the content may 
            // include program statements.  However, the content may 
            // also just be static text.

            generator_.outputRaw(
                ElementUtils.getContent(
                    currentNode_.rawContentElement, false));
        }
    }
    
    private
    void
    startExtract(
        Element extractElement
        )
    {
        generator_.conditionallyEndProgramLine();
        generator_.conditionallyEndTemplateLine(false);
        generatorStack_.push(generator_);

        generator_.outputRaw(
            ElementUtils.getTextContent(extractElement, false));

        String filename = extractElement.getAttribute("to-file");

        if ( filename == null || filename.length() == 0 )
        {
            if ( Trace.on )
            {
                Trace.enter(
                    "extracting to bottom of output file"
                    );
            }

            OutputStream output = new ByteArrayOutputStream(4096);
            extracts_.addElement(output);
            generator_ = Generator.makeGenerator(
                annotations_.getLanguage(),
                output,
                generator_.fileName(), 
                options_
                );
        }
        else
        {
            if ( ! Utils.filenameIsAbsolute(filename) )
            {
                filename = Utils.pathTo(generator_.fileName())
                    + '/' + filename;
            }
                
            if ( Trace.on )
            {
                Trace.enter(
                    "extracting to file " + filename
                    );
            }

            try
            {
                generator_ = Generator.makeGenerator(
                    annotations_.getLanguage(),
                    new FileOutputStream(filename), 
                    filename, 
                    options_
                    );
            }
            catch(IOException x)
            {
                throw new FatalError(
                    "Invalid extract destination file name" 
                    + filename);
            }
            generator_.start(options_.produceGenerationComment);
        }
    }
    
    private 
    void
    endExtract(
        Element extractElement
        )
    {
        if ( extractElement.getAttribute("to-file") != null )
        {
            generator_.end(options_.produceGenerationComment);
        }
        generator_.close();
        generator_ = (Generator) generatorStack_.pop();
    }
    
    private
    void
    maybeDoOverride()
    {
        if ( currentNode_.hiddenByParent )
        {
            // don't override anything if this element is a child
            // of an element whose content has been discarded
            return;
        }
        
        if ( currentNode_.extractElement != null )
        {
            startExtract(currentNode_.extractElement);
        }
        
        maybeOverrideBefore();

        if ( ! maybeOverrideIf() )
        {
            return;
        }

        maybeOverrideForeach();

        if ( ! currentNode_.enclosesForeachTag )
        {
            outputTag();
        }

        maybeOverrideBeforeContent();

        maybeOverrideContent();
    }
    
    private
    void
    outputEndTag(
        String tagName
        )
    {
        if ( currentNode_.hiddenByParent )
        {
            if ( Trace.on )
            {
                traceWhere_ = "element";
                Trace.leave("</" + tagName + "> (discarded)");
            }

            return;
        }
        
        if ( currentNode_.foreachBlockType != FOREACH_TYPE_NONE
            && currentNode_.enclosesForeachTag )
        {
            if ( currentNode_.foreachIfExpression != null )
            {
                generator_.endIfBlock(
                    currentNode_.foreachIfResult, true);
            }
            
            if ( currentNode_.foreachBlockType == FOREACH_TYPE_COLLECTION )
            {
                generator_.endCollectionIterationBlock(
                    currentNode_.foreachExpression);
            }
            else
            if ( currentNode_.foreachBlockType == FOREACH_TYPE_SEQUENCE )
            {
                generator_.endSequencedIterationBlock(
                    currentNode_.foreachExpression);
            }
            else
            if ( currentNode_.foreachBlockType == FOREACH_TYPE_FOR )
            {
                generator_.endForBlock(
                    currentNode_.foreachExpression);
            }
        }
        
        if ( currentNode_.isManuallyIndented )
        {
            indentation_.decrease();
        }

        if ( currentNode_.programIsManuallyIndented )
        {
            generator_.decreaseProgramIndentation();
        }

        if ( currentNode_.afterContentElement != null )
        {
            if ( Trace.on ) 
            {
                Trace.display("[applying <after-content>]");
            }

            generator_.conditionallyEndProgramLine();
// 20020809: browser whitespace bug
            generator_.conditionallyEndTemplateLine(false);
            generator_.outputRaw(
                trimAnnotation(
                    ElementUtils.getContent(
                        currentNode_.afterContentElement, false)));
        }        
        
        if ( currentNode_.hidesSelf )
        {
            if ( Trace.on )
            {
                traceWhere_ = "element";
                Trace.leave("</" + tagName + ">");
            }
        }
        else
        {
            String delimitedTag = currentNode_.delimitedTagName;
            boolean tagShouldHaveEnd = 
                tagsWithoutEnd_.indexOf(delimitedTag) < 0;

            indentation_.decrease();

// 20010128: browser whitespace bug
            if ( tagShouldHaveEnd 
                && huggingEndTags_.indexOf(delimitedTag) >= 0 )
            {
                if ( tagPrecededByWhitespace_ )
                {
                    generator_.outputTemplate(' ');
                }
            }
// 20010128: browser whitespace bug
            else
            if ( tagShouldHaveEnd 
                && optionalInlineTags_.indexOf(delimitedTag) >= 0 )
            {
                // Block tag; newline is permitted
                generator_.conditionallyEndTemplateLine();
            }
            else            
            if ( ! inPre_
                && currentNode_.outputLineNumber != generator_.lineNumber() 
                && inlineTags_.get(tagName) == null )
            {
// 20040130: whitespace: added 'false':
//           XML processing shouldn't add whitespace
                generator_.conditionallyEndTemplateLine(false);
            }

            if ( tagShouldHaveEnd )
            {
                generator_.outputTemplate("</");
                generator_.outputTemplate(currentNode_.finalTagName);
                generator_.outputTemplate('>');
                if ( Trace.on )
                {
                    traceWhere_ = "element";
                    Trace.leave("</" + tagName + ">");
                }
            }
            else
            {
                if ( Trace.on )
                {
                    traceWhere_ = "element";
                    Trace.leave();
                }
            }
        }

        if ( currentNode_.foreachBlockType != FOREACH_TYPE_NONE
            && ! currentNode_.enclosesForeachTag )
        {
            if ( currentNode_.foreachIfExpression != null )
            {
                generator_.endIfBlock(
                    currentNode_.foreachIfResult, true);
            }
            
            if ( currentNode_.foreachBlockType == FOREACH_TYPE_COLLECTION )
            {
                generator_.endCollectionIterationBlock(
                    currentNode_.foreachExpression);
            }
            else
            if ( currentNode_.foreachBlockType == FOREACH_TYPE_SEQUENCE )
            {
                generator_.endSequencedIterationBlock(
                    currentNode_.foreachExpression);
            }
            else
            if ( currentNode_.foreachBlockType == FOREACH_TYPE_FOR )
            {
                generator_.endForBlock(
                    currentNode_.foreachExpression);
            }
        }
        
        if ( currentNode_.hasIfBlock )
        {
            generator_.endIfBlock(currentNode_.ifResult, false);
        }

        if ( currentNode_.isManuallyIndentedBefore )
        {
            indentation_.decrease();
        }

        if ( currentNode_.programIsManuallyIndentedBefore )
        {
            generator_.decreaseProgramIndentation();
        }

        if ( currentNode_.afterElement != null )
        {
            if ( Trace.on ) 
            {
                Trace.display("[applying <after>]");
            }

            generator_.conditionallyEndProgramLine();
// 20020809: browser whitespace bug
            generator_.conditionallyEndTemplateLine(false);
            generator_.outputRaw(
                trimAnnotation(
                    ElementUtils.getContent(
                        currentNode_.afterElement, false)));
        }

        if ( currentNode_.extractElement != null )
        {
            endExtract(currentNode_.extractElement);
        }

// 20010128: browser whitespace bug
        tagPrecededByWhitespace_ = false;
    }
}
