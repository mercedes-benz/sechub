package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpStatusCodeException;

class SameJsonHttpStatusExceptionTestValidatorTest {

    @Test
    void the_ordering_of_map_is_not_relevant_for_compares() {
        /* prepare */
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("zorro", "vega");

        SameJsonHttpStatusExceptionTestValidator validatorToTest = new SameJsonHttpStatusExceptionTestValidator(map);

        String json = "{\"zorro\" : \"vega\", \"key1\": \"value1\", \"key2\": \"value2\"}";
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getResponseBodyAsString()).thenReturn(json);

        /* execute + test (no exception) */
        validatorToTest.validate(exception);

    }

    @Test
    void changed_value_throws_assertion_error() {
        /* prepare */
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("zorro", "vega");

        SameJsonHttpStatusExceptionTestValidator validatorToTest = new SameJsonHttpStatusExceptionTestValidator(map);

        String json = "{\"zorro\" : \"vega\", \"key1\": \"value1\", \"key2\": \"value2-changed\"}";
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getResponseBodyAsString()).thenReturn(json);

        /* execute + test */
        assertThrows(AssertionError.class, () -> validatorToTest.validate(exception));

    }

}
