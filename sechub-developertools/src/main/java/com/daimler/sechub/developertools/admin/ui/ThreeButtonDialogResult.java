// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

public class ThreeButtonDialogResult<T> {
	private boolean canceled;
	private boolean finished;
	private boolean added;

	private T value;
	
	public ThreeButtonDialogResult(boolean canceled, boolean added, boolean finished, T value) {
		this.canceled = canceled;
		this.finished = finished;
		this.added = added;
		this.value = value;
	}
	
	public boolean isCanceled() {
		return canceled;
	}

	public boolean isFinished() {
		return finished;
	}
	
	public boolean isAdded() {
		return added;
	}

	public T getValue() {
		return value;
	}
	
	public boolean hasValue() {		
		return value != null;
	}
}
