// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.util;

public class EclipseIDELogger implements IDELogger{

	@Override
	public void logError(String message, Throwable t) {
		Logging.logError(message,t);
	}

}
