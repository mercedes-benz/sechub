// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import com.fasterxml.jackson.databind.JsonNode;
import com.mercedesbenz.sechub.integrationtest.internal.TestJSONHelper;

public class JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator extends AbstractHttpStatusCodeExceptionTestValidator {

    protected static final String FIELD_TIMESTAMP = "timeStamp";
    protected static final String FIELD_DETAILS = "details";
    protected static final String FIELD_MESSAGE = "message";
    protected static final String FIELD_ERROR = "error";
    protected static final String FIELD_STATUS = "status";

    public JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator(HttpStatus expectedHttpStatus) {
        this(new HttpStatus[] { expectedHttpStatus });
    }

    JsonErrorFieldNamesAvailableHttpStatusExceptionTestValidator(HttpStatus... expectedHttpStatus) {
        super(expectedHttpStatus);
        for (HttpStatus status : expectedHttpStatus) {
            if (status == null) {
                throw new IllegalArgumentException("Testcase corrupt, given http status may not be null!");
            }
        }
    }

    @Override
    protected void customValidate(HttpStatusCodeException exception) {
        JsonNode tree = validateAndFetchJson(exception);

        Map<String, String> map = new TreeMap<>();
        convertFieldsToSimpleMap("", tree, map);
        validateJSONMap(map, exception);
    }

    protected JsonNode validateAndFetchJson(HttpStatusCodeException exception) {
        String response = exception.getResponseBodyAsString();
        if (response == null) {
            fail("repsonse is not Json but null!");
        }
        JsonNode node = null;
        try {
            node = TestJSONHelper.get().getMapper().readTree(response.getBytes());
        } catch (IOException e) {
            fail("The returned content is not valid JSON:\n" + response);
        }
        return node;
    }

    protected void validateJSONMap(Map<String, String> map, HttpStatusCodeException exception) {
        validateAllMandatoryFieldsAreFound(map, exception);
        validateStatusField(map, exception);
        validateErrorField(map, exception);
        validateMessageField(map, exception);
        validateTimeStampField(map, exception);
    }

    protected void validateAllMandatoryFieldsAreFound(Map<String, String> map, HttpStatusCodeException exception) {
        assertFieldContained(FIELD_STATUS, map, exception);
        assertFieldContained(FIELD_ERROR, map, exception);
        assertFieldContained(FIELD_MESSAGE, map, exception);
        assertFieldContained(FIELD_TIMESTAMP, map, exception);
    }

    private void assertFieldContained(String fieldName, Map<String, String> map, HttpStatusCodeException exception) {
        boolean contained = map.keySet().contains(fieldName);
        if (!contained) {
            fail("field '" + fieldName + "' is NOT contained inside JSON:\n" + exception.getResponseBodyAsString());
        }
    }

    protected void validateTimeStampField(Map<String, String> map, HttpStatusCodeException exception) {
        String timestamp = map.get(FIELD_TIMESTAMP);
        assertNotNull("timestamp is null", timestamp);
        assertFalse("timestamp is empty", timestamp.trim().isEmpty());
    }

    protected void validateErrorField(Map<String, String> map, HttpStatusCodeException exception) {
        String error = map.get(FIELD_ERROR);
        assertNotNull("error is null", error);
        // check error contains expected reasonphrase
        assertEquals("Status code reason phrase is not used as error inside JSON", "" + HttpStatus.valueOf(exception.getStatusCode().value()).getReasonPhrase(),
                error);

    }

    protected void validateMessageField(Map<String, String> map, HttpStatusCodeException exception) {
        String message = map.get(FIELD_MESSAGE);
        assertNotNull("message is null", message);
        assertFalse("message is empty", message.trim().isEmpty());
    }

    protected void validateStatusField(Map<String, String> map, HttpStatusCodeException exception) {
        String statusAsString = map.get(FIELD_STATUS);
        assertNotNull(statusAsString);

        int statusAsInt = Integer.parseInt(statusAsString);

        if (!isExpectedStatusCode(statusAsInt, getExpectedStatusCodes())) {
            fail("The status code inside JSON is:" + statusAsInt + " but was not expected.\nExpected was one of :" + Arrays.asList(getExpectedStatusCodes()));
        }
    }

    private static void convertFieldsToSimpleMap(String prefix, JsonNode node, Map<String, String> map) {
        Iterator<String> it = node.fieldNames();
        while (it.hasNext()) {
            String fieldName = it.next();

            JsonNode found = node.get(fieldName);
            if (found.isInt()) {
                map.put(prefix + fieldName, "" + found.asInt());
            } else if (found.isTextual()) {
                map.put(prefix + fieldName, found.asText());
            } else {
                convertFieldsToSimpleMap(prefix + "." + fieldName, found, map);
            }
        }
    }
}
