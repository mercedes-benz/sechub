// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.mercedesbenz.sechub.report.SecHubReportImportDialog;

/**
 * The SecHub Job handler opens an import dialog to import the SecHub report.
 */
public class SecHubJobImportHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		SecHubReportImportDialog dialog = new SecHubReportImportDialog(window.getShell());
		dialog.open();
		
		return null;
	}
}
