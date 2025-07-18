// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;
import com.mercedesbenz.sechub.api.internal.gen.model.TrafficLight;

public class FindingModel {

	private List<FindingNode> findings = new ArrayList<>();
	private UUID jobUUID;
	private TrafficLight trafficLight;
	private String status;
	private String projectId;
	private SecHubReport report;

	public UUID getJobUUID() {
		return jobUUID;
	}
	
	public SecHubReport getReport() {
		return report;
	}
	
	public void setReport(SecHubReport report) {
		this.report = report;
	}

	public void setTrafficLight(TrafficLight trafficLight) {
		this.trafficLight = trafficLight;
	}

	public TrafficLight getTrafficLight() {
		return trafficLight;
	}

	public void setJobUUID(UUID jobUUID) {
		this.jobUUID = jobUUID;
	}

	public int getFindingCount() {
		return findings.size();
	}

	public List<FindingNode> getFindings() {
		return findings;
	}

	public FindingNode getFirstFinding() {
		if (findings.size() > 0) {
			return findings.get(0);
		}
		return null;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getProjectId() {
		return projectId;
	}
}
