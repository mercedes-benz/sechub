// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

public class TestPortProviderTest {

    private static final int DEFAULT = 666;
    private TestPortProvider providerToTest;
    private EnvironmentEntryProvider mockedEnvironmentEntryProvider;

    @Before
    public void before() {

        mockedEnvironmentEntryProvider = mock(EnvironmentEntryProvider.class);

        providerToTest = new TestPortProvider();
        providerToTest.setEnvironmentEntryProvider(mockedEnvironmentEntryProvider);
    }

    @Test
    public void test_defaultinstance_uses_default_environment_entry_provider() {
        /* test */
        assertTrue(TestPortProvider.DEFAULT_INSTANCE.getEnvProvider() instanceof DefaultEnvironmentEntryProvider);
    }

    @Test
    public void test_default_returned_when_property_not_set() {
        /* test */
        assertEquals(DEFAULT, providerToTest.getEnvOrDefault("test.only.portprovider.property.notset", DEFAULT));
    }

    @Test
    public void test_default_returned_when_value_negative_1() {
        /* prepare */
        when(mockedEnvironmentEntryProvider.getEnvEntry("test.only.portprovider.property.set")).thenReturn("-1");
        /* test */
        assertEquals(DEFAULT, providerToTest.getEnvOrDefault("test.only.portprovider.property.set", DEFAULT));
    }

    @Test
    public void test_default_returned_when_value_justtext() {
        /* prepare */
        when(mockedEnvironmentEntryProvider.getEnvEntry("test.only.portprovider.property.set")).thenReturn("justtext");
        /* test */
        assertEquals(DEFAULT, providerToTest.getEnvOrDefault("test.only.portprovider.property.set", DEFAULT));
    }

    @Test
    public void test_42_returned_when_property_set_42() {
        /* prepare */
        when(mockedEnvironmentEntryProvider.getEnvEntry("test.only.portprovider.property.set")).thenReturn("42");
        /* test */
        assertEquals(42, providerToTest.getEnvOrDefault("test.only.portprovider.property.set", DEFAULT));

    }

}
