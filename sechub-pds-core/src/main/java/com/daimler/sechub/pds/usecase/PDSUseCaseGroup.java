// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.usecase;

public enum PDSUseCaseGroup {

	ANONYMOUS("Anonymous","All these usecases handling anonymous access."),

	JOB_EXECUTION("Job execution","Execution of PSD jobs"),

	MONITORING("Monitoring","Monitoring usecases"),
	
	OTHER("Other","All other use cases"),

	;

	private String description;
	private String title;

	private PDSUseCaseGroup(String title, String description) {
		this.description = description;
		this.title=title;
	}
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}
}