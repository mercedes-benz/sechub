// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.config;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.domain.scan.ScanDomainTestFileSupport;

@SuppressWarnings("deprecation") // we explicit use the deprecated method, because it is only for tests..
public class ScanConfigServiceTest {

    private ScanConfigService serviceToTest;

    @Before
    public void before() {
        serviceToTest = new ScanConfigService();
    }

    @Test
    public void not_initialized_returns_fallback_provider() {
        /* execute */
        NamePatternIdprovider provider = serviceToTest.getNamePatternIdProvider("xyz");
        /* test */
        assertNotNull(provider);
        assertEquals(null, provider.getIdForName("a-go-project-1"));
    }

    @Test
    public void initialized_example1_returns_expected_provider() {
        /* prepare */
        serviceToTest.switchConfigurationIfChanged(
                ScanConfig.createFromJSON(ScanDomainTestFileSupport.getTestfileSupport().loadTestFile("scan_config/test_scan_config1.json")));

        /* execute */
        NamePatternIdprovider provider = serviceToTest.getNamePatternIdProvider("someproduct.a.b");
        /* test */
        assertNotNull(provider);
        assertEquals("2", provider.getIdForName("a-go-project-1"));
        assertEquals("3", provider.getIdForName("other"));

        provider = serviceToTest.getNamePatternIdProvider("someproduct.c.d");
        assertNotNull(provider);
        assertEquals("20", provider.getIdForName("a-go-project-1"));
        assertEquals("10", provider.getIdForName("my-java-project-xyz"));

        provider = serviceToTest.getNamePatternIdProvider("someproduct.unknown");
        assertNotNull(provider);
        assertEquals(null, provider.getIdForName("a-go-project-1"));

    }

    @Test
    public void initialized_example2_contains_unknwon_fields_but_will_also_returns_expected_provider() {
        /* prepare */
        serviceToTest.switchConfigurationIfChanged(
                ScanConfig.createFromJSON(ScanDomainTestFileSupport.getTestfileSupport().loadTestFile("scan_config/test_scan_config2.json")));

        /* execute */
        NamePatternIdprovider provider = serviceToTest.getNamePatternIdProvider("someproduct.a.b");
        /* test */
        assertNotNull(provider);
        assertEquals("102", provider.getIdForName("a-go-project-1"));
        assertEquals("103", provider.getIdForName("other"));

        provider = serviceToTest.getNamePatternIdProvider("someproduct.c.d");
        assertNotNull(provider);
        assertEquals("20", provider.getIdForName("a-go-project-1"));
        assertEquals("10", provider.getIdForName("my-java-project-xyz"));

        provider = serviceToTest.getNamePatternIdProvider("someproduct.unknown");
        assertNotNull(provider);
        assertEquals(null, provider.getIdForName("a-go-project-1"));

    }

}
