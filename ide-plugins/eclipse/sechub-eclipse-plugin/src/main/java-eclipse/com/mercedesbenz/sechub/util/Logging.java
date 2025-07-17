// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.util;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.mercedesbenz.sechub.SecHubActivator;

public class Logging {
	public static void logInfo(String info) {
		IStatus status = new Status(IStatus.INFO, SecHubActivator.PLUGIN_ID, info);
		getLog().log(status);
	}
	
	public static void logWarning(String warning) {
		IStatus status = new Status(IStatus.WARNING, SecHubActivator.PLUGIN_ID, warning);
		getLog().log(status);
	}
	
	public static void logError(String error) {
		IStatus status = new Status(IStatus.ERROR, SecHubActivator.PLUGIN_ID, error);
		getLog().log(status);
	}
	
	public static void logError(String error, Throwable throwable) {
		IStatus status = new Status(IStatus.ERROR, SecHubActivator.PLUGIN_ID, error, throwable);
		getLog().log(status);
	}
	
	private static ILog getLog() {
		ILog log = SecHubActivator.getDefault().getLog();
		return log;
	}
}
