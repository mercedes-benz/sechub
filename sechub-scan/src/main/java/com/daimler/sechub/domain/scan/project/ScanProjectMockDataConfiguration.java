// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import java.util.Objects;
import java.util.Optional;

import com.daimler.sechub.commons.model.JSONable;
import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@JsonInclude(value = Include.NON_ABSENT)
@MustBeKeptStable
public class ScanProjectMockDataConfiguration implements JSONable<ScanProjectMockDataConfiguration> {
	
	private static final ScanProjectMockDataConfiguration CONVERTER = new ScanProjectMockDataConfiguration();
	
	public static final String PROPERTY_API_VERSION = "apiVersion";
	public static final String PROPERTY_WEB_SCAN = "webScan";
	public static final String PROPERTY_INFRA_SCAN = "infraScan";
	public static final String PROPERTY_CODE_SCAN = "codeScan";

	private String apiVersion;
	private Optional<ScanMockData> codeScan = Optional.empty();
	private Optional<ScanMockData> webScan = Optional.empty();
	private Optional<ScanMockData> infraScan = Optional.empty();

	@Override
	public Class<ScanProjectMockDataConfiguration> getJSONTargetClass() {
		return ScanProjectMockDataConfiguration.class;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getApiVersion() {
		return apiVersion;
	}
	
	public void setCodeScan(ScanMockData config) {
		this.codeScan=Optional.ofNullable(config);
	}
	
	public void setWebScan(ScanMockData config) {
		this.webScan=Optional.ofNullable(config);
	}
	
	public void setInfraScan(ScanMockData config) {
		this.infraScan=Optional.ofNullable(config);
	}

	public Optional<ScanMockData> getCodeScan() {
		return codeScan;
	}

	public Optional<ScanMockData> getWebScan() {
		return webScan;
	}

	public Optional<ScanMockData> getInfraScan() {
		return infraScan;
	}

	public static ScanProjectMockDataConfiguration fromString(String json) {
		return CONVERTER.fromJSON(json);
	}

	@Override
	public int hashCode() {
		return Objects.hash(apiVersion, codeScan, infraScan, webScan);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScanProjectMockDataConfiguration other = (ScanProjectMockDataConfiguration) obj;
		return Objects.equals(apiVersion, other.apiVersion) && Objects.equals(codeScan, other.codeScan) && Objects.equals(infraScan, other.infraScan)
				&& Objects.equals(webScan, other.webScan);
	}
	
	

}
