package com.mercedesbenz.sechub.sharedkernel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class CountableInDaysTimeUnitTest {

    @ParameterizedTest
    @ValueSource(strings = { "\"DAY\"", "\"day\"", "\"days\"", "\"DAYS\"" })
    public void from_json_days(String json) throws JsonMappingException, JsonProcessingException {
        /* prepare */
        ObjectMapper objectMapper = new ObjectMapper();

        /* execute */
        CountableInDaysTimeUnit unit = objectMapper.readValue(json, CountableInDaysTimeUnit.class);

        /* test */
        assertEquals(CountableInDaysTimeUnit.DAY, unit);
    }

    @ParameterizedTest
    @ValueSource(strings = { "\"WEEK\"", "\"week\"", "\"weeks\"", "\"WEEKS\"" })
    public void from_json_week(String json) throws JsonMappingException, JsonProcessingException {
        /* prepare */
        ObjectMapper objectMapper = new ObjectMapper();

        /* execute */
        CountableInDaysTimeUnit unit = objectMapper.readValue(json, CountableInDaysTimeUnit.class);

        /* test */
        assertEquals(CountableInDaysTimeUnit.WEEK, unit);
    }

    @ParameterizedTest
    @ValueSource(strings = { "\"MONTH\"", "\"month\"", "\"months\"", "\"MONTHS\"" })
    public void from_json_month(String json) throws JsonMappingException, JsonProcessingException {
        /* prepare */
        ObjectMapper objectMapper = new ObjectMapper();

        /* execute */
        CountableInDaysTimeUnit unit = objectMapper.readValue(json, CountableInDaysTimeUnit.class);

        /* test */
        assertEquals(CountableInDaysTimeUnit.MONTH, unit);
    }

    @ParameterizedTest
    @ValueSource(strings = { "\"YEAR\"", "\"year\"", "\"years\"", "\"YEARS\"" })
    public void from_json_years(String json) throws JsonMappingException, JsonProcessingException {
        /* prepare */
        ObjectMapper objectMapper = new ObjectMapper();

        /* execute */
        CountableInDaysTimeUnit unit = objectMapper.readValue(json, CountableInDaysTimeUnit.class);

        /* test */
        assertEquals(CountableInDaysTimeUnit.YEAR, unit);
    }

    @Test
    void days_as_expected() {
        assertEquals(1, CountableInDaysTimeUnit.DAY.getMultiplicatorDays());
        assertEquals(7, CountableInDaysTimeUnit.WEEK.getMultiplicatorDays());
        assertEquals(30, CountableInDaysTimeUnit.MONTH.getMultiplicatorDays());
        assertEquals(365, CountableInDaysTimeUnit.YEAR.getMultiplicatorDays());
    }

}
