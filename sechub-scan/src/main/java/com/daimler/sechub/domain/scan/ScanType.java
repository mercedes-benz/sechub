package com.daimler.sechub.domain.scan;

public enum ScanType {

	CODE_SCAN("codeScan"),

	WEB_SCAN("webScan"),

	INFRA_SCAN("infraScan"),

	;

	private String id;

	private ScanType(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}
