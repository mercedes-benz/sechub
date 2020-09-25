// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

public class SecHubRuntimeException extends RuntimeException{
	
	private static final long serialVersionUID = -4434773184205326471L;

	public SecHubRuntimeException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public SecHubRuntimeException(String message) {
		super(message);
		
	}


}
