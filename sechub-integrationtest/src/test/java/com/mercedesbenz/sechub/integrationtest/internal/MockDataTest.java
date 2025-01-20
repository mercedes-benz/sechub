// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.integrationtest.api.TestOnlyForRegularExecution;

@TestOnlyForRegularExecution
class MockDataTest {

    @Test
    void check_mockdata_can_be_accessed_and_contains_expected_values() {
        for (MockData mockData : MockData.values()) {
            /* id */
            assertNotNull(mockData.getId());
            assertEquals(mockData.getId(), mockData.getCombination().getId());
            assertEquals(mockData.name().toLowerCase(), mockData.getCombination().getId());

            /* content exists */
            assertNotNull(mockData.getMockResultFilePath());
            assertNotNull(mockData.getMockDataIdentifier());
        }
    }

}
