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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import javax.annotation.Nonnull;

import org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributes;
import org.codehaus.plexus.components.io.functions.InputStreamSupplier;
import org.codehaus.plexus.components.io.functions.InputStreamTransformer;
import org.codehaus.plexus.components.io.functions.PlexusIoResourceConsumer;


/**
 * Abstract base class for compressed files, aka singleton
 * resource collections.
 */
public abstract class PlexusIoCompressedFileResourceCollection
    implements PlexusIoArchivedResourceCollection, Iterable<PlexusIoResource>
{
    private File file;
    private String path;
    private InputStreamTransformer[] streamTransformers = AbstractPlexusIoResourceCollection.empty;


    public File getFile()
    {
        return file;
    }

    public void setFile( File file )
    {
        this.file = file;
        
    }

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
        
    }


    // return the file attributes of the uncompressed file
    // may be null.
    protected abstract PlexusIoResourceAttributes getAttributes(File f) throws IOException;

    public void addStreamTransformer( InputStreamTransformer streamTransformer )
    {
        streamTransformers = Arrays.copyOf( this.streamTransformers, this.streamTransformers.length + 1 );
        streamTransformers[streamTransformers.length -1] = streamTransformer;
    }

    public void setStreamTransformers( InputStreamTransformer... streamTransformers )
    {
        this.streamTransformers = streamTransformers;
    }


    public Iterator<PlexusIoResource> getResources()
        throws IOException
    {
        final File f = getFile();
        final String p = (getPath() == null ? getName( f ) : getPath()).replace( '\\', '/' );
        if ( f == null )
        {
            throw new IOException( "No archive file is set." );
        }
        if ( ! f.isFile() )
        {
            throw new IOException( "The archive file " + f.getPath()
                                   + " does not exist or is no file." ); 
        }


        final PlexusIoResourceAttributes attributes = getAttributes( f );

        final InputStreamSupplier inputStreamSupplier = new InputStreamSupplier()
        {
            @Nonnull
            public InputStream get()
                throws IOException
            {
                return getInputStream( f );
            }
        };

        final PlexusIoResource resource =
            ResourceFactory.createResource(f, p, attributes, inputStreamSupplier );

        return Collections.singleton( resource ).iterator();
    }


    public void forEach( PlexusIoResourceConsumer resourceConsumer )
        throws IOException
    {
        final Iterator<PlexusIoResource> resources = getResources();
        while (resources.hasNext()){
            resourceConsumer.accept(  resources.next() );
        }
    }

    protected String getName( File file ) throws IOException {
        final String name = file.getPath();
        final String ext = getDefaultExtension();
        if ( ext != null  &&  ext.length() > 0  &&  name.endsWith( ext ) )
        {
            return name.substring( 0, name.length() - ext.length() );
        }
        return name;
    }

    protected abstract String getDefaultExtension();

    protected abstract @Nonnull InputStream getInputStream( File file ) throws IOException;

    public InputStream getInputStream( PlexusIoResource resource )
        throws IOException
    {
        InputStream contents = resource.getContents();
        for ( InputStreamTransformer streamTransformer : streamTransformers )
        {
            final InputStream transformed = streamTransformer.transform( resource, contents );
            contents = new ClosingInputStream( transformed, contents );
        }
        return contents;
    }


    public Iterator<PlexusIoResource> iterator()
    {
        try
        {
            return getResources();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    public String getName( PlexusIoResource resource )
        throws IOException
    {
        return resource.getName();
    }

    public long getLastModified() throws IOException
    {
        File f = getFile();
        return f == null ? PlexusIoResource.UNKNOWN_MODIFICATION_DATE : f.lastModified();
    }
}