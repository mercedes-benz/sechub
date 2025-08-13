// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.util;

public class FallbackIDELogger implements IDELogger{

	@Override
	public void logError(String message, Throwable t) {
		System.err.println("FALLBACK IDE logger:"+message);
		t.printStackTrace();
	}

}
