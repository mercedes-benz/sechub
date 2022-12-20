// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

public class TestPortProviderTest {

    private static final String TEST_PROPERTY = "test.only.portprovider.property.set";
    private static final int DEFAULT = 666;
    private TestPortProvider portProviderToTest;
    private SystemPropertyProvider systemPropertyProvider;

    @Before
    public void before() {

        systemPropertyProvider = mock(SystemPropertyProvider.class);

        portProviderToTest = new TestPortProvider();
        portProviderToTest.setSystemPropertyProvider(systemPropertyProvider);
    }

    @Test
    public void test_defaultinstance_uses_default_environment_entry_provider() {
        /* test */
        assertTrue(TestPortProvider.DEFAULT_INSTANCE.getSystemPropertyProvider() instanceof TestEnvironmentProvider);
    }

    @Test
    public void test_default_returned_when_property_not_set() {
        /* test */
        assertEquals(DEFAULT, portProviderToTest.getSystemPropertyOrDefault("test.only.portprovider.property.notset", DEFAULT));
    }

    @Test
    public void test_default_returned_when_value_negative_1() {
        /* prepare */
        when(systemPropertyProvider.getSystemProperty(TEST_PROPERTY)).thenReturn("-1");

        /* test */
        assertEquals(DEFAULT, portProviderToTest.getSystemPropertyOrDefault(TEST_PROPERTY, DEFAULT));
    }

    @Test
    public void test_default_returned_when_value_justtext() {
        /* prepare */
        when(systemPropertyProvider.getSystemProperty(TEST_PROPERTY)).thenReturn("justtext");

        /* test */
        assertEquals(DEFAULT, portProviderToTest.getSystemPropertyOrDefault(TEST_PROPERTY, DEFAULT));
    }

    @Test
    public void test_42_returned_when_property_set_42() {
        /* prepare */
        when(systemPropertyProvider.getSystemProperty(TEST_PROPERTY)).thenReturn("42");

        /* test */
        assertEquals(42, portProviderToTest.getSystemPropertyOrDefault(TEST_PROPERTY, DEFAULT));

    }

    @Test
    public void system_property_as_empty_string_is_returning_default() {
        /* prepare */
        when(systemPropertyProvider.getSystemProperty(TEST_PROPERTY)).thenReturn("");

        /* test */
        assertEquals(DEFAULT, portProviderToTest.getSystemPropertyOrDefault(TEST_PROPERTY, DEFAULT));

    }

    @Test
    public void system_property_as_null_is_returning_default() {
        /* prepare */
        when(systemPropertyProvider.getSystemProperty(TEST_PROPERTY)).thenReturn(null);

        /* test */
        assertEquals(DEFAULT, portProviderToTest.getSystemPropertyOrDefault(TEST_PROPERTY, DEFAULT));

    }

}
