package com.mercedesbenz.sechub.integrationtest.api;

import java.math.BigInteger;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
class TestJobRunStatisticData {

    public UUID executionUUID;

    public String type;

    public String id;

    public BigInteger value;

    @Override
    public String toString() {
        return "TestJobStatisticData [" + (executionUUID != null ? "sechubJobUUID=" + executionUUID + ", " : "") + (type != null ? "type=" + type + ", " : "")
                + (id != null ? "id=" + id + ", " : "") + (value != null ? "value=" + value : "") + "]";
    }

}