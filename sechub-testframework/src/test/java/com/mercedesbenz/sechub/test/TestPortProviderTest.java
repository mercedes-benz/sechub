// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

public class TestPortProviderTest {

    private static final int DEFAULT = 666;
    private TestPortProvider providerToTest;
    private SystemPropertyProvider systemPropertyProvider;

    @Before
    public void before() {

        systemPropertyProvider = mock(SystemPropertyProvider.class);

        providerToTest = new TestPortProvider();
        providerToTest.setSystemPropertyProvider(systemPropertyProvider);
    }

    @Test
    public void test_defaultinstance_uses_default_environment_entry_provider() {
        /* test */
        assertTrue(TestPortProvider.DEFAULT_INSTANCE.getSystemPropertyProvider() instanceof TestEnvironmentProvider);
    }

    @Test
    public void test_default_returned_when_property_not_set() {
        /* test */
        assertEquals(DEFAULT, providerToTest.getSystemPropertyOrDefault("test.only.portprovider.property.notset", DEFAULT));
    }

    @Test
    public void test_default_returned_when_value_negative_1() {
        /* prepare */
        when(systemPropertyProvider.getSystemProperty("test.only.portprovider.property.set")).thenReturn("-1");

        /* test */
        assertEquals(DEFAULT, providerToTest.getSystemPropertyOrDefault("test.only.portprovider.property.set", DEFAULT));
    }

    @Test
    public void test_default_returned_when_value_justtext() {
        /* prepare */
        when(systemPropertyProvider.getSystemProperty("test.only.portprovider.property.set")).thenReturn("justtext");

        /* test */
        assertEquals(DEFAULT, providerToTest.getSystemPropertyOrDefault("test.only.portprovider.property.set", DEFAULT));
    }

    @Test
    public void test_42_returned_when_property_set_42() {
        /* prepare */
        when(systemPropertyProvider.getSystemProperty("test.only.portprovider.property.set")).thenReturn("42");

        /* test */
        assertEquals(42, providerToTest.getSystemPropertyOrDefault("test.only.portprovider.property.set", DEFAULT));

    }

}
