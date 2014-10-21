package org.codehaus.plexus.components.io.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import org.codehaus.plexus.components.io.attributes.PlexusIoResourceAttributes;
import org.codehaus.plexus.components.io.functions.InputStreamSupplier;

/**
 * @author Kristian Rosenvold
 */
public class ResourceFactory
{

    public static PlexusIoResource createResource( File f, String p, PlexusIoResourceAttributes attributes,
                                                   final InputStreamSupplier inputStreamSupplier )
    {
        return attributes.isSymbolicLink() ? new PlexusIoSymlinkResource( f, p, attributes )
        {
            @Nonnull
            public InputStream getContents()
                throws IOException
            {
                return inputStreamSupplier.get();
            }
        } :

            new PlexusIoFileResource( f, p, attributes )
            {
                @Nonnull
                public InputStream getContents()
                    throws IOException
                {
                    return inputStreamSupplier.get();
                }
            };
    }

}
