// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

public class SecHubTimeUnitTest {
    @Test
    public void get_multiplicator_milliseconds__get_millisecond() {
        /* execute */
        SecHubTimeUnit unit = SecHubTimeUnit.MILLISECOND;

        /* test */
        assertNotNull(unit);
        assertEquals(1, unit.getMultiplicatorMilliseconds());
    }

    @Test
    public void value_of__millisecond() {
        /* execute */
        SecHubTimeUnit unit = SecHubTimeUnit.valueOf("MILLISECOND");

        /* test */
        assertEquals(SecHubTimeUnit.MILLISECOND, unit);
    }

    @Test
    public void value_of__null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> {
            SecHubTimeUnit.valueOf(null);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = { "\"MILLISECOND\"", "\"millisecond\"", "\"milliseconds\"", "\"MILLISECONDS\"" })
    public void from_json_millisecond(String json) throws JsonMappingException, JsonProcessingException {
        /* prepare */
        ObjectMapper objectMapper = new ObjectMapper();

        /* execute */
        SecHubTimeUnit unit = objectMapper.readValue(json, SecHubTimeUnit.class);

        /* test */
        assertEquals(SecHubTimeUnit.MILLISECOND, unit);
    }

    @ParameterizedTest
    @ValueSource(strings = { "\"SECOND\"", "\"second\"", "\"seconds\"", "\"SECONDS\"" })
    public void from_json_second(String json) throws JsonMappingException, JsonProcessingException {
        /* prepare */
        ObjectMapper objectMapper = new ObjectMapper();

        /* execute */
        SecHubTimeUnit unit = objectMapper.readValue(json, SecHubTimeUnit.class);

        /* test */
        assertEquals(SecHubTimeUnit.SECOND, unit);
    }

    @ParameterizedTest
    @ValueSource(strings = { "\"MINUTE\"", "\"minute\"", "\"minutes\"", "\"MINUTES\"" })
    public void from_json_minute(String json) throws JsonMappingException, JsonProcessingException {
        /* prepare */
        ObjectMapper objectMapper = new ObjectMapper();

        /* execute */
        SecHubTimeUnit unit = objectMapper.readValue(json, SecHubTimeUnit.class);

        /* test */
        assertEquals(SecHubTimeUnit.MINUTE, unit);
    }

    @ParameterizedTest
    @ValueSource(strings = { "\"HOUR\"", "\"hour\"", "\"hours\"", "\"HOURS\"" })
    public void from_json_hours(String json) throws JsonMappingException, JsonProcessingException {
        /* prepare */
        ObjectMapper objectMapper = new ObjectMapper();

        /* execute */
        SecHubTimeUnit unit = objectMapper.readValue(json, SecHubTimeUnit.class);

        /* test */
        assertEquals(SecHubTimeUnit.HOUR, unit);
    }

    @ParameterizedTest
    @ValueSource(strings = { "\"DAY\"", "\"day\"", "\"days\"", "\"DAYS\"" })
    public void from_json_days(String json) throws JsonMappingException, JsonProcessingException {
        /* prepare */
        ObjectMapper objectMapper = new ObjectMapper();

        /* execute */
        SecHubTimeUnit unit = objectMapper.readValue(json, SecHubTimeUnit.class);

        /* test */
        assertEquals(SecHubTimeUnit.DAY, unit);
    }

    @Test
    public void from_json_months() throws JsonMappingException, JsonProcessingException {
        /* prepare */
        String json = "\"months\"";
        ObjectMapper objectMapper = new ObjectMapper();

        /* execute + test */
        assertThrows(InvalidFormatException.class, () -> {
            objectMapper.readValue(json, SecHubTimeUnit.class);
        });
    }
}
