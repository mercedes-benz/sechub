// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.nessus;

import org.springframework.http.HttpStatus;

public class NessusRESTFailureException extends RuntimeException {

	private static final long serialVersionUID = 6448794893494468643L;
	private final String body;

	public NessusRESTFailureException(HttpStatus status, String body) {
		super("Netsparker REST failed with HTTP Status:" + (status != null ? status.name() : "null"));
		this.body = body;
	}

	public String getResponseBody() {
		return body;
	}

	@Override
	public String toString() {
		return super.toString() + "\nBody:\n" + getResponseBody();
	}
}
