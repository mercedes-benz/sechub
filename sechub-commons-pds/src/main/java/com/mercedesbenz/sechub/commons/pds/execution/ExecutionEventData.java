package com.mercedesbenz.sechub.commons.pds.execution;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ExecutionEventData {

    @JsonIgnore
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionEventData.class);

    public static final String CANCEL_REQUEST_SECONDS_TO_WAIT_FOR_PROCESS = null;

    private String creationTimeStamp;
    private Map<String, String> details;

    public ExecutionEventData() {
        this(LocalDateTime.now().toInstant(ZoneOffset.UTC));
    }

    @JsonIgnore
    public ExecutionEventData(Instant creationTimeInstant) {
        creationTimeStamp = creationTimeInstant.toString();
        details = new TreeMap<>();
    }

    @JsonIgnore
    public void setDetail(ExecutionEventDetailIdentifier identifier, String value) {
        getDetails().put(identifier.getDetailId(), value);
    }

    @JsonIgnore
    public String getDetail(ExecutionEventDetailIdentifier identifier) {
        return getDetails().get(identifier.getDetailId());
    }

    /**
     * Get detail for the given identifier - if not available return default
     *
     * @param identifier
     * @param defaultValue
     * @return value or default integer value
     */
    @JsonIgnore
    public int getDetail(ExecutionEventDetailIdentifier identifier, int defaultValue) {
        String value = getDetails().get(identifier.getDetailId());
        int detail = -1;
        if (value != null) {
            try {
                detail = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                LOG.warn("Was not able to parse data for identifier:{}", identifier, e);
            }
        }
        if (detail == -1) {
            LOG.trace("Will use default value:{}", value);
            detail = defaultValue;
        }
        return detail;
    }

    public String getCreationTimeStamp() {
        return creationTimeStamp;
    }

    private Map<String, String> getDetails() {
        return details;
    }

    public void setDetail(ExecutionEventDetailIdentifier cancelRequestSecondsToWaitForProcess, int value) {
        setDetail(cancelRequestSecondsToWaitForProcess, "" + value);
    }
}
