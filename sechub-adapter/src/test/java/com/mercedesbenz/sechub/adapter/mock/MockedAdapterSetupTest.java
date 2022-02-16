// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.mock;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MockedAdapterSetupTest {

    private MockedAdapterSetup setupToTest;

    @Before
    public void before() {
        setupToTest = new MockedAdapterSetup();
    }

    @Test
    public void empty_get_something_is_null() {
        assertNull(setupToTest.getEntryFor("xyz"));
    }

    @Test
    public void non_empty_get_something_not_contained_is_null() {
        /* prepare */
        setupToTest.getEntries().add(new MockedAdapterSetupEntry());

        /* test */
        assertNull(setupToTest.getEntryFor("xyz"));
    }

    @Test
    public void non_empty_get_something_contained_returns_entry() {
        /* prepare */
        MockedAdapterSetupEntry e = new MockedAdapterSetupEntry();
        e.setAdapterId("xyz");

        setupToTest.getEntries().add(e);

        /* test */
        assertEquals(e, setupToTest.getEntryFor("xyz"));
    }

}
