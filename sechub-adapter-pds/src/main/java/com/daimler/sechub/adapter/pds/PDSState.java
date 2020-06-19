// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

public enum PDSState {
	COMPLETE("Complete"),

	FAILED("Failed"),

	CANCELED("Cancelled");

	private String id;

	private PDSState(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id may not be null!");
		}
		this.id = id;
	}

	public boolean isRepresentedBy(String state) {
		if (state == null) {
			return false;
		}
		return id.equals(state);
	}

	public static boolean isWellknown(String state) {
		for (PDSState value : values()) {
			if (value.isRepresentedBy(state)) {
				return true;
			}
		}
		return false;
	}
}
