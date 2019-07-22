// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx.support;

public class QueueDetails {

	String stageValue;
	boolean newQueueEntryFound;
	int checkCount;
	String failureText;
	boolean done;

	public String getStageValue() {
		return stageValue;
	}

	public boolean isRunning() {
		return isStillProcessing() && !hasFailed();
	}

	public boolean hasFailed() {
		return "Failed".equals(stageValue);
	}

	public int getCheckCount() {
		return checkCount;
	}

	public boolean isStillProcessing() {
		return !done;
	}

	public boolean isNewQueueEntryFound() {
		return newQueueEntryFound;
	}

	public String getFailureText() {
		return failureText;
	}

	public boolean hasNeverRun() {
		return ! newQueueEntryFound;
	}

}