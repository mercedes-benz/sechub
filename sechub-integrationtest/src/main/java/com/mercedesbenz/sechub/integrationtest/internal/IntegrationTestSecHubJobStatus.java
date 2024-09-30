// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.integrationtest.JSONTestSupport;

public class IntegrationTestSecHubJobStatus {
    private static JSONTestSupport jsonTestSupport = JSONTestSupport.DEFAULT;
    public UUID jobUUID;

    public String owner;

    public String created;
    public String started;
    public String ended;

    public String state;

    public List<SecHubMessage> messages;

    public static IntegrationTestSecHubJobStatus fromJson(String json) {
        return new IntegrationTestSecHubJobStatus().internalFromJson(json);
    }

    private IntegrationTestSecHubJobStatus internalFromJson(String json) {
        try {
            JsonNode jsonNode = jsonTestSupport.fromJson(json);
            JsonNode jobUUIDNode = jsonNode.get("jobUUID");
            jobUUID = UUID.fromString(jobUUIDNode.asText("no jobuuid in json"));
            owner = jsonNode.get("owner").asText();
            created = jsonNode.get("created").asText();
            started = jsonNode.get("started").asText();
            ended = jsonNode.get("ended").asText();
            state = jsonNode.get("state").asText();

            JsonNode messagesNode = jsonNode.get("messages");
            if (messagesNode != null && messagesNode.isArray()) {
                messages = new ArrayList<>();
                ArrayNode arrayNode = (ArrayNode) messagesNode;
                Iterator<JsonNode> elementIterator = arrayNode.elements();
                while (elementIterator.hasNext()) {
                    JsonNode element = elementIterator.next();
                    String type = element.get("type").asText();
                    JsonNode textNode = element.get("text");
                    String text = null;
                    if (textNode != null) {
                        text = textNode.asText();
                    }
                    SecHubMessage message = new SecHubMessage();
                    message.setType(SecHubMessageType.valueOf(type.toUpperCase()));
                    message.setText(text);

                    messages.add(message);
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException("pds status json conversion failed, json was:\n" + json, e);
        }

        return this;
    }

}