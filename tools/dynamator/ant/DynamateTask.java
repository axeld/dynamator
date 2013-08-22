/***********************************************************************
*   Copyright 2002-2004 by Jay Dunning.
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

package dynamator.ant;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import dynamator.Trace;
import dynamator.Utils;

/** 
    <p>
    <b>Description</b>
    </p>
    <p>
    Ant task to Dynamate an XML or HTML file, creating a server page
    or program.
    </p>
    <p>
    <b>Parameters</b>
    </p>
    <table border="1" cellspacing="0">
    <tr>
      <th>attribute</th>
      <th>description</th>
      <th>default value</th>
      <th>required?</th>
    </tr>
    <tr>
      <td>destdir</td>
      <td>The root directory for output files.
        If this value begins with an asterisk, the remaining value is
        appended to the name of the directory in which the input file
        is found. 
      </td>
      <td>&nbsp;</td>
      <td>required</td>
    </tr>
    <tr>
      <td>destsuffix</td>
      <td>The suffix Dynamator is expected to give to
         output files.  This value is only used to locate the
         output file.  The file suffix is determined by the 
         &lt;dynamator&gt; tag within the Dynamator file.
      </td>
      <td>&nbsp;</td>
      <td>required</td>
    </tr>
    <tr>
      <td>dynfile</td>
      <td>The Dynamator file to be applied to argument template files.
          Overrides file name matching.
      </td>
      <td>&nbsp;</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>dynsuffix</td>
      <td>The suffix Dynamator is expected to use for Dynamator
          files.
      </td>
      <td>dyn</td>
      <td>required</td>
    </tr>
    <tr>
      <td>srcdir</td>
      <td>The root directory for Dynamator argument files.
          This directory is used to locate files identified with the
          <tt>includes</tt> attribute.  Usually, this is the directory
          that contains template files (e.g. html or xml).
      </td>
      <td>&nbsp;</td>
      <td>required</td>
    </tr>
    <tr>
      <td>matchdir</td>
      <td>The root directory for files to be matched to Dynamator
          argument files.  If argument files are template files, then
          this is the directory that contains Dynamator files.  If
          argument files are Dynamator files, this is the directory
          that contains template files.
      </td>
      <td>srcdir</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>encoding</td>
      <td>Use the Java character encoding specified to read the 
         input file.
      </td>      
      <td>ASCII</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>excludes</td>
      <td>Comma-separated list of filepath patterns to be excluded
        during template file selection.
        If omitted, all files under srcdir are included.
      </td>
      <td>&nbsp;</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>force</td>
      <td>'true' to cause Dynamator to rebuild all files regardless of
          their timestamps.
      </td>      
      <td>false</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>gencomment</td>
      <td>'false' to omit the file generation comment from the output
          file. 
      </td>      
      <td>true</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>ignoreasxml</td>
      <td>'true' to always convert HTML to XML, even if the asxml
         file is newer than the HTML file.
      </td>      
      <td>false</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>html</td>
      <td>'true' to treat all template files as HTML.
          If not specified, files having a suffix other than 
          ".html" or ".htm" are treated as XML.
      </td>      
      <td>false</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>xml</td>
      <td>'true' to treat all template files as XML.
          If not specified, files having a suffix 
          ".html" or ".htm" are treated as HTML.
      </td>      
      <td>false</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>bodyonlyhtml</td>
      <td>'true' to process template files as Body-Only HTML.
        This option generates server page files that output HTML
        fragments to be inserted into the body of the page.
        The template file is processed by JTidy, but the
        &lt;html&gt;, &lt;/html&gt;, &lt;body&gt;, and &lt;/body&gt;,
        tags and the &lt;head&gt; element are all removed.
      </td>      
      <td>false</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>includeds</td>
      <td>Comma-separated list of filepath patterns of dynamator files
          included using Dynamator's include mechanism.  These files
          are assumed to reside under 'matchdir' (which means that this
          option won't work when dynamator files are in 'srcDir'; I'll
          figure that out later).  
          If specified, target files are rebuilt if they are older than
          any of these files.
        <p>
        Example:
        <code>*</code><code>*</code><code>/*.dyninclude</code>
        </p>
      </td>
      <td>&nbsp;</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>includes</td>
      <td>Comma-separated list of filepath patterns to be included
        during argument file selection.  These are usually template
        file names.
        If omitted, all files under srcdir are included.
        <p>
        Example:
        <code>*</code><code>*</code><code>/*.html</code>
        </p>
      </td>
      <td>&nbsp;</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>includepath</td>
      <td>A colon- or semicolon-separated list of directories 
          to be searched for included files.
      </td>
      <td>&nbsp;</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>includepathref</td>
      <td>A reference to a path specifying the directories
          to be searched for included files.
      </td>
      <td>&nbsp;</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>indent</td>
      <td>'false' to not indent output.
      </td>      
      <td>true</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>pretend</td>
      <td>'true' to cause no action to be taken.
          Use with 'ant -v' to report what would have been done
          (like <tt>make -n</tt>).
      </td>      
      <td>false</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>reportversion</td>
      <td>'true' to report the Dynamator version.
      </td>      
      <td>false</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>stripcomments</td>
      <td>'true' to remove all HTML or XML comments from the generated
          file. 
      </td>      
      <td>false</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>templatedir</td>
      <td>Directory containing templates referred to in the dynamator
      tag's template attribute.
      </td>      
      <td>&nbsp;</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>trace</td>
      <td>'true' to output an execution trace.  Any other value is used
          as a filename for trace output.
      </td>      
      <td>false</td>
      <td>optional</td>
    </tr>
    <tr>
      <td>validate</td>
      <td>'true' to validate Dynamator files; false to process without
          validation.
      </td>      
      <td>true</td>
      <td>optional</td>
    </tr>
<!--
  Verbosity controlled through the environment, but
  no way to get the verbosity from the environment
    
    <tr>
      <td>verbose</td>
      <td>
        DEPRECATED: use 'ant -v'
        'true' to report dynamate command.
      </td>      
      <td>false</td>
      <td>optional</td>
    </tr>
-->
    </table>
    <p>
    <b>Parameters specified as nested elements</b>
    </p>
    <p>
    <b>includepath</b>
    </p>
    <p>
    The 'includepath' attribute is a path-like structure and may be set
    using a nested 'includepath' element.
    </p>
    <p>
    <b>Behavior</b>
    </p>
    <ol>
      <li>The Dynamate task searches the templates directory tree for
          all files under <tt>srcdir</tt> matching the
          <tt>includes</tt> pattern.</li>
      <li>For each template file found, the task looks for a matching file in
          the corresponding directory under <tt>matchdir</tt>, with the
          suffix ".dyn".  Template files that do not have matching .dyn
          files are excluded from further processing.</li>
      <li>For each match found, the task looks for a target file under
          <tt>destdir</tt>, with the suffix specified in
          <tt>destsuffix</tt>.</li>
      <li>If the target file is not found, Dynamator is executed for that
          file.</li>
      <li>If the target file is found, the timestamps of all three
          files (template, .dyn, and destination) are compared.  If
          either the template file or the .dyn file have a newer
          timestamp than the destination file, Dynamator is executed
          for that file.</li>
    </ol>
    <p>
    <b>Example</b>
    </p>
    <pre>&lt;taskdef name="dynamate" classname="dynamator.ant.DynamateTask"/&gt;
...
&lt;target name="create-jsp"&gt;
  &lt;dynamate
      srcdir="html"
      destdir="htdocs"
      matchdir="dyn"
      destsuffix="jsp"
      includes="&#42;&#42;/&#42;.html"
  /&gt;
&lt;/target&gt;</pre>
    <p>
    Dynamates each file <code>html/&#42;&#42;/x.html</code> that has a matching
    file <code>dyn/&#42;&#42;/x.dyn</code>, placing the output in 
    <code>htdocs/&#42;&#42;/x.jsp</code>.
    </p>
**/
public
class DynamateTask
extends MatchingTask
{
    // required attributes
    private File matchDir_ = null;
    private File srcDir_ = null;
    private File destDir_ = null;
    private String destDirPrefix_ = null;
    private String destDirSuffix_ = null;
    private String destSuffix_ = null;
    
    // optional attributes
    private boolean pretend_ = false;
    private String dynSuffix_ = "dyn";
    private String dynFileName_ = null;
    private Path includePath_ = null;
    private boolean force_ = false;
    private FileSet includeds_ = null;

    protected Vector toBuild_ = new Vector();
    
    private final Vector optionalArguments_ = new Vector();

    public void setDestdir(String value)
    { 
        if ( value.charAt(0) == '*' )
        {
            destDirSuffix_ = value.substring(1);
        }
        else
        if ( value.charAt(value.length()-1) == '*' )
        {
            destDirPrefix_ = value.substring(0, value.length()-1);
        }
        else
        {
            destDir_ = project.resolveFile(value); 
        }
    }

    public void setSrcdir(String value)
    { 
        srcDir_ = project.resolveFile(value); 
        if ( matchDir_ == null )
        {
            matchDir_ = srcDir_;
        }
    }

    public void setMatchdir(String value)
    { matchDir_ = project.resolveFile(value); }
    
    public void setDestsuffix(String value)
    { destSuffix_ = value; }

    public void setDynfile(String value)
    { 
        dynFileName_ = value;
    }
    
    public void setDynsuffix(String value)
    { dynSuffix_ = value; }
    
    public void setEncoding(String value)
    { optionalArguments_.addElement("-e" + value); }
    
    public void setIgnoreasxml(boolean value)
    { 
        if ( value )
        {
            optionalArguments_.addElement("-a"); 
        }
    }
    
    public void setGencomment(boolean value)
    { 
        if ( ! value )
        {
            optionalArguments_.addElement("-G"); 
        }
    }
    
    public void setTemplatedir(String value)
    {
        if ( value != null && value.length() > 0 )
        {
            optionalArguments_.addElement(
                "-t" + project.resolveFile(value));
        }
    }
    
    public void setTrace(String value)
    { 
        // To prevent the output file being overwritten for
        // each file, Trace behavior is controlled from here.
        
        if ( "true".equalsIgnoreCase(value) 
            || "yes".equalsIgnoreCase(value)
            || "on".equalsIgnoreCase(value) )
        {
            Trace.on = true;
        }
        else
        if ( "false".equalsIgnoreCase(value) 
            || "no".equalsIgnoreCase(value) 
            || "off".equalsIgnoreCase(value) )
        {
            Trace.on = false;
        }
        else
        {
            Trace.on = true;
            try
            {
                Trace.open(value);
            }
            catch(IOException x)
            {
                throw new BuildException(
                    "Could not open trace file ("
                    + x.toString() 
                    + ')');
            }
        }
    }

    public void setReportversion(boolean value)
    { 
        if ( value )
        {
            optionalArguments_.addElement("-v"); 
        }
    }

    public void setStripcomments(boolean value)
    { 
        if ( value )
        {
            optionalArguments_.addElement("-C"); 
        }
    }
    
    public void setBodyonlyhtml(boolean value)
    { 
        if ( value )
        {
            optionalArguments_.addElement("-B"); 
        }
    }
    
    public void setHtml(boolean value)
    { 
        if ( value )
        {
            optionalArguments_.addElement("-H"); 
        }
    }
    
    public void setXml(boolean value)
    { 
        if ( value )
        {
            optionalArguments_.addElement("-X"); 
        }
    }
    
    public void setIndent(boolean value)
    { 
        if ( ! value )
        {
            optionalArguments_.addElement("-N"); 
        }
    }

    public void setForce(boolean value)
    { 
        if ( value )
        {
            force_ = true;
        }
    }
    
    public void setVerbose(boolean value)
    {
//        verbosity_ =
//            value
//            ? Project.MSG_VERBOSE
//            : Project.MSG_WARN;
    }
    
    public void setValidate(boolean value)
    { 
        if ( ! value )
        {
            optionalArguments_.addElement("-V"); 
        }
    }
    
    public void setDebug(boolean value)
    { 
//        verbosity_ =
//            value
//            ? Project.MSG_DEBUG
//            : Project.MSG_WARN;
    }
    
    public void setPretend(boolean value)
    { 
        if ( value )
        {
            pretend_ = true;
        }
    }
    
    public void setIncludeds(String includes)
    {
        if ( includeds_ == null )
        {
            includeds_ = new FileSet();
        }
        includeds_.setIncludes(includes);
    }
    
    public void setIncludepath(Path value)
    {
        if ( includePath_ == null )
        {
            includePath_ = value;
        }
        else
        {
            includePath_.append(value);
        }
    }

    public
    Path
    createIncludepath()
    {
        if ( includePath_ == null )
        {
            includePath_ = new Path(project);
        }
        return includePath_.createPath();
    }
    
    public
    void
    setIncludepathref(Reference r)
    {  createIncludepath().setRefid(r); }
    
    public
    void
    execute()
    throws BuildException
    {
        checkInput();

        String[] filesToProcess = findFilesToProcess();

        if ( filesToProcess.length == 0 )
        {
            return;
        }
        
        log("Dynamating " + filesToProcess.length + " file"
            + (filesToProcess.length == 1 ? "" : "s"), Project.MSG_INFO);

        int optionalArguments_Size = optionalArguments_.size();
        int includePathSize = includePath_ == null ? 0 : includePath_.size();
        String[] argv = new String[
            optionalArguments_Size
            + includePathSize
            + ( dynFileName_ == null ? 0 : 1 )
            + 2                         // for destdir and matchdir
            + 1                         // for file
            ];
        int iSource = -1;
        int iArgv = -1;
        while ( ++iSource < optionalArguments_Size )
        {
            argv[++iArgv] = (String) optionalArguments_.elementAt(iSource);
        }
        if ( includePath_ != null )
        {
            String[] includePath = includePath_.list();
            iSource = -1;
            while ( ++iSource < includePathSize )
            {
                argv[++iArgv] = "-I" + includePath[iSource];
            }
        }

        if ( dynFileName_ != null )
        {
            argv[++iArgv] = "-F" + dynFileName_;
        }
        
        int successful = 0;
        int i = -1;
        while ( ++i < filesToProcess.length )
        {
            if ( processFile(argv, filesToProcess[i]) )
            {
                ++successful;
            }
        }

        if ( successful != filesToProcess.length )
        {
            int unsuccessful = filesToProcess.length - successful;
            throw new BuildException(
                "" + unsuccessful + " files not processed");
        }
    }        

    protected
    void
    checkInput()
    {
        if ( srcDir_ == null )
        {
            throw new BuildException("srcdir must be specified");
        }
        if ( matchDir_ == null )
        {
            throw new BuildException("matchdir must be specified");
        }
        if ( destDir_ == null 
            && destDirPrefix_ == null 
            && destDirSuffix_ == null)
        {
            throw new BuildException("destdir must be specified");
        }
        if ( ! srcDir_.exists() )
        {
            throw new BuildException("srcdir not found: '" + srcDir_ + "'");
        }
        if ( ! matchDir_.exists() )
        {
            throw new BuildException("matchdir not found: '" + matchDir_ + "'");
        }
        if ( destDir_ != null && ! destDir_.exists() )
        {
            throw new BuildException("destdir not found: '" + destDir_ + "'");
        }
    }
    
    protected
    String[]
    findFilesToProcess()
    {
        Vector vResult = new Vector();
        
        // 1.  When was the most recent included file last modified?
        long mostRecentIncluded = mostRecent(includeds_);
                
        // 2.  Search templates directory tree for all files under
        //     'srcdir' matching the 'includes/excludes' pattern.
        String[] srcNames = getDirectoryScanner(srcDir_).getIncludedFiles();

        String srcfilename;
        String srcfileprefix;
        String dynfilename;
        String destfilename;
        File srcfile;
        File dynfile;
        File destfile;
        int i = -1;
        while ( ++i < srcNames.length )
        {
            srcfilename = srcNames[i];

            srcfile = new File(srcDir_, srcfilename);
            
            // if Dynamator argument file is a Dynamator file, 
            // require processing, since I don't want to open 
            // it to find the template file name.
            if ( srcfilename.endsWith(dynSuffix_) )
            {
                vResult.addElement(srcfilename);
                continue;
            }
            
            srcfileprefix = srcfilename.substring(
                0, srcfilename.lastIndexOf(".")+1);

            if ( dynFileName_ != null )
            {
                if ( Utils.filenameIsAbsolute(dynFileName_) )
                {
                    dynfile = new File(dynFileName_);
                }
                else
                {
                    dynfile = new File(matchDir_, dynFileName_);
                }
                
                if ( ! dynfile.exists() )
                {
                    throw new BuildException(
                        "dynfile not found: " 
                        + dynfile.toString()
                        );
                }
            }
            else
            {
                dynfilename = srcfileprefix + dynSuffix_;
                dynfile = new File(matchDir_, dynfilename);

                // 3.  Unless .dyn file is pre-specified, 
                //     exclude any template file found that does not
                //     have a matching .dyn file under 'matchdir'.
                if ( ! dynfile.exists() )
                {
                    log("Discarding " + srcfile + ": "
                        + "dynfile not found: " + dynfile.toString(), 
                        Project.MSG_DEBUG);
                    continue;
                }
            }

            if ( ! force_ )
            {
                // 3.  Look for target file in 'destdir'
                if ( destDirPrefix_ != null )
                {
                    destfilename = destDirPrefix_ + srcfileprefix + destSuffix_;
                }
                else
                if ( destDirSuffix_ != null )
                {
                    int p = srcfileprefix.lastIndexOf('/');
                    if ( p == -1 )
                    {
                        p = srcfileprefix.lastIndexOf('\\');
                    }
                    if ( p >= 0 )
                    {
                        destfilename = 
                            srcfileprefix.substring(0, p)
                            + destDirSuffix_
                            + srcfileprefix.substring(p)
                            + destSuffix_;
                    }
                    else
                    {
                        destfilename = 
                            destDirSuffix_ + srcfileprefix + destSuffix_;
                    }
                }
                else
                {
                    destfilename = srcfileprefix + destSuffix_;
                }
    
                if ( destDir_ != null )
                {
                    destfile = new File(destDir_, destfilename);
                }
                else
                {
                    destfile = new File(destfilename);
                }

                if ( destfile.exists() )
                {
                    // 4.  Compare timestamps ...
                    if ( srcfile.lastModified() > destfile.lastModified()
                        || dynfile.lastModified() > destfile.lastModified() 
                        || mostRecentIncluded > destfile.lastModified()
                       )
                    {
                        vResult.addElement(srcfilename);
                    }
                    else
                    {
                        log(
                            "Discarding " + srcfile + ": "
                            + "destfile newer...", Project.MSG_DEBUG);
                        log(
                            "     [" + dynfile.lastModified() + "]: "
                            + dynfile.toString(), Project.MSG_DEBUG);
                        log(
                            "     [" + srcfile.lastModified() + "]: "
                            + srcfile.toString(), Project.MSG_DEBUG);
                        log(
                            "     [" + destfile.lastModified() + "]: "
                            + destfile.toString(), Project.MSG_DEBUG);
                    }
                }
                else
                {
                    //     ... require compile if target doesn't exist
                    vResult.addElement(srcfilename);
                }
            }
            else
            {
                //     ... require compile if force
                vResult.addElement(srcfilename);
            }
        }

        String[] result = new String[vResult.size()];
        vResult.copyInto(result);
        
        return result;
    }

    protected
    boolean
    processFile(
        String[] argv,
        String srcfilename
        )
    {
        String srcpath = Utils.pathTo(srcfilename.replace('\\','/'));
        String slashsrcpath = ( srcpath.length() > 0 )
            ? File.separator + srcpath
            : srcpath;

        String destDir;

        if ( destDirPrefix_ != null )
        {
            destDir = destDirPrefix_  + slashsrcpath;
        }
        else
        if ( destDirSuffix_ != null )
        {
            destDir = srcDir_ + File.separator + srcpath + destDirSuffix_;
        }
        else
        {
            destDir = destDir_  + slashsrcpath;
        }

        {
            File fDestDir = new File(destDir);
            if ( ! fDestDir.exists() )
            {
                fDestDir.mkdirs();
            }
        }

        argv[argv.length-3] = "-d" + destDir;
        argv[argv.length-2] = "-f" + matchDir_ + slashsrcpath;
        argv[argv.length-1] = 
            srcDir_.getAbsolutePath() + File.separator + srcfilename;

        {
            StringBuffer buf = new StringBuffer();
            buf.append("dynamate ");
            int i = -1;
            while ( ++i < argv.length )
            {
                buf.append(argv[i]);
                buf.append(" ");
            }
            log(buf.toString(), Project.MSG_VERBOSE);
        }

        boolean result = true;

        if ( ! pretend_ )
        {
            result = dynamator.dynamate.invoke(argv, System.err);

            if ( result )
            {
                log(argv[argv.length-1] + " processed successfully", 
                    Project.MSG_VERBOSE);
            }
            else
            {
                log(argv[argv.length-1] + " had errors", 
                    Project.MSG_ERR);
            }
        }

        return result;
    }

    private
    long
    mostRecent(
        FileSet fileset
        )
    {
        long result = 0;

        if ( fileset == null )
        {
            return result;
        }

        // for now, assume that directory for includeds is same as matchdir
        
        File file;

        // a kludge until I can figure out the right thing to do
        fileset.setDir(matchDir_);

        String[] filenames = 
            fileset.getDirectoryScanner(project).getIncludedFiles();
        int i = filenames.length;
        while ( --i >= 0 )
        {
            file = new File(matchDir_, filenames[i]);
            result = Math.max(file.lastModified(), result);
        }

        return result;
    }
}
