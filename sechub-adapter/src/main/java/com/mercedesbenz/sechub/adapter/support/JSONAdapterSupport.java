// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.support;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mercedesbenz.sechub.adapter.Adapter;
import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.AdapterLogId;
import com.mercedesbenz.sechub.adapter.SpringUtilFactory;
import com.mercedesbenz.sechub.adapter.TraceIdProvider;

public class JSONAdapterSupport {

    private TraceIdProvider provider;
    private Adapter<?> adapter;
    private ObjectMapper objectMapper = SpringUtilFactory.createDefaultObjectMapper();

    /**
     * Should only be used inside tests, because no traceIdProvider or adapter is
     * set!
     */
    public static final JSONAdapterSupport FOR_UNKNOWN_ADAPTER = new JSONAdapterSupport();

    private JSONAdapterSupport() {

    }

    public JSONAdapterSupport(Adapter<?> adapter, TraceIdProvider provider) {
        if (adapter == null) {
            throw new IllegalArgumentException("adapter may not be null!");
        }
        if (provider == null) {
            throw new IllegalArgumentException("trace id provider may not be null!");
        }
        this.adapter = adapter;
        this.provider = provider;
    }

    public Access fetchRootNode(ResponseEntity<String> response) throws AdapterException {
        String content = response.getBody();
        return fetchRootNode(content);
    }

    public Access fetchRootNode(String content) throws AdapterException {
        try {
            JsonNode node = objectMapper.readTree(content);
            if (node == null) {
                throw asAdapterException("Node not readable, so not valid JSON", provider);
            }
            return new Access("", node);

        } catch (IOException e) {
            throw asAdapterException("Was not able read content as JSON", e, provider);
        }
    }

    public Access fetch(String nodeName, ResponseEntity<String> response) throws AdapterException {
        return fetch(nodeName, response.getBody());
    }

    public Access fetch(String nodeName, String content) throws AdapterException {
        return fetch(nodeName, fetchRootNode(content).asNode());
    }

    public Access fetchArray(int index, ArrayNode node) throws AdapterException {
        JsonNode result = node.get(index);
        if (result == null) {
            String message = "Did not find array index '" + index + "' in given json array node!";
            throw asAdapterException(message, provider);
        }
        return new Access("array:" + index, result);
    }

    public Access fetch(String nodeName, JsonNode node) throws AdapterException {
        JsonNode result = node.get(nodeName);
        if (result == null) {
            String message = "Did not find '" + nodeName + "' in given json node!";
            throw asAdapterException(message, provider);
        }
        return new Access(nodeName, result);
    }

    public class Access {

        private JsonNode node;
        private String nodeName;

        public Access(String nodeName, JsonNode node) throws AdapterException {
            if (node == null) {
                throw asAdapterException("Node is null!", provider);
            }
            this.node = node;
            this.nodeName = nodeName;
        }

        public String asText() {
            return asNode().asText();
        }

        public long asLong() {
            return asNode().asLong();
        }

        public JsonNode asNode() {
            return node;
        }

        public Access fetch(String fieldName) throws AdapterException {
            return JSONAdapterSupport.this.fetch(fieldName, asNode());
        }

        public Access fetchArrayElement(int index) throws AdapterException {
            JsonNode element = asArray().get(index);
            if (element == null) {
                throw asAdapterException("No array element found on index=" + index, provider);
            }
            return new Access(nodeName + "[" + index + "]", element);
        }

        public Access fetchArrayElementHaving(String fieldName, Map<String, String> map) throws AdapterException {
            ArrayNode historyArray = asArray();
            JsonNode result = null;
            for (Iterator<JsonNode> elements = historyArray.elements(); elements.hasNext();) {
                JsonNode nextNode = elements.next();
                boolean allMapEntriesFound = evaluateAllMapEntriesFound(map, nextNode);
                if (allMapEntriesFound) {
                    result = nextNode.get(fieldName);
                    break;
                }

            }
            if (result == null) {
                /* not found */
                throw asAdapterException("Was not able to find element '" + fieldName + "'", provider);
            }
            return new Access(fieldName, result);
        }

        private boolean evaluateAllMapEntriesFound(Map<String, String> map, JsonNode nextNode) {
            for (Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String expected = entry.getValue();

                JsonNode nextNodeChild = nextNode.get(key);
                if (nextNodeChild == null) {
                    return false;
                }

                String found = nextNodeChild.asText();

                if (!ObjectUtils.nullSafeEquals(expected, found)) {
                    return false;
                }

            }
            return true;
        }

        public ArrayNode asArray() throws AdapterException {
            if (!node.isArray()) {
                throw asAdapterException("Node '" + nodeName + "' is not an array!", provider);
            }
            return (ArrayNode) node;
        }

    }

    public <T extends Object> T fromJSON(Class<T> wanted, String json) throws AdapterException {
        try {
            return objectMapper.readValue(json, wanted);
        } catch (Exception e) {
            throw asAdapterException("Was not able to process given json", e, provider);
        }
    }

    public String toJSON(Object object) throws AdapterException {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw asAdapterException("Was not able to process given object", e, provider);
        }
    }

    public String toJSON(Map<String, ?> map) throws AdapterException {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw asAdapterException("Was not able to process given map", e, provider);
        }
    }

    private AdapterException asAdapterException(String message, TraceIdProvider provider) {
        return asAdapterException(message, null, provider);
    }

    private AdapterException asAdapterException(String message, Exception e, TraceIdProvider provider) {
        AdapterLogId id = null;
        if (adapter != null) {
            id = adapter.getAdapterLogId(provider);
        } else {
            id = new AdapterLogId("undefined", "unknown");
        }
        return AdapterException.asAdapterException(id, message, e);
    }
}