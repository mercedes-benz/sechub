// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

public enum ExecutionState {

	INITIALIZING("Initializing. E.g. Workspace has pending uploads etc."),

	READY_TO_START("No state information available"),

	STARTED("Is started"),

	CANCEL_REQUESTED("A cancel was requested - but not ended now"),

	ENDED("");

	private String description;

	private ExecutionState(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
