package com.daimler.sechub.pds.monitoring;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.pds.PDSJSONConverter;
import com.daimler.sechub.pds.PDSJSONConverterException;
import com.daimler.sechub.pds.execution.PDSExecutionStatus;
import com.daimler.sechub.pds.util.PDSLocalDateTimeDeserializer;
import com.daimler.sechub.pds.util.PDSLocalDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class PDSClusterMember {

    

    private static final Logger LOG = LoggerFactory.getLogger(PDSClusterMember.class);

    private String hostname;
    private String ip;
    
    @JsonDeserialize(using = PDSLocalDateTimeDeserializer.class)  
    @JsonSerialize(using = PDSLocalDateTimeSerializer.class)  
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

    public String toJSON() {
        try {
            return PDSJSONConverter.get().toJSON(this);
        } catch (PDSJSONConverterException e) {
            LOG.error("cannot convert cluster member data to JSON",e);
            return "{}";
        }
    }

}
