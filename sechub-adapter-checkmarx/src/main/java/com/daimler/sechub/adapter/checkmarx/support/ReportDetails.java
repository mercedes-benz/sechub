// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx.support;

public class ReportDetails {

	String status;
	boolean notFound;
	public boolean isNotFound() {
		return notFound;
	}
	public boolean isRunning() {
		return isCheckPossible() && !isReportCreated();
	}
	
	private boolean isCheckPossible() {
		return ! isNotFound();
	}
	
	private boolean isReportCreated() {
		return "Created".equals(status);
	}
	

}
