package com.daimler.sechub.pds.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This class represents the schedule job status which can be obtained by REST
 *
 * @author Albert Tregnaghi
 *
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PDSJobStatus{

    public static final String PROPERTY_JOBUUID = "jobUUID";
    public static final String PROPERTY_OWNER= "owner";
    public static final String PROPERTY_CREATED= "created";
    public static final String PROPERTY_STARTED = "started";
    public static final String PROPERTY_ENDED= "ended";
    public static final String PROPERTY_STATE= "state";
    public static final String PROPERTY_RESULT= "result";

    UUID jobUUID;

    String owner;

    String created;
    String started;
    String ended;

    String state;
    String result;

    PDSJobStatus() {

    }

    public PDSJobStatus(PDSScheduleSecHubJob secHubJob) {
        this.jobUUID = secHubJob.getUUID();

        /*
         * why are nearly all parts represented as string and not direct parts? because
         * I didn't like "null" appearing in output to user - thats all
         */
        this.owner = secHubJob.getOwner();

        this.created = convertToString(secHubJob.getCreated());
        this.started = convertToString(secHubJob.getStarted());
        this.ended = convertToString(secHubJob.getEnded());

        this.state = convertToString(secHubJob.getExecutionState());
        this.result = convertToString(secHubJob.getExecutionResult());
    }

    private String convertToString(PDSJobExecutionResult result) {
        if (result == null) {
            return "";
        }
        return result.name();
    }

    private String convertToString(PDSJobExecutionState state) {
        if (state == null) {
            return "";
        }
        return state.name();
    }

    private String convertToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return "";
        }
        return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}