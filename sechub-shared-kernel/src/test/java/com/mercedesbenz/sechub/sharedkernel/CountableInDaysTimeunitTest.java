package com.mercedesbenz.sechub.sharedkernel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class CountableInDaysTimeunitTest {

    @ParameterizedTest
    @ValueSource(strings = { "\"DAY\"", "\"day\"", "\"days\"", "\"DAYS\"" })
    public void from_json_days(String json) throws JsonMappingException, JsonProcessingException {
        /* prepare */
        ObjectMapper objectMapper = new ObjectMapper();

        /* execute */
        CountableInDaysTimeunit unit = objectMapper.readValue(json, CountableInDaysTimeunit.class);

        /* test */
        assertEquals(CountableInDaysTimeunit.DAY, unit);
    }

    @ParameterizedTest
    @ValueSource(strings = { "\"WEEK\"", "\"week\"", "\"weeks\"", "\"WEEKS\"" })
    public void from_json_week(String json) throws JsonMappingException, JsonProcessingException {
        /* prepare */
        ObjectMapper objectMapper = new ObjectMapper();

        /* execute */
        CountableInDaysTimeunit unit = objectMapper.readValue(json, CountableInDaysTimeunit.class);

        /* test */
        assertEquals(CountableInDaysTimeunit.WEEK, unit);
    }

    @ParameterizedTest
    @ValueSource(strings = { "\"MONTH\"", "\"month\"", "\"months\"", "\"MONTHS\"" })
    public void from_json_month(String json) throws JsonMappingException, JsonProcessingException {
        /* prepare */
        ObjectMapper objectMapper = new ObjectMapper();

        /* execute */
        CountableInDaysTimeunit unit = objectMapper.readValue(json, CountableInDaysTimeunit.class);

        /* test */
        assertEquals(CountableInDaysTimeunit.MONTH, unit);
    }

    @ParameterizedTest
    @ValueSource(strings = { "\"YEAR\"", "\"year\"", "\"years\"", "\"YEARS\"" })
    public void from_json_years(String json) throws JsonMappingException, JsonProcessingException {
        /* prepare */
        ObjectMapper objectMapper = new ObjectMapper();

        /* execute */
        CountableInDaysTimeunit unit = objectMapper.readValue(json, CountableInDaysTimeunit.class);

        /* test */
        assertEquals(CountableInDaysTimeunit.YEAR, unit);
    }

    @Test
    void days_as_expected() {
        assertEquals(1, CountableInDaysTimeunit.DAY.getMultiplicatorDays());
        assertEquals(7, CountableInDaysTimeunit.WEEK.getMultiplicatorDays());
        assertEquals(30, CountableInDaysTimeunit.MONTH.getMultiplicatorDays());
        assertEquals(365, CountableInDaysTimeunit.YEAR.getMultiplicatorDays());
    }

}
