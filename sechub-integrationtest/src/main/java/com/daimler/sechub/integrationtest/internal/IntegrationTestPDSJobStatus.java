// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import java.util.UUID;

import com.daimler.sechub.integrationtest.JSONTestSupport;
import com.fasterxml.jackson.databind.JsonNode;

public class IntegrationTestPDSJobStatus {
    private static JSONTestSupport jsonTestSupport = JSONTestSupport.DEFAULT;
    public UUID jobUUID;

    public String owner;

    public String created;
    public String started;
    public String ended;

    public String state;

    public static IntegrationTestPDSJobStatus fromJson(String json) {
        return new IntegrationTestPDSJobStatus().internalFromJson(json);
    }

    private IntegrationTestPDSJobStatus internalFromJson(String json) {
        try {
            JsonNode jsonNode = jsonTestSupport.fromJson(json);
            JsonNode jobUUIDNode = jsonNode.get("jobUUID");
            jobUUID = UUID.fromString(jobUUIDNode.asText("no jobuuid in json"));
            owner = jsonNode.get("owner").asText();
            created = jsonNode.get("created").asText();
            started = jsonNode.get("started").asText();
            ended = jsonNode.get("ended").asText();
            state = jsonNode.get("state").asText();
            
        } catch (Exception e) {
            throw new IllegalStateException("pds status json conversion failed, json was:\n"+json,e);
        }
        
        return this;
    }

}