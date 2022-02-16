// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sereco.metadata.SerecoClassification;

public class CheckmarxCategoriesToClassificationConverterTest {

    private CheckmarxCategoriesToClassificationConverter converterToTest;

    @Before
    public void before() {
        converterToTest = new CheckmarxCategoriesToClassificationConverter();
    }

    @Test
    public void convertNullOnNullClassification() {
        assertNull(converterToTest.convert(null, null));
    }

    @Test
    public void convertNullReturnsClassification() {
        assertNotNull(converterToTest.convert(null, new SerecoClassification()));
    }

    @Test
    public void classification_null_values() {
        SerecoClassification classification = converterToTest.convert(";,;,;", new SerecoClassification());
        assertEquals(null, classification.getOwasp());
        assertEquals(null, classification.getPci31());
        assertEquals(null, classification.getPci32());

    }

    @Test
    public void classification_example1() {
        /* execute */
        SerecoClassification classification = converterToTest.convert(
                "PCI DSS v3.2;PCI DSS (3.2) - 6.5.8 - Improper access control,OWASP Top 10 2013;A4-Insecure Direct Object References,OWASP Top 10 2017;A5-Broken Access Control",
                new SerecoClassification());

        assertEquals("6.5.8", classification.getPci32());
        assertEquals("A5", classification.getOwasp()); /* must be latest , so 2017 */

    }

    @Test
    public void classification_example2() {
        /* execute */
        SerecoClassification classification = converterToTest
                .convert("FISMA 2014;Identification And Authentication,NIST SP 800-53;AC-3 Access Enforcement (P1)", new SerecoClassification());

        assertEquals("Identification And Authentication", classification.getFisma());
        assertEquals("AC-3", classification.getNist());

    }

}
