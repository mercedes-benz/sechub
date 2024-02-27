// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import static org.junit.Assert.*;

import org.junit.Test;

import com.mercedesbenz.sechub.sereco.ImportParameter;

public class ImportSupportTest {

    /**
     * This test is import for failure handling. When a product is not reachable its
     * result will be an empty string, which shall lead to a critical finding about
     * product problems.
     */
    @Test
    public void an_empty_product_result_can_never_be_imported() {
        /* @formatter:off */
		ImportParameter importParameter = ImportParameter.builder().
				importId("id1").
				importData("").
				productId("productId").
				build();
		/* @formatter:on */
        assertEquals(false, ImportSupport.builder().build().isAbleToImport(importParameter));
    }

    @Test
    public void a_null_product_result_can_never_be_imported() {
        /* @formatter:off */
		ImportParameter importParameter = ImportParameter.builder().
				importId("id1").
				importData(null).
				productId("productId").
				build();
		/* @formatter:on */
        assertEquals(false, ImportSupport.builder().build().isAbleToImport(importParameter));
    }

    @Test
    public void isXMLWorksForSimpleString() {
        assertTrue(ImportSupport.builder().build().isXML("<?xml "));
    }

    @Test
    public void isXMLWorksForSimpleStringButUppercase() {
        assertTrue(ImportSupport.builder().build().isXML("<?XML "));
    }

    @Test
    public void isXMLWorksForSimpleStringWithBOM() {
        // see https://en.wikipedia.org/wiki/Byte_order_mark
        char bom = 65279;
        assertTrue(ImportSupport.builder().build().isXML(bom + "<?xml "));
    }

    @Test
    public void isJSONWorksForSimpleString() {
        assertTrue(ImportSupport.builder().build().isJSON("{ bla }"));
    }

    @Test
    public void isJSONWorksForSimpleStringWithBOM() {
        // see https://en.wikipedia.org/wiki/Byte_order_mark
        char bom = 65279;
        assertTrue(ImportSupport.builder().build().isJSON(bom + "{ bla }"));
    }

}
