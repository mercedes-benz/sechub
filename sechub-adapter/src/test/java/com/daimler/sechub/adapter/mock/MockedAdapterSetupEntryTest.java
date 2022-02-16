// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.mock;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MockedAdapterSetupEntryTest {

    private MockedAdapterSetupEntry entryToTest;

    @Before
    public void before() {
        entryToTest = new MockedAdapterSetupEntry();
    }

    @Test
    public void empty_but_combinations_not_null() {
        assertNotNull(entryToTest.getCombinations());
    }

    @Test
    public void any_other_not_defined_file_path_xyz_returns_null() {
        assertEquals(null, entryToTest.getResultFilePathFor("xyz"));
    }

    @Test
    public void any_other_defined_file_path_xyz_returns_any_other() {
        addAnyOtherCombination();

        /* test */
        assertEquals("filepath-any", entryToTest.getResultFilePathFor("xyz"));
    }

    @Test
    public void not_defined_throws_adapter_exceptions_returns_flase() {
        assertFalse(entryToTest.isThrowingAdapterExceptionFor("xyz"));
    }

    @Test
    public void any_other_not_defined_defined_filePpath_returns_path() {
        /* prepare */
        MockedAdapterSetupCombination combination = new MockedAdapterSetupCombination();
        combination.setFilePath("bla");
        combination.setTarget("xyz");
        entryToTest.getCombinations().add(combination);

        /* test */
        assertEquals("bla", entryToTest.getResultFilePathFor("xyz"));
    }

    @Test
    public void defined_throws_adapter_exceptions_returns_true() {
        /* prepare */
        MockedAdapterSetupCombination combination = new MockedAdapterSetupCombination();
        combination.setTarget("xyz");
        combination.setThrowsAdapterException(true);

        entryToTest.getCombinations().add(combination);

        /* test */
        assertTrue(entryToTest.isThrowingAdapterExceptionFor("xyz"));
    }

    @Test
    public void any_other_defined_defined_filePpath_returns_path() {
        /* prepare */
        MockedAdapterSetupCombination combination2 = new MockedAdapterSetupCombination();
        combination2.setFilePath("bla");
        combination2.setTarget("xyz");
        entryToTest.getCombinations().add(combination2);
        addAnyOtherCombination();

        /* test */
        assertEquals("bla", entryToTest.getResultFilePathFor("xyz"));
    }

    private void addAnyOtherCombination() {
        MockedAdapterSetupCombination combination = new MockedAdapterSetupCombination();
        combination.setTarget(MockedAdapterSetupCombination.ANY_OTHER_TARGET);
        combination.setFilePath("filepath-any");

        entryToTest.getCombinations().add(combination);
    }
}
