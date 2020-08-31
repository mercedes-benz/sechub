// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

public class CheckmarxSastScanSettings {

	private long projectId;
	private long presetId;
	private long engineConfigurationId;

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public long getProjectId() {
		return projectId;
	}

	public void setPresetId(long presetId) {
		this.presetId = presetId;
	}

	public long getPresetId() {
		return presetId;
	}

	public long getEngineConfigurationId() {
		return engineConfigurationId;
	}

	public void setEngineConfigurationId(long engineConfigurationId) {
		this.engineConfigurationId = engineConfigurationId;
	}

}
