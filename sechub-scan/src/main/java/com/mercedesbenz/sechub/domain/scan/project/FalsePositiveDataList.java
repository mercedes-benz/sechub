// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.model.JSONable;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@JsonInclude(value = Include.NON_ABSENT)
@MustBeKeptStable
public class FalsePositiveDataList implements JSONable<FalsePositiveDataList> {

    @Deprecated
    public static final String DEPRECATED_ACCEPTED_TYPE = "falsePositiveJobDataList";

    public static final String ACCEPTED_TYPE = "falsePositiveDataList";

    private static final FalsePositiveDataList CONVERTER = new FalsePositiveDataList();

    public static final String PROPERTY_API_VERSION = "apiVersion";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_JOBDATA = "jobData";
    public static final String PROPERTY_PROJECTDATA = "projectData";

    private String apiVersion;

    private String type = ACCEPTED_TYPE;

    private List<FalsePositiveJobData> jobData = new ArrayList<>();

    private List<FalsePositiveProjectData> projectData = new ArrayList<>();

    public List<FalsePositiveJobData> getJobData() {
        return jobData;
    }

    public List<FalsePositiveProjectData> getProjectData() {
        return projectData;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Class<FalsePositiveDataList> getJSONTargetClass() {
        return FalsePositiveDataList.class;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public static FalsePositiveDataList fromString(String json) {
        return CONVERTER.fromJSON(json);
    }

    @JsonSetter
    public void setJobData(List<FalsePositiveJobData> jobData) {
        this.jobData = (jobData != null) ? jobData : new ArrayList<>();
    }

    @JsonSetter
    public void setProjectData(List<FalsePositiveProjectData> projectData) {
        this.projectData = (projectData != null) ? projectData : new ArrayList<>();
    }

}
