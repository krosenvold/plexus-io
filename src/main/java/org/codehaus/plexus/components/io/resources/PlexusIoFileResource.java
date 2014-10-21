package org.codehaus.plexus.components.io.resources;

/*
 * Copyright 2007 The Codehaus Foundation.
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

import javax.annotation.Nonnull;

import org.codehaus.plexus.components.io.attributes.Java7AttributeUtils;
import org.codehaus.plexus.components.io.attributes.Java7Reflector;
import org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributes;

/**
 * Implementation of {@link PlexusIoResource} for files.
 */
public class PlexusIoFileResource
    extends AbstractPlexusIoResource
    implements PlexusIoResourceWithAttributes
{
    @Nonnull
    private final File file;

    @Nonnull
    private final PlexusIoResourceAttributes attributes;

    /**
     * Creates a new instance. This constructor is usually used with a directory
     */
    private PlexusIoFileResource( @Nonnull File file )
    {
        this( file, getName( file ), null );
    }

    private PlexusIoFileResource( @Nonnull File file, String name )
    {
        this( file, name, null );
    }

    public PlexusIoFileResource( @Nonnull File file, @Nonnull PlexusIoResourceAttributes attrs )
    {
        this( file, getName( file ), attrs );
    }

    public PlexusIoFileResource( @Nonnull File file, @Nonnull String name, @Nonnull PlexusIoResourceAttributes attrs )
    {
        super( name, file.lastModified(), file.length(), file.isFile(), file.isDirectory(), file.exists() );
        this.file = file;
        if (attrs == null) throw new IllegalArgumentException( "attrs is null for file " + file.getName() );
        this.attributes = attrs;
    }

    private static String getName( File file )
    {
        return file.getPath().replace( '\\', '/' );
    }

    public static @Nonnull PlexusIoFileResource fileOnDisk(File file, String name, @Nonnull PlexusIoResourceAttributes attrs)
    {
        if ( attrs.isSymbolicLink() )
            return new PlexusIoSymlinkResource( file, name, attrs);
        else
            return new PlexusIoFileResource( file, name, attrs );
    }

    public static @Nonnull PlexusIoFileResource withName( File file, String name, @Nonnull PlexusIoResourceAttributes attrs )
    {
        if ( attrs.isSymbolicLink() )
            return new PlexusIoSymlinkResource( file, name, attrs);
        else
            return new PlexusIoFileResource( file, name, attrs );
    }

    public static @Nonnull PlexusIoFileResource existingFile( File file, @Nonnull PlexusIoResourceAttributes attrs )
    {
        if ( attrs.isSymbolicLink() )
            return new PlexusIoSymlinkResource( file, getName( file ), attrs);
        else
            return new PlexusIoFileResource( file, getName( file ), attrs );
    }

    /**
     * Returns the resources file.
     */
    @Nonnull
    public File getFile()
    {
        return file;
    }

    @Nonnull
    public InputStream getContents()
        throws IOException
    {
        return new FileInputStream( getFile() );
    }

    @Nonnull
    public URL getURL()
        throws IOException
    {
        return getFile().toURI().toURL();
    }

    public long getSize()
    {
        return getFile().length();
    }

    public boolean isDirectory()
    {
        return getFile().isDirectory();
    }

    public boolean isExisting()
    {
        return getFile().exists();
    }

    public boolean isFile()
    {
        return getFile().isFile();
    }

    @Nonnull public PlexusIoResourceAttributes getAttributes()
    {
        return attributes;
    }

    public long getLastModified()
    {
        if ( Java7Reflector.isAtLeastJava7() )
        {
            return Java7AttributeUtils.getLastModified( getFile() );
        }
        else
        {
            return getFile().lastModified();
        }
    }

    @Override public boolean isSymbolicLink() {
        return getAttributes().isSymbolicLink();
    }
}