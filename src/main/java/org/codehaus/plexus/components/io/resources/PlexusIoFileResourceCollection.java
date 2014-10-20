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

import static org.codehaus.plexus.components.io.attributes.Java7Reflector.isAtLeastJava7;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.annotation.Nonnull;

import org.codehaus.plexus.components.io.attributes.*;
import org.codehaus.plexus.components.io.dirscanner.DirectoryScanner;
import org.codehaus.plexus.components.io.functions.PlexusIoResourceAttributeSupplier;
import org.codehaus.plexus.components.io.functions.PlexusIoResourceConsumer;
import org.codehaus.plexus.util.StringUtils;

/**
 * Implementation of {@link PlexusIoResourceCollection} for the set
 * of files in a common directory.
 */
public class PlexusIoFileResourceCollection
    extends AbstractPlexusIoResourceCollectionWithAttributes
{
    /**
     * Role hint of this component
     */
    public static final String ROLE_HINT = "files";

    private File baseDir;

    private boolean isFollowingSymLinks = true;

    public PlexusIoFileResourceCollection()
    {
    }

    /**
     * @param baseDir The base directory of the file collection
     */
    public void setBaseDir( File baseDir )
    {
        this.baseDir = baseDir;
    }

    /**
     * @return Returns the file collections base directory.
     */
    public File getBaseDir()
    {
        return baseDir;
    }

    /**
     * @return Returns, whether symbolic links should be followed.
     * Defaults to true.
     */
    public boolean isFollowingSymLinks()
    {
        return isFollowingSymLinks;
    }

    /**
     * @param pIsFollowingSymLinks whether symbolic links should be followed
     */
    @SuppressWarnings({ "UnusedDeclaration" })
    public void setFollowingSymLinks( boolean pIsFollowingSymLinks )
    {
        isFollowingSymLinks = pIsFollowingSymLinks;
    }

    public void setDefaultAttributes( final int uid, final String userName, final int gid, final String groupName,
                                      final int fileMode, final int dirMode )
    {
        setDefaultFileAttributes( createDefaults(uid, userName, gid, groupName, fileMode) );

        setDefaultDirAttributes( createDefaults(uid, userName, gid, groupName, dirMode) );
    }

    public void setOverrideAttributes( final int uid, final String userName, final int gid, final String groupName,
                                       final int fileMode, final int dirMode )
    {
        setOverrideFileAttributes( createDefaults(uid, userName, gid, groupName, fileMode) );

        setOverrideDirAttributes( createDefaults(uid, userName, gid, groupName, dirMode) );
    }

    private static PlexusIoResourceAttributes createDefaults( final int uid, final String userName, final int gid,
                                                              final String groupName, final int mode )
    {
        return new SimpleResourceAttributes( uid, userName, gid, groupName, mode >= 0 ? mode : -1 );
    }


    @Override
    public void setPrefix(String prefix) {
        char nonSeparator = File.separatorChar == '/' ?'\\' : '/';
        super.setPrefix(StringUtils.replace( prefix, nonSeparator, File.separatorChar));
    }

    private void addResources( List<PlexusIoResource> result, String[] resources,
                               Map<String, PlexusIoResourceAttributes> attributesByPath )
        throws IOException
    {

        final File dir = getBaseDir();
        for ( String name : resources )
        {
            String sourceDir = name.replace( '\\', '/' );

            PlexusIoFileResource resource = addResource(attributesByPath, name, new File(dir, sourceDir));
            if ( isSelected( resource ) )
            {
                result.add( resource );
            }
        }
    }

    private @Nonnull PlexusIoFileResource addResource(Map<String, PlexusIoResourceAttributes> attributesByPath,
            String name, File f) {

        PlexusIoResourceAttributes attrs = attributesByPath.get( name.length() > 0 ? name : "." );
        if ( attrs == null )
		{
			attrs = attributesByPath.get(f.getAbsolutePath());
		}
        if ( attrs == null )
		{
			attrs = SimpleResourceAttributes.lastResortDummyAttributesForBrokenOS();
		}

        if ( f.isDirectory() )
		{
			attrs = PlexusIoResourceAttributeUtils.mergeAttributes(getOverrideDirAttributes(), attrs,
                    getDefaultDirAttributes());
		}
		else
		{
			attrs = PlexusIoResourceAttributeUtils.mergeAttributes( getOverrideFileAttributes(), attrs,
																	getDefaultFileAttributes() );
		}

        return PlexusIoFileResource.fileOnDisk(f, name, attrs);
    }


    private void addResourcesJava7( List<PlexusIoResource> result,  List<Java7FileResource> src )
        throws IOException
    {

        final File dir = getBaseDir();
        final HashMap<Integer, String> cache1 = new HashMap<Integer, String>();
        final HashMap<Integer, String> cache2 = new HashMap<Integer, String>();
        for ( Java7FileResource name : src )
        {
            final PlexusIoResource plexusIoResource = addJava7Resource( cache1, cache2, name );
            if (plexusIoResource != null) result.add( plexusIoResource);
        }
    }

    private PlexusIoResource addJava7Resource( final HashMap<Integer,  String> cache1, final HashMap<Integer,  String> cache2,
                                               final Java7FileResource name )
        throws IOException
    {
        PlexusIoResourceAttributeSupplier supp = new PlexusIoResourceAttributeSupplier()
        {
            public PlexusIoResourceAttributes getAttributes()
            {
                PlexusIoResourceAttributes attrs;
                try
                {
                    attrs = new Java7FileAttributes( name.getFile(), cache1, cache2 );
                if ( name.isDirectory() )
                {
                    attrs = PlexusIoResourceAttributeUtils.mergeAttributes( getOverrideDirAttributes(), attrs,
                                                                            getDefaultDirAttributes() );
                }
                else
                {
                    attrs = PlexusIoResourceAttributeUtils.mergeAttributes( getOverrideFileAttributes(), attrs,
                                                                            getDefaultFileAttributes() );
                }
                    return attrs;
                } catch (IOException e){
                    throw new RuntimeException( e );
                }

            }
        };

        PlexusIoResource resource = name.withAttributes( supp );
        if ( isSelected( resource ) )
        {
            return resource;
        }                    else return null;
    }

    public Iterator<PlexusIoResource> getResources()
        throws IOException
    {
        final DirectoryScanner ds = getDirectoryScanner();
        final List<Java7FileResource> resultz = new ArrayList<Java7FileResource>(  );
        ds.scan(new PlexusIoResourceConsumer()
        {
            public void accept( PlexusIoResource resource )
            {
                if (resource.isDirectory() && isIncludingEmptyDirectories() || !resource.isDirectory())
                    resultz.add( (Java7FileResource) resource );
            }
        });

        if ( isAtLeastJava7() )
        {
            final List<PlexusIoResource> result = new ArrayList<PlexusIoResource>();
            addResourcesJava7( result, resultz );
            return result.iterator();
        }
        else
        {
            Map<String, PlexusIoResourceAttributes> attributesByPath =
                PlexusIoResourceAttributeUtils.getFileAttributesByPath( getBaseDir() );

            final List<PlexusIoResource> result = new ArrayList<PlexusIoResource>();
            addResourcesJava7( result, resultz );
            return result.iterator();
        }
    }

    private DirectoryScanner getDirectoryScanner() {
        final DirectoryScanner ds = new DirectoryScanner();
        final File dir = getBaseDir();
        ds.setBasedir( dir );
        final String[] inc = getIncludes();
        if ( inc != null && inc.length > 0 )
        {
            ds.setIncludes( inc );
        }
        final String[] exc = getExcludes();
        if ( exc != null && exc.length > 0 )
        {
            ds.setExcludes( exc );
        }
        if ( isUsingDefaultExcludes() )
        {
            ds.addDefaultExcludes();
        }
        ds.setCaseSensitive( isCaseSensitive() );
        ds.setFollowSymlinks( isFollowingSymLinks() );
        return ds;
    }

    public void forEach(PlexusIoResourceConsumer resourceConsumer)
            throws IOException
    {
        final DirectoryScanner ds = getDirectoryScanner();
        PlexusIoResourceConsumer wrapper = isAtLeastJava7()  ? new AttributeJ7WrappingResourceConsumer( resourceConsumer )
                                                         : new AttributeWrappingResourceConsumer(resourceConsumer);
        ds.scan(wrapper);
    }


    class AttributeJ7WrappingResourceConsumer implements PlexusIoResourceConsumer {
        private final PlexusIoResourceConsumer target;

        HashMap<Integer, String> cache1 = new HashMap<Integer, String>(  );
        HashMap<Integer, String> cache2 =   new HashMap<Integer, String>(  );

        AttributeJ7WrappingResourceConsumer( PlexusIoResourceConsumer target )
            throws IOException
        {
            this.target = target;

        }

        public void accept( PlexusIoResource resource )
            throws IOException
        {
            final PlexusIoResource plexusIoResource = addJava7Resource(cache1, cache2, (Java7FileResource) resource);
            if ( plexusIoResource != null) target.accept( plexusIoResource );

        }
    }


    class AttributeWrappingResourceConsumer implements PlexusIoResourceConsumer {
        private final PlexusIoResourceConsumer target;

        private final Map<String, PlexusIoResourceAttributes> attributesByPath;


        AttributeWrappingResourceConsumer( PlexusIoResourceConsumer target ) throws IOException
        {
            this.target = target;
            attributesByPath = PlexusIoResourceAttributeUtils.getFileAttributesByPath( getBaseDir() );
        }

        public void accept( PlexusIoResource resource )
                throws IOException
        {
            target.accept(addResource(attributesByPath, resource.getName(), new File(resource.getName())));
        }
    }

}
