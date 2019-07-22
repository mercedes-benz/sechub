// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.log;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProjectScanLogSummary {

	UUID sechubJobUUID;

	String executedBy;

	LocalDateTime started;

	LocalDateTime ended;

	String status;

	public ProjectScanLogSummary(UUID sechubJobUUID, String executedBy, LocalDateTime started, LocalDateTime ended, String status) {
		this.sechubJobUUID = sechubJobUUID;
		this.executedBy = executedBy;
		this.started = started;
		this.ended = ended;
		this.status = status;
	}

	public UUID getSechubJobUUID() {
		return sechubJobUUID;
	}

	public void setSechubJobUUID(UUID sechubJobUUID) {
		this.sechubJobUUID = sechubJobUUID;
	}

	public String getExecutedBy() {
		return executedBy;
	}

	public void setExecutedBy(String executedBy) {
		this.executedBy = executedBy;
	}

	public LocalDateTime getStarted() {
		return started;
	}

	public void setStarted(LocalDateTime started) {
		this.started = started;
	}

	public LocalDateTime getEnded() {
		return ended;
	}

	public void setEnded(LocalDateTime ended) {
		this.ended = ended;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


}
