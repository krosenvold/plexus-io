/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.codehaus.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact codehaus@codehaus.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.codehaus.org/>.
 */

package org.codehaus.plexus.components.io.dirscanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.codehaus.plexus.components.io.attributes.Java7Reflector;
import org.codehaus.plexus.components.io.functions.PlexusIoResourceConsumer;
import org.codehaus.plexus.components.io.resources.Java7FileResource;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;
import org.codehaus.plexus.util.Java7FileUtil;

/**
 * Class for scanning a directory for files/directories which match certain
 * criteria.
 * <p/>
 * These criteria consist of selectors and patterns which have been specified.
 * With the selectors you can select which files you want to have included.
 * Files which are not selected are excluded. With patterns you can include
 * or exclude files based on their filename.
 * <p/>
 * The idea is simple. A given directory is recursively scanned for all files
 * and directories. Each file/directory is matched against a set of selectors,
 * including special support for matching against filenames with include and
 * and exclude patterns. Only files/directories which match at least one
 * pattern of the include pattern list or other file selector, and don't match
 * any pattern of the exclude pattern list or fail to match against a required
 * selector will be placed in the list of files/directories found.
 * <p/>
 * When no list of include patterns is supplied, "**" will be used, which
 * means that everything will be matched. When no list of exclude patterns is
 * supplied, an empty list is used, such that nothing will be excluded. When
 * no selectors are supplied, none are applied.
 * <p/>
 * The filename pattern matching is done as follows:
 * The name to be matched is split up in path segments. A path segment is the
 * name of a directory or file, which is bounded by
 * <code>File.separator</code> ('/' under UNIX, '\' under Windows).
 * For example, "abc/def/ghi/xyz.java" is split up in the segments "abc",
 * "def","ghi" and "xyz.java".
 * The same is done for the pattern against which should be matched.
 * <p/>
 * The segments of the name and the pattern are then matched against each
 * other. When '**' is used for a path segment in the pattern, it matches
 * zero or more path segments of the name.
 * <p/>
 * There is a special case regarding the use of <code>File.separator</code>s
 * at the beginning of the pattern and the string to match:<br>
 * When a pattern starts with a <code>File.separator</code>, the string
 * to match must also start with a <code>File.separator</code>.
 * When a pattern does not start with a <code>File.separator</code>, the
 * string to match may not start with a <code>File.separator</code>.
 * When one of these rules is not obeyed, the string will not
 * match.
 * <p/>
 * When a name path segment is matched against a pattern path segment, the
 * following special characters can be used:<br>
 * '*' matches zero or more characters<br>
 * '?' matches one character.
 * <p/>
 * Examples:
 * <p/>
 * "**\*.class" matches all .class files/dirs in a directory tree.
 * <p/>
 * "test\a??.java" matches all files/dirs which start with an 'a', then two
 * more characters and then ".java", in a directory called test.
 * <p/>
 * "**" matches everything in a directory tree.
 * <p/>
 * "**\test\**\XYZ*" matches all files/dirs which start with "XYZ" and where
 * there is a parent directory called test (e.g. "abc\test\def\ghi\XYZ123").
 * <p/>
 * Case sensitivity may be turned off if necessary. By default, it is
 * turned on.
 * <p/>
 * Example of usage:
 * <pre>
 *   String[] includes = {"**\\*.class"};
 *   String[] excludes = {"modules\\*\\**"};
 *   ds.setIncludes(includes);
 *   ds.setExcludes(excludes);
 *   ds.setBasedir(new File("test"));
 *   ds.setCaseSensitive(true);
 *   ds.scan();
 *
 *   System.out.println("FILES:");
 *   String[] files = ds.getIncludedFiles();
 *   for (int i = 0; i < files.length; i++) {
 *     System.out.println(files[i]);
 *   }
 * </pre>
 * This will scan a directory called test for .class files, but excludes all
 * files in all proper subdirectories of a directory called "modules"
 *
 * @author Arnout J. Kuiper
 *         <a href="mailto:ajkuiper@wxs.nl">ajkuiper@wxs.nl</a>
 * @author Magesh Umasankar
 * @author <a href="mailto:bruce@callenish.com">Bruce Atherton</a>
 * @author <a href="mailto:levylambert@tiscali-dsl.de">Antoine Levy-Lambert</a>
 */
public class DirectoryScanner
    extends AbstractScanner
{

    /**
     * The base directory to be scanned.
     */
    private File basedir;

    /**
     * Whether or not symbolic links should be followed.
     *
     * @since Ant 1.5
     */
    private boolean followSymlinks = true;


    private final String[] tokenizedEmpty = tokenizePathToString( "", File.separator );

    /**
     * Sole constructor.
     */
    public DirectoryScanner()
    {
    }

    public static String[] tokenizePathToString( String path, String separator )
    {
        List<String> ret = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer( path, separator );
        while ( st.hasMoreTokens() )
        {
            ret.add( st.nextToken() );
        }
        return ret.toArray( new String[ret.size()] );
    }

    public static char[][] tokenizePathToString( String path,  char separator )
    {
        final char[] src = path.toCharArray();
        int found = 0;
        for ( char c : src )
        {
            if ( c == separator) found++;
        }
        char[][] result = new char[found+1][];

        int off = 0;
        int pos = 0;
        char c;
        StringBuilder sb = new StringBuilder(  );
        while ( pos < path.length()){
            c = src[pos++];
            if (c == separator){
                if (sb.length() > 0) {
                    result[off++] = sb.toString().toCharArray();
                    sb.setLength( 0 );
                }
                while ( pos < (path.length()-1) && src[pos + 1] == separator){
                    pos++;
                }
            } else {
                sb.append( c );
            }
        }
        if (sb.length() > 0) {
            result[off++] = sb.toString().toCharArray();
        }
        if ( off <= found ){
            char[][] result2 = new char[off][];
            System.arraycopy( result, 0, result2, 0, off  );
            return result2;

        }
        return result;
    }

    /**
     * Sets the base directory to be scanned. This is the directory which is
     * scanned recursively.
     *
     * @param basedir The base directory for scanning.
     *                Should not be <code>null</code>.
     */
    public void setBasedir( File basedir )
    {
        this.basedir = basedir;
    }

    /**
     * Sets whether or not symbolic links should be followed.
     *
     * @param followSymlinks whether or not symbolic links should be followed
     */
    public void setFollowSymlinks( boolean followSymlinks )
    {
        this.followSymlinks = followSymlinks;
    }

    /**
     * Scans the base directory for files which match at least one include
     * pattern and don't match any exclude patterns. If there are selectors
     * then the files must pass muster there, as well.
     *
     * @throws IllegalStateException if the base directory was set
     *                               incorrectly (i.e. if it is <code>null</code>, doesn't exist,
     *                               or isn't a directory).
     */
    public void scan(PlexusIoResourceConsumer consumer)
        throws IllegalStateException, IOException
    {
        if ( basedir == null )
        {
            throw new IllegalStateException( "No basedir set" );
        }
        if ( !basedir.exists() )
        {
            throw new IllegalStateException( "basedir " + basedir + " does not exist" );
        }
        if ( !basedir.isDirectory() )
        {
            throw new IllegalStateException( "basedir " + basedir + " is not a directory" );
        }

        setupDefaultFilters();
        setupMatchPatterns();

        if ( isIncluded( "", tokenizedEmpty ) )
        {
            if ( !isExcluded( "", tokenizedEmpty ) )
            {
                consumer.accept( new Java7FileResource( new File( "" ), "", "" ) );
            }
        }
        scandir( basedir, "", consumer );
    }

    /**
     * gets resources relative to a supplied directory
     *
     * @param vpath
     * @param dir The dir to scan, may be relative or absolute
     * @return An array of resources.
     * @throws IOException
     */
    private Java7FileResource[] getFiles( String vpath, File dir )
        throws IOException
    {
        String[] newfiles = dir.list();
        if ( newfiles == null )
        {
            newfiles = new String[0];
        }
        final int numFiles = newfiles.length;
        Java7FileResource[] result = new Java7FileResource[numFiles];
        for (int i = 0; i < numFiles; i++){
            result[i] = new Java7FileResource( dir, vpath, newfiles[i] );
        }
        return result;
    }

    /**
     * Scans the given directory for files and directories. Found files and
     * directories are placed in their respective collections, based on the
     * matching of includes, excludes, and the selectors.  When a directory
     * is found, it is scanned recursively.
     *
     *
     * @param dir   The directory to scan. Must not be <code>null</code>.
     * @param vpath The path relative to the base directory (needed to
     *              prevent problems with an absolute path when using
     *              dir). Must not be <code>null</code>.
     * @param consumer
     */
    private void scandir( File dir, String vpath, PlexusIoResourceConsumer consumer )
        throws IOException
    {
        Java7FileResource[] newfiles = getFiles( vpath, dir );

        if ( !followSymlinks )
        {
            newfiles = rewriteSymlinks( dir, newfiles );
        }

        // dir forandrer seg, peker alltid på det vi scanner
        // vpath er sub-path, dvs offset fra utgangspunkt
        // name er det vi har, relativt til dir

        for ( Java7FileResource newfile : newfiles )
        {
            String subVpath = newfile.asVpath();
            String[] tokenizedName =  tokenizePathToString( subVpath, File.separator );
            if ( newfile.isDirectory() )
            {
                if ( isIncluded( subVpath, tokenizedName ) )
                {
                    if ( !isExcluded( subVpath, tokenizedName ) )
                    {
                        consumer.accept( newfile );
                        scandir( newfile.getFile(), newfile.asVpath(), consumer );
                    }
                    else
                    {
                        if ( couldHoldIncluded( subVpath ) )
                        {
                            scandir( newfile.getFile(), newfile.asVpath(), consumer );
                        }
                    }
                }
                else
                {
                    if ( couldHoldIncluded( subVpath ) )
                    {
                        scandir( newfile.getFile(), newfile.asVpath(), consumer );
                    }
                }
            }
            else if ( newfile.isFile() )
            {
                if ( isIncluded( subVpath, tokenizedName ) )
                {
                    if ( !isExcluded( subVpath, tokenizedName ) )
                    {
                        consumer.accept( newfile );
                    }
                }
            }
        }
    }

    private Java7FileResource[] rewriteSymlinks( File dir, Java7FileResource[] newfiles )
    {
        ArrayList<Java7FileResource> noLinks = new ArrayList<Java7FileResource>();
        for ( Java7FileResource newfile : newfiles )
        {
            try
            {
                if ( !isParentSymbolicLink( dir, newfile ) )
                {
                    noLinks.add( newfile );
                }
            }
            catch ( IOException ioe )
            {
                String msg = "IOException caught while checking " + "for links, couldn't get cannonical path!";
                // will be caught and redirected to Ant's logging system
                System.err.println( msg );
                noLinks.add( newfile );
            }
        }
        newfiles = noLinks.toArray(new Java7FileResource[noLinks.size()]);
        return newfiles;
    }

    /**
     * Checks whether the parent of this file is a symbolic link.
     * <p/>
     *
     * <p> For java versions prior to 7 It doesn't really test for
     * symbolic links but whether the
     * canonical and absolute paths of the file are identical - this
     * may lead to false positives on some platforms.</p>
     *
     * @param parent the parent directory of the file to test
     * @param name   the name of the file to test.
     * @return true if it's a symbolic link
     * @throws java.io.IOException .
     * @since Ant 1.5
     */
    private boolean isParentSymbolicLink( File parent, PlexusIoResource name )
        throws IOException
    {
        if ( Java7Reflector.isAtLeastJava7() )
        {
            return Java7FileUtil.isSymLink( parent );
        }
        File resolvedParent = new File( parent.getCanonicalPath() );
        File toTest = new File( resolvedParent, name.getName() );
        return !toTest.getAbsolutePath().equals( toTest.getCanonicalPath() );
    }

    public static PlexusIoResourceConsumer listConsumer(final List<PlexusIoResource> target){
        return new PlexusIoResourceConsumer()
        {
            public void accept( PlexusIoResource resource )
            {
                target.add( resource);
            }
        };
    }
}
