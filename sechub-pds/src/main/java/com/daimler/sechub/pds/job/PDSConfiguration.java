package com.daimler.sechub.pds.job;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.daimler.sechub.pds.PDSJSONConverter;
import com.daimler.sechub.pds.PDSJSONConverterException;
import com.daimler.sechub.pds.execution.PDSExecutionConfigEntry;

public class PDSConfiguration {

    private UUID sechubJobUUID;
    private String apiVersion;
    private List<PDSExecutionConfigEntry> config = new ArrayList<>();
    
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
    
    public String getApiVersion() {
        return apiVersion;
    }
    /**
     * @return related SecHub job UUID
     */
    public UUID getSechubJobUUID() {
        return sechubJobUUID;
    }
    
    public void setSechubJobUUID(UUID sechubJobUUID) {
        this.sechubJobUUID = sechubJobUUID;
    }
    
    public List<PDSExecutionConfigEntry> getConfig() {
        return config;
    }

    public static PDSConfiguration fromJSON(String json) throws PDSJSONConverterException {
        return PDSJSONConverter.get().fromJSON(PDSConfiguration.class, json);
    }
    
    public String toJSON() throws PDSJSONConverterException {
        return PDSJSONConverter.get().toJSON(this);
    }
    
}
