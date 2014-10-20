package org.codehaus.plexus.components.io.dirscanner;
/*
 * Copyright 2014 The Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import junit.framework.TestCase;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirectoryScannerTest
    extends TestCase
{

    public void testScan()
        throws Exception
    {

        File basedir = new File("src/test/resources/directorywalker");

        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(  basedir );
        List<PlexusIoResource>  result = new ArrayList<PlexusIoResource>();

        ds.scan( DirectoryScanner.listConsumer( result ));

        System.out.println( "result = " + result );

        org.codehaus.plexus.util.DirectoryScanner oldds = new org.codehaus.plexus.util.DirectoryScanner();
        oldds.setBasedir(  basedir );
        oldds.scan();
        final String[] includedFiles = oldds.getIncludedFiles();
        final String[] includedDirectories = oldds.getIncludedDirectories();
        System.out.println( " = " + includedFiles + includedDirectories);


    }

    public void testTokenize(){
        final char[][] chars = DirectoryScanner.tokenizePathToString( "ab.def.hsig", '.' );
        assertEquals( "ab", new String(chars[0]));
        assertEquals( "def", new String(chars[1]));
        assertEquals( "hsig", new String(chars[2]));
        assertEquals( 3, chars.length );
    }

    public void testTokenize2(){
        final String path = "ab.def..hsig.";
        final char[][] chars = DirectoryScanner.tokenizePathToString( path, '.' );
        final String[] o = DirectoryScanner.tokenizePathToString( path, "." );
        assertEquals( "ab", new String( chars[0] ) );
        assertEquals( "def", new String(chars[1]));
        assertEquals( "hsig", new String(chars[2]));
        assertEquals( 3, chars.length);
    }

    public void testTokenize3(){
        final String path = "..ab.def..hsig";
        final char[][] chars = DirectoryScanner.tokenizePathToString( path, '.' );
        final String[] o = DirectoryScanner.tokenizePathToString( path, "." );
        assertEquals( "ab", new String( chars[0] ) );
        assertEquals( "def", new String(chars[1]));
        assertEquals( "hsig", new String(chars[2]));
        assertEquals( 3, chars.length);
    }

}