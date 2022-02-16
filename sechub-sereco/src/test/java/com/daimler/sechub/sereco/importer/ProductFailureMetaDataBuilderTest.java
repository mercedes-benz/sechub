// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sereco.ImportParameter;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

public class ProductFailureMetaDataBuilderTest {

    private ProductFailureMetaDataBuilder builderToTest;

    @Before
    public void before() throws Exception {
        builderToTest = new ProductFailureMetaDataBuilder();
    }

    @Test
    public void creates_a_meta_model_with_product_information() {
        /* prepare */
        ImportParameter param = ImportParameter.builder().importId("id1").productId("productId").build();

        /* execute */
        SerecoMetaData result = builderToTest.forParam(param).build();

        /* test */
        assertNotNull(result);
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();
        assertNotNull(vulnerabilities);
        assertEquals(1, vulnerabilities.size());
        SerecoVulnerability v = vulnerabilities.iterator().next();

        assertEquals(SerecoSeverity.CRITICAL, v.getSeverity());
        assertEquals("SecHub failure", v.getType());
        assertEquals("Security product 'productId' failed, so cannot give a correct answer.", v.getDescription());
    }

    @Test
    public void creates_a_meta_model_with_product_information_nothing_set_will_at_least_work() {
        /* prepare */
        ImportParameter param = ImportParameter.builder().build();

        /* execute */
        SerecoMetaData result = builderToTest.forParam(param).build();

        /* test */
        assertNotNull(result);
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();
        assertNotNull(vulnerabilities);
        assertEquals(1, vulnerabilities.size());
        SerecoVulnerability v = vulnerabilities.iterator().next();

        assertEquals(SerecoSeverity.CRITICAL, v.getSeverity());
        assertEquals("SecHub failure", v.getType());
        assertEquals("Security product 'null' failed, so cannot give a correct answer.", v.getDescription());
    }

}
