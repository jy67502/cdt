/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

/**
 * Ruler action delegate for the breakpoint properties action.
 */
public class BreakpointPropertiesRulerActionDelegate extends AbstractDisassemblyRulerActionDelegate {

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions.AbstractDisassemblyRulerActionDelegate#createAction(org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyPart, org.eclipse.jface.text.source.IVerticalRulerInfo)
	 */
	@Override
	protected IAction createAction(IDisassemblyPart disassemblyPart, IVerticalRulerInfo rulerInfo) {
		return new BreakpointPropertiesRulerAction(disassemblyPart, rulerInfo);
	}

}
