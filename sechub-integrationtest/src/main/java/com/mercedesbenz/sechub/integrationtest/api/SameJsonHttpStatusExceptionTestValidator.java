package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.web.client.HttpStatusCodeException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mercedesbenz.sechub.integrationtest.internal.TestJSONHelper;

public class SameJsonHttpStatusExceptionTestValidator implements HttpStatusCodeExceptionTestValidator {

    private String expectedJson;

    public SameJsonHttpStatusExceptionTestValidator(Map<String, Object> expectedMap) {
        this.expectedJson = TestJSONHelper.get().createJSON(expectedMap);
    }

    public SameJsonHttpStatusExceptionTestValidator(String expectedJson) {
        this.expectedJson = expectedJson;
    }

    @Override
    public void validate(HttpStatusCodeException exception) {
        if (exception == null) {
            fail("no exception!??!?");
        }

        String responseBody = exception.getResponseBodyAsString();

        String orderedAndformattedResponseJson = createFormattedAndOrderedJSON(responseBody);
        String orderedAndFormattedExpectedJson = createFormattedAndOrderedJSON(expectedJson);

        if (!orderedAndFormattedExpectedJson.equals(orderedAndformattedResponseJson)) {
            String message = "Json body not as expected.\nException message was:" + exception.getMessage();
            assertEquals(message, orderedAndFormattedExpectedJson, orderedAndformattedResponseJson);
        }

    }

    private String createFormattedAndOrderedJSON(String json) {
        String formattedResponseJson = null;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> unsortedMap = TestJSONHelper.get().getMapper().readValue(json, Map.class);
            SortedMap<String, Object> treeMap = new TreeMap<>(unsortedMap);
            formattedResponseJson = TestJSONHelper.get().createJSON(treeMap, true);

        } catch (JsonProcessingException e) {
            System.err.println("response body illegal Json:");
            System.err.println(json);

            throw new IllegalStateException("Was not able to parse given json from response body, look at console output for content");
        }
        return formattedResponseJson;
    }

}
