package com.daimler.sechub.domain.scan;

public class HTMLScanResultCodeScanEntry {

	String location;

	Integer line;

	Integer column;

	String source;


	public Integer getLine() {
		return line;
	}

	public Integer getColumn() {
		return column;
	}

	public String getSource() {
		return source;
	}

	public String getLocation() {
		return location;
	}
}
