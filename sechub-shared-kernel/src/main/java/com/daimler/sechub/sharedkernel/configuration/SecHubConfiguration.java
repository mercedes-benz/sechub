// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration;

import java.util.Optional;

import com.daimler.sechub.commons.model.JSONable;
import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Be aware to add only parts into this class and do NOT remove properties being
 * still in PROD! (E.g. API V1 stills supported has field "ugly" and API V2 does
 * not support it, but API V1 is still in use... and supported) If you dont
 * support a field in a special API variant you should trigger an error in
 * validation!
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by users to schedule a job. It has to be backward compatible. To afford this we will NOT remove older parts since final API releases")
public class SecHubConfiguration implements JSONable<SecHubConfiguration> {

	public static final String PROPERTY_PROJECT_ID = "projectId";
	public static final String PROPERTY_API_VERSION = "apiVersion";
	public static final String PROPERTY_WEB_SCAN = "webScan";
	public static final String PROPERTY_INFRA_SCAN = "infraScan";
	public static final String PROPERTY_CODE_SCAN = "codeScan";

	private static final SecHubConfiguration INITIALIZER = new SecHubConfiguration();
	private Optional<SecHubWebScanConfiguration> webScan = Optional.empty();
	private Optional<SecHubInfrastructureScanConfiguration> infraScan = Optional.empty();
	private Optional<SecHubCodeScanConfiguration> codeScan = Optional.empty();

	private String apiVersion;

	private String projectId;

	public static SecHubConfiguration createFromJSON(String json) {
		return INITIALIZER.fromJSON(json);
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setWebScan(SecHubWebScanConfiguration webScan) {
		this.webScan = Optional.ofNullable(webScan);
	}

	public Optional<SecHubWebScanConfiguration> getWebScan() {
		return webScan;
	}

	public void setCodeScan(SecHubCodeScanConfiguration codeScan) {
		this.codeScan = Optional.ofNullable(codeScan);
	}

	public Optional<SecHubCodeScanConfiguration> getCodeScan() {
		return codeScan;
	}

	public void setInfraScan(SecHubInfrastructureScanConfiguration infraStructureScan) {
		this.infraScan = Optional.ofNullable(infraStructureScan);
	}

	public Optional<SecHubInfrastructureScanConfiguration> getInfraScan() {
		return infraScan;
	}

	@Override
	public Class<SecHubConfiguration> getJSONTargetClass() {
		return SecHubConfiguration.class;
	}

}
