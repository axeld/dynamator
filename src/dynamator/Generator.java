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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Hashtable;

import org.xml.sax.Locator;

/**
    Outputs merged file in target language.
    A specialized concrete class is required for each target language.
    <p>
    From the perspective of this class, the output file has two kinds
    of lines: template lines and program lines.  A template line is a
    line from the original template, or a line consisting of raw
    content from the annotations file.  Depending on the target language,
    it may be wrapped in something like an 'out.println'.  A program
    line is a line of program logic from the dynamator annotations
    file.
**/
public abstract
class Generator
implements Cloneable
{
//    private static final String rcsID_ = 
//        "$Id: Generator.java,v 1.10 2004/03/23 07:36:49 jaydunning Exp $";

    private PrintWriter out_;
    private PrintWriter saveOut_ = null;
    private OutputStream rawOutputStream_ = null;
    private ErrorHandler errorHandler_;
    private Locator locator_;

    private String fileName_;
    private String encoding_;

    private boolean formatting_ = true;
    private boolean expectsIndent_ = true;

    // if outputLineNumber_ of a start tag was different from that of 
    // the corresponding end tag, a newline is emitted before the end
    // tag; otherwise the end tag is emitted on the same line
    private int lineNumber_ = 0;

    protected Options options_;
    protected Indentation indentation_;
    
    // public for debugging purposes
    public Writer debugWriter_ = null;

    protected PrintStream errorStream_;

    private static Hashtable prototypes_ = new Hashtable();
    
    /**
        Factory method to create a Generator.
        <p>
        This method instantiates and initializes a class named
        'dynamator.<i>language</i>.Generator', where <i>language</i> is
        the language argument.
    **/
    public static
    Generator
    makeGenerator(
        String language,
        OutputStream rawOutputStream,
        String fileName,
        Options options
        )
    {
        Generator result = makeInstance(language);

        result.init(rawOutputStream, fileName, options);
        result.init();
        return result;
    }
    
    private static
    Generator
    makeInstance(
        String language
        )
    {
        Generator result;
        
        synchronized( prototypes_ )
        {
            result = (Generator) prototypes_.get(language);
            if ( result == null )
            {
                try   
                {
                    result = (Generator) 
                        Class.forName(
                            "dynamator." + language + ".Generator")
                        .newInstance();

                    prototypes_.put(language, result);
                }
                catch (ClassNotFoundException x)
                {
                    throw new FatalError(
                        "Language not supported: '" + language + "'");
                }
                catch (IllegalAccessException x)
                {
                    throw new FatalError("Configuration error: ", x);
                }
                catch (InstantiationException x)
                {
                    throw new FatalError("Configuration error", x);
                }
            }
        }

        return (Generator) result.clone();
    }
    
    /**
        Default constructor for generators.
        Must be followed by a call to 
        {@link 
        #init(java.io.OutputStream,java.lang.String,dynamator.Options)}.
        Clients should use makeGenerator() instead.
    **/
    public
    Generator()
    {}
    
    /**
        Clone is used only for prototypes, which have null values for
        object attributes.
    **/
    public
    Object
    clone()
    {
        try
        {
            return super.clone();
        }
        catch(CloneNotSupportedException x)
        {
            throw new FatalError(
                "Generator prototype could not be cloned: " 
                + x.toString());
        }
    }
    
    /**
        Required initialization for generators.
    **/
    protected
    void
    init(
        OutputStream rawOutputStream,
        String fileName,
        Options options
        )
    {
        fileName_    = fileName;
        options_     = options;
        errorStream_ = options.errorStream;
        indentation_ = options.indentation;
        encoding_    = options.encoding;
        rawOutputStream_ = rawOutputStream;
        try
        {
            out_ = outputWriter(rawOutputStream, encoding_);
        }
        catch(UnsupportedEncodingException x)
        {
            throw new FatalError(
                "Unsupported encoding: " + encoding_);
        }
    }

    /**
        Optional initialization for generators.
        Invoked after mandatory initialization.
    **/
    protected
    void
    init()
    {}

    protected
    PrintWriter
    outputWriter(
        OutputStream rawOutputStream,
        String encoding
        )
    throws UnsupportedEncodingException
    {
        return new PrintWriter(
            new BufferedWriter(
                new OutputStreamWriter(
                    rawOutputStream,
                    encoding
                    )
                )
            );
    }
    
// Debugging is probably not correct wrt encoding
    public
    void
    startDebug()
    {
        if ( debugWriter_ != null )
        {
            throw new IllegalStateException(
                "Generator is already in debug mode");
        }
        debugWriter_ = new StringWriter();
        saveOut_ = out_;
        out_ = new PrintWriter(debugWriter_);
    }
    
    public
    void
    stopDebug()
    {
        if ( debugWriter_ == null )
        {
            throw new IllegalStateException(
                "Generator is not in debug mode");
        }

        out_.flush();
        out_.close();
        
        PrintWriter out = null;
        try
        {
            out = new PrintWriter(
                new OutputStreamWriter(
                    new FileOutputStream(
                        fileName_),
                    encoding_));
            out.print(debugWriter_.toString());
        }
        catch (Exception x)
        {
            errorStream_.println(x.toString());
        }
        finally
        {
            if ( out != null )
            {
                out.flush();
                out.close();
            }
        }


        out_ = saveOut_;
        saveOut_ = null;
    }
    
    public
    void
    close()
    {
        conditionallyEndProgramLine();
        conditionallyEndTemplateLine(true);
        ++lineNumber_;
        if ( debugWriter_ != null )
        {
            stopDebug();
        }
        out_.flush();
        out_.close();
    }
    
    public
    void
    flush()
    {
        out_.flush();
    }

    /**
        The primitive output stream.
    **/
    public
    OutputStream
    rawOutputStream()
    {
        return rawOutputStream_;
    }

//======================================================================
    
    private static final char MODE_TEMPLATE = 'T';
    private static final char MODE_PROGRAM  = 'P';
    private static final char MODE_NEUTRAL  = ' ';
    private char mode_ = MODE_NEUTRAL;
        // must avoid generating a newline
        // at top of program-generated file

    // Have to expose inTemplateLine(boolean) so that jsp class can
    // properly indicate end of scriptlet
    // and for DynamateXml
    public
    boolean
    inTemplateLine()
    {
        return mode_ == MODE_TEMPLATE;
    }

    protected
    void
    inTemplateLine(
        boolean value
        )
    {
        mode_ = value ? MODE_TEMPLATE : MODE_NEUTRAL;
    }

    protected
    boolean
    inProgramLine()
    {
        return mode_ == MODE_PROGRAM;
    }

    protected
    void
    inProgramLine(
        boolean value
        )
    {
        mode_ = value ? MODE_PROGRAM : MODE_NEUTRAL;
    }

    public
    void
    outputRaw(
        char value
        )
    {
        out_.print(value);
    }
    
    public
    void
    outputRaw(
        String value
        )
    {
        out_.print(value);
    }
    
    public
    void
    outputRaw(
        char[] buf,
        int offset,
        int length
        )
    {
        out_.write(buf, offset, length);
    }
    
    public
    void
    startTemplateLine()
    {
        if ( inProgramLine() )
        {
            endProgramLine();
        }
        
        if ( formatting_ && expectsIndent_ )
        {
            out_.print(indentation_.current());
        }
        inTemplateLine(true);
    }
    
    /**
        Assumes current line is a template line
    */
    public
    void
    endTemplateLine()
    {
        endTemplateLine(true);
    }
    
    /**
        Assumes current line is a template line
    */
    public
    void
    endTemplateLine(
        boolean newline
        )
    {
        if ( newline )
        {
            out_.println();
            ++lineNumber_;
        }
        inTemplateLine(false);
        expectsIndent_ = newline;
    }
    
    /**
        True if template line was ended.
    **/
    public
    boolean
    conditionallyEndTemplateLine()
    {
        return conditionallyEndTemplateLine(true);
    }
    
    /**
        True if template line was ended.
    **/
    public
    boolean
    conditionallyEndTemplateLine(
        boolean newline
        )
    {
        if ( inTemplateLine() )
        {
            endTemplateLine(newline);
            return true;
        }
        return false;
    }

    /**
        Print the beginning of a new program line.
        Default behavior is to print the indentation if formatting is on.
    **/
    public
    void
    startProgramLine()
    {
        if ( inTemplateLine() )
        {
            endTemplateLine();
        }
        else
        if ( inProgramLine() )
        {
            endProgramLine();
        }

        if ( formatting_ )
        {
            out_.print(indentation_.current());
        }

        inProgramLine(true);
    }

    /**
        Print the end of the current program line.
        Default behavior is to print a newline.
    **/
    public
    void
    endProgramLine()
    {
        out_.println();

        inProgramLine(false);
    }
   
    public
    void
    conditionallyEndProgramLine()
    {
        if ( inProgramLine() )
        {
            endProgramLine();
        }
    }

    public
    void
    nextProgramLine()
    {
        endProgramLine();
        startProgramLine();
    }

    public
    boolean // contents were output
    outputWhitespace(
        char[] buf,
        int offset,
        int length
        )
    {
        /*
            For HTML: 
            
            '\n'?   Indent?   Subsequent '<'?   Output
            -----   -------   ---------------   ---------------
            T       T         T                 '\n' 
            T       T         F                 doesn't matter
            T       F         T                 '\n'
            T       F         F                 doesn't matter
            F       T         T                 input
            F       T         F                 input
            F       F         T                 input
            F       F         F                 input

            In cases 1 and 3, output just the newline because otherwise
            indentation will be too much.
            
            For non-HTML, the subclass can override.
        */
        
        // For regression testing to succeed,
        // behavior must not be dependent on XML parser.
        // Unfortunately, XML parsers are free to handle
        // whitespace any way they want: one character 
        // at a time, all together, or anything in between.
        //
        // An example of the effect of whitespace reduction
        // is the Java Calendar example, which discards all but one of
        // each element.  When whitespace is rendered faithfully,
        // this results in a whole bunch of indented newlines, because
        // the whitespace _around_ the discarded elements is not 
        // also discarded.
        //
        // I'll probably never figure out the whitespace issue to my
        // satisfaction.  For now, I'm picking whitespace reduction
        // over cross-parser consistency.

        if ( Utils.contains(buf, offset, length, '\n') 
            || Utils.contains(buf, offset, length, '\r') )
        {
            conditionallyEndTemplateLine(true);
        }
        else
        {
            outputTemplate(buf, offset, length);
        }

        return true;
    }
    
    public
    void
    outputTemplate(
        char c
        )
    {
        if ( inProgramLine() )
        {
            endProgramLine();
        }
        
        if ( ! inTemplateLine() )
        {
            startTemplateLine();
        }

        if ( c == '\n' 
            || c == '\r' )
        {
            endTemplateLine();
        }
        else
        {
            out_.print(c);
        }
    }
    
    public
    void
    outputTemplate(
        char[] buf,
        int offset,
        int length
        )
    {
        while ( --length >= 0 )
        {
            outputTemplate(buf[offset++]);
        }
    }

    public
    void
    outputTemplate(
        String value
        )
    {
        if ( value.indexOf('\n') >= 0 
            || value.indexOf('\r') >= 0 )
        {
            outputTemplate(value.toCharArray(), 0, value.length());
        }
        else
        {
            if ( inProgramLine() )
            {
                endProgramLine();
            }
            
            if ( ! inTemplateLine() )
            {
                startTemplateLine();
            }

            out_.print(value);
        }
    }

    /**
        Output an XML element beginning with '&lt;!'.
    */
    public
    void
    outputXmlEscape(
        String elementText
        )
    {
        outputTemplate(elementText);
        endTemplateLine();
    }
    
    /**
        Output an XML processing instruction (syntax &lt;?...?&gt;)

        @param elementText 
        The content of the processing instruction tag, not including
        the &lt;? or ?&gt;
    */
    public
    void
    outputProcessingInstruction(
        String elementText
        )
    {
        outputTemplate("<?");
        outputTemplate(elementText);
        outputTemplate("?>");
    }
    
    /**
        Output an XML CDATA section (syntax &lt;![CDATA[...]]&gt;)

        @param sectionText 
        The entire CDATA section, including the &lt;![CDATA[ and the ]]&gt;.
    */
    public
    void
    outputCDATA(
        String sectionText
        )
    {
        // Whitespace is significant within CDATA
        
        indentation_.off();

        outputTemplate(sectionText);

        indentation_.on();
    }
    
    /**
        Output a sequence of program lines.  Only used for
        'before', 'before-content', 'raw-content', 'after-content', and
        'after'. 
    */
    public
    void
    outputProgram(  
        char[] buf,
        int offset,
        int length
        )
    {
        if ( inTemplateLine() )
        {
            endTemplateLine();
        }
        if ( ! inProgramLine() )
        {
            startProgramLine();
        }

        char c;
        while ( --length >= 0 )
        {
            c = buf[offset];
            if ( c == '\n' 
                || c == '\r' )
            {
                endProgramLine();
                startProgramLine();
            }
            else
            {
                out_.print(c);
            }
            ++offset;
        }
    }
    
    /**
        Output a sequence of program lines, ignoring leading and
        trailing blank lines.  Only used for
        'before', 'before-content', 'raw-content', 'after-content', and
        'after'. 
    */
    public
    void
    outputProgram(
        String value
        )
    {
        outputProgram(value.toCharArray(), 0, value.length());
    }

//    protected static
//    String
//    removeSurroundingBlankLines(
//        String value
//        )
//    {
//        int prevNewline = -1;
//        char c;
//        int i = -1;
//        while ( ++i < value.length() 
//            && Character.isWhitespace(c = value.charAt(i)) )
//        {
//            if ( c == '\n' 
//                || c == '\r' ) 
//            {
//                prevNewline = i;
//            }
//        }
//        if ( prevNewline > -1 )
//        {
//            value = value.substring(prevNewline + 1);
//        }
//
//        prevNewline = -1;
//        i = value.length();
//        while ( --i >= 0
//            && Character.isWhitespace(c = value.charAt(i)) )
//        {
//            if ( c == '\n' 
//                || c == '\r' )
//            {
//                prevNewline = i;
//            }
//        }
//        if ( prevNewline > -1 )
//        {
//            value = value.substring(0, prevNewline);
//        }
//
//        return value;
//    }

    // ------------------------------------------------------------
    // Other services
    // ------------------------------------------------------------

    /**
        The writer for the output file.
    **/
    /* package */ protected final 
    PrintWriter
    writer()
    {
        return out_;
    }
    
    /**
        Report a warning.  Execution continues and output is assumed to
        be acceptable.
    **/
    protected
    void
    warning(
        String message
        )
    {
        errorHandler_.warning(message, locator_);
    }
    
    /**
        Report an error.  Execution continues but output is assumed to
        be unacceptable.
    **/
    protected
    void
    error(
        String message
        )
    {
        errorHandler_.error(message, locator_);
    }
    
    /**
        Report a fatal error.  Execution terminates immediately.
    **/
    protected
    void
    fatal(
        String message
        )
    {
        errorHandler_.fatal(message, locator_);
    }
    
    /**
        For the parser.
    **/
    /* package */
    void
    setErrorHandler(
        ErrorHandler errorHandler
        )
    {
        errorHandler_ = errorHandler;
    }

    /**
        For the parser.
    **/
    /* package */
    void
    setLocator(
        Locator locator
        )
    {
        locator_ = locator;
    }

    /**
        The name of the output file.
    **/
    public
    String
    fileName()
    {
        return fileName_;
    }
    
    /**
        The output file encoding.
    **/
    public
    String
    encoding()
    {
        return encoding_;
    }
    
    /**
        The current relative line number.
        Only used to determine placement of an end tag:
        if the current line number is the same as the line number when
        the start tag was emitted, the end tag is placed on the same
        line; otherwise it is placed on a new line.
    **/
    public
    int
    lineNumber()
    {
        return lineNumber_;
    }
    
    /**
        Increment the relative line number, forcing the matching end tag
        for a previous tag to be placed on a new line.
    **/
    protected
    void
    incrementLineNumber()
    {
        ++lineNumber_;
    }

    /**
        Whether output is indented to match XML nesting level.
    **/
    protected
    boolean
    formatting()
    {
        return formatting_;
    }
    
    /**
        Indicate whether output should be indented to match XML nesting
        level.
    **/
    public
    void
    setFormatting(
        boolean value
        )
    {
        formatting_ = value;
    }
    
    /**
        Increase the level of program indentation.
        Only effective for non-template languages like java.
    **/
    public
    void
    increaseProgramIndentation()
    {}
    
    /**
        Decrease the level of program indentation.
        Only effective for non-template languages like java.
    **/
    public
    void
    decreaseProgramIndentation()
    {}
    
    /**
        Emit the code required at the beginning of a program file
        (before the prolog).
    **/
    public abstract
    void
    start(
        boolean produceGenerationComment
        );
    
    /**
        Emit the code required at the end of a program file.
        The default method does nothing.
    **/
    public
    void
    end(
        boolean produceGenerationComment
        )
    {}
    
    /**
        Emit the code required to output a programming language expression.
        May only occur within a template line.
        
        @param value
            The programming language expression.
    **/
    public abstract
    void
    outputDynamicValueExpression(
        String value
        );

    /**
        Emit the code required to output a programming language
        expression within an attribute value.
        May only occur within a template line.
        
        @param value
            The programming language expression.
    **/
    public
    void
    outputDynamicAttributeValue(
        String value
        )
    {
        outputDynamicValueExpression(value);
    }

    /**
        Emit the code required at the beginning of an if block.

        @param expression
            The content of the &lt;if&gt; element.
        @param newline
            True if the if-block should cause a newline.

        @return 
            An object to be passed to endIfBlock().  May be null.
    **/
    public abstract
    Object
    startIfBlock(
        String expression,
        boolean newline
        );

    /**
        Emit the code required at the end of an if block.

        @param startResult
            The return value from the corresponding startIfBlock().
        @param forElement
            True if the if-block controls an element.
            False if the if-block controls an attribute.
    **/
    public abstract
    void
    endIfBlock(
        Object startResult,
        boolean forElement
        );
        
    /**
        Emit the code required at the beginning of a collection
        iteration block.

        @param collectionExpression
            The content of the &lt;foreach&gt; tag.
            Always present.
        @param collectionType
            The type of the collection.
            Defined for each output format.
            Optional (may be required for typesafe languages and
            optional for typeless languages).
        @param elementName
            The name of the variable that represents the current object
            of the collection for each iteration.
            Optional.
        @param iName
            The name of the variable that represents the current
            iteration number.
            Optional.
        @param collectionName
            The name of the variable that references the collection.
            Optional.
            
        @return
            An object to be passed to endCollectionIterationBlock().
            May be null.
    **/
    public abstract
    Object
    startCollectionIterationBlock(
        String collectionExpression,
        String collectionType, 
        String elementName,
        String iName,
        String collectionName
        );
    
    /**
        Emit the code required at the end of a collection iteration block.

        @param startResult
            The return value from the corresponding
            startCollectionIterationBlock(). 
    **/
    public abstract
    void
    endCollectionIterationBlock(
        Object startResult
        );

    /**
        <b>This method has been deprecated in favor of startForBlock, 
        and will be removed in a subsequent release.</b>
        
        Emit the code required at the beginning of a sequenced
        iteration block.   Corresponds to the C pattern
        'for (<i>first</i>; <i>last</i>; <i>step</i>.)' or the VB pattern
        'for <i>i</i> = <i>first</i> to <i>last</i> step <i>step</i>'.

        @param i
            The name of the iteration variable.
        @param first
            The value of the iteration variable for the first pass of
            the iteration.
            Always present.
        @param last
            The value of the iteration variable for the last pass of
            the iteration.
            Always present.
        @param step
            The program language expression invoked at the end of each
            iteration to advance to the next value.
            Optional, depending on language.
            
        @return
            An object to be passed to endSequencedIterationBlock().
            May be null.
    **/
    public abstract
    Object
    startSequencedIterationBlock(
        String i,
        String first,
        String last, 
        String step
        );
    
    /**
        <b>This method has been deprecated in favor of endForBlock, 
        and will be removed in a subsequent release.</b>
        
        Emit the code required at the end of a sequenced iteration block.

        @param startResult
            The return value from the corresponding
            startSequencedIterationBlock(). 
    **/
    public abstract
    void
    endSequencedIterationBlock(
        Object startResult
        );

    /**
        Emit the code required at the beginning of a for block.
        Corresponds to the C pattern 
        <p align="center"><code>
        for (first; last; step)
        </code></p>        
        or the VB pattern
        <p align="center"><code>
        for i = first to last step inc
        </code></p>        
        In the Dynamator file these would be expressed as
        <p align="center"><code>
        &lt;for&gt;(first; last; step)&lt;/for&gt;
        </code></p>        
        or 
        <p align="center"><code>
        &lt;for&gt;i = first to last step inc&lt;/for&gt;
        </code></p>        
        

        @param expression
            The text after the for.
            
        @return
            An object to be passed to endForBlock().
            May be null.
    **/
    public abstract
    Object
    startForBlock(
        String expression
        );
    
    /**
        Emit the code required at the end of a for block.

        @param startResult
            The return value from the corresponding
            startForBlock(). 
    **/
    public abstract
    void
    endForBlock(
        Object startResult
        );
}
