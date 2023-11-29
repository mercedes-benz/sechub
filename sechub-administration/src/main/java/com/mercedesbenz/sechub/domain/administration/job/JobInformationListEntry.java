package com.mercedesbenz.sechub.domain.administration.job;

import java.time.LocalDateTime;
import java.util.UUID;

public record JobInformationListEntry(

        UUID jobUUID,

        LocalDateTime since,

        JobStatus status,

        String projectId) {

    public static final String PROPERTY_JOB_UUID = "jobUUID";
    public static final String PROPERTY_STATUS = "status";

    public static final String PROPERTY_PROJECT_ID = "projectId";
    public static final String PROPERTY_SINCE = "since";
}
