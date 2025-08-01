// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.mercedesbenz.sechub.report.SecHubReportImporter;
import com.mercedesbenz.sechub.util.EclipseIDELogger;
import com.mercedesbenz.sechub.util.IDELogAdapter;

/**
 * The activator class controls the plug-in life cycle
 */
public class SecHubActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.mercedesbenz.sechub.plugin"; //$NON-NLS-1$

	// The shared instance
	private static SecHubActivator plugin;
	private SecHubReportImporter importer;

	@Override
	public void start(BundleContext context) throws Exception {
		importer = new SecHubReportImporter();
		super.start(context);

		plugin = this;
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.addWorkbenchListener(new SecHubWorkbenchListener());
		
		IDELogAdapter.use(new EclipseIDELogger());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static SecHubActivator getDefault() {
		return plugin;
	}

	public SecHubReportImporter getImporter() {
		return importer;
	}

}
