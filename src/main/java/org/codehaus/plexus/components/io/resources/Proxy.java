package org.codehaus.plexus.components.io.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.annotation.Nonnull;

import org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributes;
import org.codehaus.plexus.components.io.functions.PlexusIoResourceAttributeSupplier;

class Proxy
    implements PlexusIoResource, PlexusIoResourceWithAttributes {
    private final PlexusIoResource target;

    private final PlexusIoResourceAttributeSupplier plexusIoResourceAttributes;

    Proxy( PlexusIoResource target, PlexusIoResourceAttributeSupplier plexusIoResourceAttributes )
    {

        this.target = target;
        this.plexusIoResourceAttributes = plexusIoResourceAttributes;
    }

    public long getLastModified()
    {
        return target.getLastModified();
    }

    public boolean isExisting()
    {
        return target.isExisting();
    }

    public long getSize()
    {
        return target.getSize();
    }

    public boolean isFile()
    {
        return target.isFile();
    }

    public boolean isDirectory()
    {
        return target.isDirectory();
    }

    @Nonnull public InputStream getContents()
        throws IOException
    {
        return target.getContents();
    }

    public URL getURL()
        throws IOException
    {
        return target.getURL();
    }

    public String getName()
    {
        return target.getName();
    }

    public boolean isSymbolicLink()
    {
        return target.isSymbolicLink();
    }

    public PlexusIoResourceAttributes getAttributes()
    {
       return plexusIoResourceAttributes.getAttributes();
    }
}
