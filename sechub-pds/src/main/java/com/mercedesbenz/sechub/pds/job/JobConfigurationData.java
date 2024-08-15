// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

public class JobConfigurationData {
    private PDSJobConfiguration jobConfigurationJson;
    private String metaData;

    JobConfigurationData(PDSJobConfiguration jobConfigurationJson, String metaData) {
        this.jobConfigurationJson = jobConfigurationJson;
        this.metaData = metaData;
    }

    public String getMetaData() {
        return metaData;
    }

    public PDSJobConfiguration getJobConfiguration() {
        return jobConfigurationJson;
    }
}