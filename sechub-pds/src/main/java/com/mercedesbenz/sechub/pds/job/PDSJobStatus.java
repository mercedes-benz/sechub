// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.commons.core.PDSJSONConverter;
import com.mercedesbenz.sechub.pds.commons.core.PDSJSONConverterException;

/**
 * This class represents the schedule job status which can be obtained by REST
 *
 * @author Albert Tregnaghi
 *
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class PDSJobStatus {

    public static final String PROPERTY_JOBUUID = "jobUUID";
    public static final String PROPERTY_OWNER = "owner";
    public static final String PROPERTY_CREATED = "created";
    public static final String PROPERTY_STARTED = "started";
    public static final String PROPERTY_ENDED = "ended";
    public static final String PROPERTY_STATE = "state";

    UUID jobUUID;

    String owner;

    String created;
    String started;
    String ended;

    String state;

    PDSJobStatus() {

    }

    public PDSJobStatus(PDSJob secHubJob) {
        this.jobUUID = secHubJob.getUUID();

        this.owner = secHubJob.getOwner();

        this.created = convertToString(secHubJob.getCreated());
        this.started = convertToString(secHubJob.getStarted());
        this.ended = convertToString(secHubJob.getEnded());

        this.state = convertToString(secHubJob.getState());
    }

    private String convertToString(PDSJobStatusState result) {
        if (result == null) {
            return "";
        }
        return result.name();
    }

    private String convertToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return "";
        }
        return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public String toJSON() throws PDSJSONConverterException {
        return PDSJSONConverter.get().toJSON(this);
    }
}