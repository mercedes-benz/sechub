// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.mercedesbenz.sechub.EclipseUtil;
import com.mercedesbenz.sechub.SecHubActivator;

/**
 * The SecHub Job handler opens an import dialog to import the SecHub report.
 */
public class SecHubJobImportByContextMenuHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (! (selection instanceof IStructuredSelection)) {
			return null;
		}
		IStructuredSelection sse = (IStructuredSelection) selection;
		Object element = sse.getFirstElement();
		if (! (element instanceof IResource)) {
			return null;
		}
		IResource resource = (IResource) element;
		File fileOrNull;
		try {
			fileOrNull = EclipseUtil.toFileOrNull(resource);
		} catch (CoreException e) {
			throw new ExecutionException("Was not able to convert resource path",e);
		}
		
		SecHubActivator.getDefault().getImporter().importAndDisplayReport(fileOrNull);
		
		return null;
	}
}
