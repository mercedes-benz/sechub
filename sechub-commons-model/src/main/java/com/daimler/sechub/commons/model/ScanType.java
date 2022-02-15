// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ScanType {

	CODE_SCAN("codeScan"),

	WEB_SCAN("webScan"),

	INFRA_SCAN("infraScan"), 
	
	/*
	 * This is just a fallback for unknown scan type.
	 */
	UNKNOWN("unknown"), 

	;

	private String id;

	private ScanType(String id) {
		this.id = id;
	}

	@JsonValue
	public String getId() {
		return id;
	}
}
