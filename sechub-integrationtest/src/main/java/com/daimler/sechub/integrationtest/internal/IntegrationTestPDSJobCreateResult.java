// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import java.util.UUID;

import com.daimler.sechub.integrationtest.JSONTestSupport;
import com.fasterxml.jackson.databind.JsonNode;

public class IntegrationTestPDSJobCreateResult {
    private static JSONTestSupport jsonTestSupport = JSONTestSupport.DEFAULT;
    public UUID jobUUID;

    public static IntegrationTestPDSJobCreateResult fromJson(String json) {
        return new IntegrationTestPDSJobCreateResult().internalFromJson(json);
    }

    private IntegrationTestPDSJobCreateResult internalFromJson(String json) {
        try {
            JsonNode jsonNode = jsonTestSupport.fromJson(json);
            JsonNode jobUUIDNode = jsonNode.get("jobUUID");
            jobUUID = UUID.fromString(jobUUIDNode.asText("no jobuuid in json"));

        } catch (Exception e) {
            throw new IllegalStateException("pds status json conversion failed, json was:\n" + json, e);
        }

        return this;
    }

}