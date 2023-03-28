// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.monitoring;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mercedesbenz.sechub.commons.model.SecHubLocalDateTimeDeserializer;
import com.mercedesbenz.sechub.commons.model.SecHubLocalDateTimeSerializer;
import com.mercedesbenz.sechub.pds.commons.core.PDSJSONConverter;
import com.mercedesbenz.sechub.pds.commons.core.PDSJSONConverterException;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionStatus;

public class PDSClusterMember {

    private static final Logger LOG = LoggerFactory.getLogger(PDSClusterMember.class);

    private String hostname;
    private String ip;
    private int port;

    @JsonDeserialize(using = SecHubLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SecHubLocalDateTimeSerializer.class)
    private LocalDateTime heartBeatTimestamp = LocalDateTime.now(); // we use initial now

    private PDSExecutionStatus executionState;

    public PDSClusterMember() {

    }

    public void setHeartBeatTimestamp(LocalDateTime heartBeatTimestamp) {
        this.heartBeatTimestamp = heartBeatTimestamp;
    }

    public LocalDateTime getHeartBeatTimestamp() {
        return heartBeatTimestamp;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getHostname() {
        return hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public PDSExecutionStatus getExecutionState() {
        return executionState;
    }

    public void setExecutionState(PDSExecutionStatus executionState) {
        this.executionState = executionState;
    }

    /**
     * Serializes member to JSON
     *
     * @return json - in failure case an empty json string will be returned ("{}")
     */
    public String toJSON() {
        try {
            return PDSJSONConverter.get().toJSON(this);
        } catch (PDSJSONConverterException e) {
            LOG.error("cannot convert cluster member data to JSON", e);
            return "{}";
        }
    }

    /**
     * Deserializes from json to cluster member
     *
     * @param json
     * @return <code>null</code> when json not valid/cannot be deserialized to
     *         cluster member
     */
    public static PDSClusterMember fromJSON(String json) {
        try {
            return PDSJSONConverter.get().fromJSON(PDSClusterMember.class, json);
        } catch (PDSJSONConverterException e) {
            LOG.error("cannot convert json to cluster member", e);
            return null;
        }
    }

}
