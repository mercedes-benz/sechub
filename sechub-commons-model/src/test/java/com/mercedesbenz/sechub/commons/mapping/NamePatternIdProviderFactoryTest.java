package com.mercedesbenz.sechub.commons.mapping;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class NamePatternIdProviderFactoryTest {

    private NamePatternIdProviderFactory factoryToTest;

    @BeforeEach
    public void beforeEach() throws Exception {
        factoryToTest = new NamePatternIdProviderFactory();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", "{}", "  " })
    void null_json_results_in_working_provider_which_returns_null_for_ids(String json) {

        /* execute */
        NamePatternIdProvider result = factoryToTest.createProvider("the.id", json);

        /* test */
        assertEquals(null, result.getIdForName("something")); // null returned, but no problems
    }

}
