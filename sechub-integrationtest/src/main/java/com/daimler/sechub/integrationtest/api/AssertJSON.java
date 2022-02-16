// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.integrationtest.JSONTestSupport;
import com.fasterxml.jackson.databind.JsonNode;

public class AssertJSON {

    private static final Logger LOG = LoggerFactory.getLogger(AssertJSON.class);
    private String plainJson;

    public static AssertJSON assertJson(String json) {
        return new AssertJSON(json);
    }

    private JsonNode rootNode;

    private AssertJSON(String json) {
        try {
            plainJson = json;
            rootNode = JSONTestSupport.DEFAULT.fromJson(json);
        } catch (IOException e) {
            LOG.error("JSON parse problem", e);
            dumpErrorJSON("JSON parse problem", json);
            fail("Not correct JSON - " + e.getMessage());
        }
    }

    public AssertJSONFieldPath fieldPathes() {
        return new AssertJSONFieldPath();
    }

    public class AssertJSONFieldPath {

        private AssertJSONFieldPath() {

        }

        /**
         * Check field path is found - and contains text . Only the FIRST field path is
         * used!
         *
         * @param expectedText
         * @param fields
         * @return
         */
        public AssertJSONFieldPath containsTextValue(String expectedText, String... fields) {
            JsonNode node = findFirstNodeByFieldPathOrFail(fields);
            assertEquals(expectedText, node.asText());
            return this;
        }

        public AssertJSON endFieldPathes() {
            return AssertJSON.this;
        }
    }

    private JsonNode findFirstNodeByFieldPathOrFail(String... fields) {
        JsonNode current = rootNode;
        StringBuilder sb = new StringBuilder();
        for (String field : fields) {
            JsonNode found = current.findValue(field);
            if (found == null) {
                dumpErrorJSON("field not found:" + field + ", path current:" + sb);
                fail("json does not contain field:" + field + ". Current path:" + sb);
            }
            current = found;
            sb.append(field);
            sb.append("->");

        }
        return current;
    }

    private void dumpErrorJSON(String reason) {
        dumpErrorJSON(reason, rootNode.toPrettyString());
    }

    private void dumpErrorJSON(String reason, String json) {
        LOG.error("JSON dump reason:{}\n{}", reason, json);
    }

    public AssertJSON containsText(String string) {
        assertTrue(plainJson.contains(string));
        return this;
    }
}
