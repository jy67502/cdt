/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyEditorInput;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyView;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Toggles a line breakpoint in a C/C++ editor.
 */
public class ToggleBreakpointAdapter implements IToggleBreakpointsTarget {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleLineBreakpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
		String errorMessage = null;
		if ( part instanceof IEditorPart ) {
			IEditorPart editorPart = (IEditorPart)part;
			IEditorInput input = editorPart.getEditorInput();
			if ( input == null ) {
				errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Empty_editor_1" ); //$NON-NLS-1$
			}
			else {
				ITextEditor textEditor = (ITextEditor)editorPart;
				IDocument document = textEditor.getDocumentProvider().getDocument( input );
				if ( document == null ) {
					errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Missing_document_1" ); //$NON-NLS-1$
				}
				else {
					IResource resource = getResource( textEditor );
					if ( resource == null ) {
						errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Missing_resource_1" ); //$NON-NLS-1$
					}
					else {
						BreakpointLocationVerifier bv = new BreakpointLocationVerifier();
						int lineNumber = bv.getValidLineBreakpointLocation( document, ((ITextSelection)selection).getStartLine() );
						if ( lineNumber == -1 ) {
							errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Invalid_line_1" ); //$NON-NLS-1$
						}
						else {
							String sourceHandle = getSourceHandle( input );
							ICLineBreakpoint breakpoint = CDIDebugModel.lineBreakpointExists( sourceHandle, resource, lineNumber );
							if ( breakpoint != null ) {
								DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( breakpoint, true );
							}
							else {
								CDIDebugModel.createLineBreakpoint( sourceHandle, 
																	resource, 
																	lineNumber, 
																	true, 
																	0, 
																	"", //$NON-NLS-1$
																	true );
							}
							return;
						}
					}
				}
			}
		}
		else if ( part instanceof DisassemblyView ) {
			IEditorInput input = ((DisassemblyView)part).getInput();
			if ( !(input instanceof DisassemblyEditorInput) ) {
				errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Empty_editor_1" ); //$NON-NLS-1$
			}
			else {
				BreakpointLocationVerifier bv = new BreakpointLocationVerifier();
				int lineNumber = bv.getValidAddressBreakpointLocation( null, ((ITextSelection)selection).getStartLine() );
				if ( lineNumber == -1 ) {
					errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Invalid_line_1" ); //$NON-NLS-1$
				}
				else {
					IResource resource = ResourcesPlugin.getWorkspace().getRoot();
					String sourceHandle = getSourceHandle( input );
					long address = ((DisassemblyEditorInput)input).getAddress( lineNumber );
					ICAddressBreakpoint breakpoint = CDIDebugModel.addressBreakpointExists( sourceHandle, resource, address );
					if ( breakpoint != null ) {
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( breakpoint, true );
					}
					else {
						CDIDebugModel.createAddressBreakpoint( sourceHandle, 
															   resource, 
															   address, 
															   true, 
															   0, 
															   "", //$NON-NLS-1$
															   true );
					}
					return;
				}
			}
		}
		else {
			errorMessage = ActionMessages.getString( "RunToLineAdapter.Operation_is_not_supported_1" ); //$NON-NLS-1$
		}
		throw new CoreException( new Status( IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), ICDebugUIConstants.INTERNAL_ERROR, errorMessage, null ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleLineBreakpoints( IWorkbenchPart part, ISelection selection ) {
		return ( selection instanceof ITextSelection );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleMethodBreakpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleMethodBreakpoints( IWorkbenchPart part, ISelection selection ) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleWatchpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
		IEditorPart editorPart = (IEditorPart)part;
		IEditorInput input = editorPart.getEditorInput();
		String errorMessage = null;
		if ( input == null ) {
			errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Empty_editor_2" ); //$NON-NLS-1$
		}
		else {
			ITextEditor textEditor = (ITextEditor)editorPart;
			IDocument document = textEditor.getDocumentProvider().getDocument( input );
			if ( document == null ) {
				errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Missing_document_2" ); //$NON-NLS-1$
			}
			else {
				IResource resource = getResource( textEditor );
				if ( resource == null ) {
					errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Missing_resource_2" ); //$NON-NLS-1$
				}
				else {
					if ( !(resource instanceof IWorkspaceRoot) )
						resource = resource.getProject();
					String expression = ( selection instanceof TextSelection ) ? ((TextSelection)selection).getText().trim() : ""; //$NON-NLS-1$
					AddWatchpointDialog dlg = new AddWatchpointDialog( textEditor.getSite().getShell(), true, false, expression );
					if ( dlg.open() != Window.OK )
						return;
					expression = dlg.getExpression();
					WatchpointExpressionVerifier wev = new WatchpointExpressionVerifier();
					if ( !wev.isValidExpression( document, expression ) ) {
						errorMessage = ActionMessages.getString( "ToggleBreakpointAdapter.Invalid_expression_1" ) + expression; //$NON-NLS-1$
					}
					else {
						String sourceHandle = getSourceHandle( input );
						ICWatchpoint watchpoint = CDIDebugModel.watchpointExists( sourceHandle, resource, expression );
						if ( watchpoint != null ) {
							DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( watchpoint, true );
						}
						else {
							CDIDebugModel.createWatchpoint( sourceHandle, 
															resource,
															dlg.getWriteAccess(), 
															dlg.getReadAccess(),
															expression, 
															true, 
															0, 
															"", //$NON-NLS-1$
															true );
						}
						return;
					}
				}
			}
		}
		throw new CoreException( new Status( IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), ICDebugUIConstants.INTERNAL_ERROR, errorMessage, null ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleWatchpoints( IWorkbenchPart part, ISelection selection ) {
		return false;
	}

	protected void report( String message, IWorkbenchPart part ) {
		IEditorStatusLine statusLine = (IEditorStatusLine)part.getAdapter( IEditorStatusLine.class );
		if ( statusLine != null ) {
			if ( message != null ) {
				statusLine.setMessage( true, message, null );
			}
			else {
				statusLine.setMessage( true, null, null );
			}
		}
		if ( message != null && CDebugUIPlugin.getActiveWorkbenchShell() != null ) {
			CDebugUIPlugin.getActiveWorkbenchShell().getDisplay().beep();
		}
	}

	protected static IResource getResource( IEditorPart editor ) {
		IResource resource;
		IEditorInput editorInput = editor.getEditorInput();
		if ( editorInput instanceof IFileEditorInput ) {
			resource = ((IFileEditorInput)editorInput).getFile();
		}
		else {
			resource = ResourcesPlugin.getWorkspace().getRoot();
		}
		return resource;
	}

	private String getSourceHandle( IEditorInput input ) throws CoreException {
		if ( input instanceof IFileEditorInput ) {
			return ((IFileEditorInput)input).getFile().getFullPath().toOSString();
		}
		if ( input instanceof IStorageEditorInput ) {
			return ((IStorageEditorInput)input).getStorage().getName();
		}
		if ( input instanceof DisassemblyEditorInput ) {
			IFile file = ((DisassemblyEditorInput)input).getModuleFile();
			return ( file != null ) ? file.getLocation().toOSString() : ""; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}
}
