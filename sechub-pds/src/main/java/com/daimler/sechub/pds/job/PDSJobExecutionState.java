// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

public enum PDSJobExecutionState {

	INITIALIZING("Initializing. E.g. Job has pending uploads etc."),

	READY_TO_START("Initialihzation done - so ready to start"),

	STARTED("Is started"),

	CANCEL_REQUESTED("A cancel was requested - but not ended now"),

	ENDED("");

	private String description;

	private PDSJobExecutionState(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
