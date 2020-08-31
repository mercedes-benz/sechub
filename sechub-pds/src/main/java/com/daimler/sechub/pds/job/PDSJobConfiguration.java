// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.daimler.sechub.pds.PDSJSONConverter;
import com.daimler.sechub.pds.PDSJSONConverterException;
import com.daimler.sechub.pds.execution.PDSExecutionParameterEntry;

public class PDSJobConfiguration {

    private UUID sechubJobUUID;
    private String apiVersion;

    private String productId;

    private List<PDSExecutionParameterEntry> parameters = new ArrayList<>();

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
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

    public List<PDSExecutionParameterEntry> getParameters() {
        return parameters;
    }

    public static PDSJobConfiguration fromJSON(String json) throws PDSJSONConverterException {
        return PDSJSONConverter.get().fromJSON(PDSJobConfiguration.class, json);
    }

    public String toJSON() throws PDSJSONConverterException {
        return PDSJSONConverter.get().toJSON(this);
    }

}
