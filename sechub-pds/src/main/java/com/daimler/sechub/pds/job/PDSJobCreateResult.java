// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PDSJobCreateResult {

    public static final String PROPERTY_JOBID="jobUUID";
    
    UUID jobUUID;

    public PDSJobCreateResult(UUID jobUUID) {
        this.jobUUID = jobUUID;
    }

    public UUID getJobUUID() {
        return jobUUID;
    }


}
