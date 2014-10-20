package org.codehaus.plexus.components.io.resources;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;

import javax.annotation.Nonnull;

import org.codehaus.plexus.components.io.functions.PlexusIoResourceAttributeSupplier;

public class Java7FileResource
    implements PlexusIoResource
{
    private final File baseDir;

    private final String fileName;

    private final File file;

    private final String name;

    private final long lastModified, size;

    private final boolean isFile, isDirectory, isExisting;

    private final boolean isSymbolicLink;

    private final String vpath;

    private final String vpathName;

    public Java7FileResource( File basedir, String vpath, String fileName )
        throws IOException
    {
        this.baseDir = basedir;
        this.vpath = vpath;
        this.fileName = fileName;
        this.file = new File( basedir, fileName);
        this.name = getName( this.file );
        this.isExisting = file.exists();
        this.vpathName = vpath + fileName;
        if (isExisting)
        {
            final BasicFileAttributes attrs = Files.readAttributes( file.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS );
            this.size = attrs.size();
            this.isFile = attrs.isRegularFile();
            this.isDirectory = attrs.isDirectory();
            this.isSymbolicLink = attrs.isSymbolicLink();
            this.lastModified = attrs.lastModifiedTime().toMillis();
        } else {
            this.size = -1;
            this.isFile = false;
            this.isDirectory = false;
            this.isSymbolicLink = false;
            this.lastModified = -1;
        }
    }

    public String getName()
    {
        return vpathName;
    }

    public long getSize()
    {
        return size;
    }

    public boolean isDirectory()
    {
        return isDirectory;
    }

    public boolean isExisting()
    {
        return isExisting;
    }

    public boolean isFile()
    {
        return isFile;
    }

    /**
     * Creates a new instance. This constructor is usually used with a directory
     */
    private static String getName( File file, String fileName )
    {
        File tmp = new File( file, fileName);
        return tmp.getPath().replace( '\\', '/' );
    }

    private static String getName( File tmp )
    {
        return tmp.getPath().replace( '\\', '/' );
    }

    /**
     * Returns the resources file.
     */
    public File getFile()
    {
        return file;
    }


    /*
    The filename (without paths)
     */
    public String getFileName()
    {
        return fileName;
    }

    @Nonnull public InputStream getContents()
        throws IOException
    {
        return new FileInputStream( getFile() );
    }

    public URL getURL()
        throws IOException
    {
        return getFile().toURI().toURL();
    }

    public long getLastModified()
    {
        return  lastModified;
    }

    public boolean isSymbolicLink()
    {
        return isSymbolicLink;
    }

    @Override
    public String toString()
    {
        return file.getPath();
    }

    public String asVpath(){
        return getName() + File.separator;
    }

    public PlexusIoResource withAttributes(PlexusIoResourceAttributeSupplier attributes){
        return new Proxy(this, attributes);
    }
}
