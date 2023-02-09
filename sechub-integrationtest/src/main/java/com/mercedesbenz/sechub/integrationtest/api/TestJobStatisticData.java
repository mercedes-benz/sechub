package com.mercedesbenz.sechub.integrationtest.api;

import java.math.BigInteger;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
class TestJobStatisticData {

    public UUID sechubJobUUID;

    public String type;

    public String id;

    public BigInteger value;

    @Override
    public String toString() {
        return "TestJobStatisticData [" + (sechubJobUUID != null ? "sechubJobUUID=" + sechubJobUUID + ", " : "") + (type != null ? "type=" + type + ", " : "")
                + (id != null ? "id=" + id + ", " : "") + (value != null ? "value=" + value : "") + "]";
    }

}