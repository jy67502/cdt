/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.sourcelookup;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * 
 * Locates source elements in a Java project. Returns instances of <code>IFile</code>.
 * 
 * @since Sep 23, 2002
 */
public class CProjectSourceLocation implements ICSourceLocation
{
	/**
	 * The project associated with this source location
	 */
	private IProject fProject;

	/**
	 * Constructor for CProjectSourceLocation.
	 */
	public CProjectSourceLocation( IProject project )
	{
		setProject( project );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#findSourceElement(String)
	 */
	public Object findSourceElement( String name ) throws CoreException
	{
		if ( getProject() != null )
		{
			File file = new File( name );
			if ( file.isAbsolute() )
				return findFileByAbsolutePath( name );
			else
				return findFileByRelativePath( name );
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( ICSourceLocation.class ) )
			return this;
		if ( adapter.equals( CProjectSourceLocation.class ) )
			return this;
		return null;
	}

	/**
	 * Sets the project in which source elements will be searched for.
	 * 
	 * @param project the project
	 */
	private void setProject( IProject project )
	{
		fProject = project;
	}

	/**
	 * Returns the project associated with this source location.
	 * 
	 * @return project
	 */
	public IProject getProject()
	{
		return fProject;
	}

	private IFile findFile( IContainer parent, IPath name ) throws CoreException 
	{
		if ( name.isAbsolute() )
		{
			if ( name.toOSString().startsWith( parent.getLocation().toOSString() ) )
			{
				name = new Path( name.toOSString().substring( parent.getLocation().toOSString().length() + 1 ) );
			}
		}
		IResource found = parent.findMember( name );
		if ( found != null && found.getType() == IResource.FILE ) 
		{
			return (IFile)found;
		}
		IResource[] children= parent.members();
		for ( int i= 0; i < children.length; i++ ) 
		{
			if ( children[i] instanceof IContainer ) 
			{
				return findFile( (IContainer)children[i], name );
			}
		}
		return null;		
	}

	private Object findFileByAbsolutePath( String name )
	{
		IPath path = new Path( name );
		String fileName = path.toOSString();

		String pPath = new String( getProject().getLocation().toOSString() );
		int i = 0;
		if ( (i = fileName.indexOf( pPath )) >= 0 ) 
		{
			i += pPath.length() + 1;
			if ( fileName.length() > i )
				return getProject().getFile( fileName.substring( i ) );
		}
		return null;
	}

	private Object findFileByRelativePath( String fileName )
	{
		IPath path = getProject().getLocation().append( fileName );
		return findFileByAbsolutePath( path.toOSString() );
	}

	/**
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#getPaths()
	 */
	public IPath[] getPaths()
	{
		IPath[] result = new IPath[0];
		if ( getProject() != null )
		{
			result = new IPath[] { getProject().getLocation() };
		}
		return result;
	}
}
