// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DefaultEnvironmentEntryProviderTest {

    private DefaultEnvironmentEntryProvider providerToTest;

    @Before
    public void before() {
        providerToTest = new DefaultEnvironmentEntryProvider();
    }

    @Test
    public void environment_entry_path_is_returned_from_provider_as_would_be_by_system_class() {
        /* prepare */
        String pathValue = System.getenv("PATH");
        if (pathValue == null) {
            throw new IllegalStateException("PATH should be always available, no matter if linux or windows!");
        }

        /* test */
        assertEquals(pathValue, providerToTest.getEnvEntry("PATH"));

    }

}
