// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.commons.model.JSONable;
import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@JsonInclude(value = Include.NON_ABSENT)
@MustBeKeptStable
public class FalsePositiveJobDataList implements JSONable<FalsePositiveJobDataList> {
	
    public static final String ACCEPTED_TYPE = "falsePositiveJobDataList";
	private static final FalsePositiveJobDataList CONVERTER = new FalsePositiveJobDataList();
	
	public static final String PROPERTY_API_VERSION = "apiVersion";
	public static final String PROPERTY_TYPE="type";
	public static final String PROPERTY_JOBDATA="jobData";

	private String apiVersion;
	
	private String type = ACCEPTED_TYPE;
	
	private List<FalsePositiveJobData> jobData = new ArrayList<>();
	
	public List<FalsePositiveJobData> getJobData() {
        return jobData;
    }
	
	public String getType() {
        return type;
    }
	
	public void setType(String type) {
        this.type = type;
    }
	
	@Override
	public Class<FalsePositiveJobDataList> getJSONTargetClass() {
		return FalsePositiveJobDataList.class;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getApiVersion() {
		return apiVersion;
	}
	
	public static FalsePositiveJobDataList fromString(String json) {
		return CONVERTER.fromJSON(json);
	}

}
