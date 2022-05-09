package com.mercedesbenz.sechub.commons.archive;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class MutableArchiveTransformationDataTest {

    private MutableArchiveTransformationData dataToTest;

    @BeforeEach
    void beforeEach() {
        dataToTest = new MutableArchiveTransformationData();
    }

    @Test
    void check_initial_defaults_as_expected() {
        /* test */
        assertFalse(dataToTest.isAccepted());

        assertNull(dataToTest.getChangedPath());
        assertFalse(dataToTest.isPathChangeWanted());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void set_accepted_works(boolean accepted) {
        /* execute */
        dataToTest.setAccepted(accepted);

        /* test */
        assertEquals(accepted, dataToTest.isAccepted());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/path/toSomething.txt", "path/anotherone", "" })
    @NullSource
    void set_wanted_path_has_value_and_says_path_change_is_wanted_when_path_is_not_null(String path) {
        /* execute */
        dataToTest.setWantedPath(path);

        /* test */
        assertEquals(path, dataToTest.getChangedPath());
        if (path == null) {
            assertFalse(dataToTest.isPathChangeWanted());
        } else {
            assertTrue(dataToTest.isPathChangeWanted());
        }
    }

}
