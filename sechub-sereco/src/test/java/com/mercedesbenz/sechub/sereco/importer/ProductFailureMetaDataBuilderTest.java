// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.sereco.ImportParameter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoAnnotation;
import com.mercedesbenz.sechub.sereco.metadata.SerecoAnnotationType;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

public class ProductFailureMetaDataBuilderTest {

    private ProductFailureMetaDataBuilder builderToTest;

    @BeforeEach
    void beforeEach() throws Exception {
        builderToTest = new ProductFailureMetaDataBuilder();
    }

    @Test
    void creates_a_meta_model_with_product_information() {
        /* prepare */
        ImportParameter param = ImportParameter.builder().importId("id1").productId("productId").build();

        /* execute */
        SerecoMetaData result = builderToTest.forParam(param).build();

        /* test */
        assertResultHasProductFailureAnnotationButNoVulnerabilities(result);
    }

    @Test
    void creates_a_meta_model_without_product_information_at_least_works() {
        /* prepare */
        ImportParameter param = ImportParameter.builder().build();

        /* execute */
        SerecoMetaData result = builderToTest.forParam(param).build();

        /* test */
        assertResultHasProductFailureAnnotationButNoVulnerabilities(result);
    }
    
    @Test
    void when_canceled_creates_a_meta_model_without_product_information_at_least_works() {
        /* prepare */
        ImportParameter param = ImportParameter.builder().canceled(true).build();
        
        /* execute */
        SerecoMetaData result = builderToTest.forParam(param).build();
        
        /* test */
        assertResultHasProductCanceledAnnotationButNoVulnerabilities(result);
    }

    private void assertResultHasProductFailureAnnotationButNoVulnerabilities(SerecoMetaData result) {
        assertResultHasProductFailureAnnotationButNoVulnerabilities(result, false);
    }
    
   private void assertResultHasProductCanceledAnnotationButNoVulnerabilities(SerecoMetaData result) {
       assertResultHasProductFailureAnnotationButNoVulnerabilities(result, true);
        
    }
    private void assertResultHasProductFailureAnnotationButNoVulnerabilities(SerecoMetaData result, boolean canceled) {
        assertNotNull(result);

        // no vulnerabilities inside
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();
        assertNotNull(vulnerabilities);
        assertEquals(0, vulnerabilities.size());

        // annotation added about product failure
        Set<SerecoAnnotation> annotations = result.getAnnotations();
        assertEquals(1, annotations.size());
        SerecoAnnotation anno = annotations.iterator().next();

        if (canceled) {
            assertEquals(SerecoAnnotationType.USER_INFO, anno.getType());
        }else {
            assertEquals(SerecoAnnotationType.INTERNAL_ERROR_PRODUCT_FAILED, anno.getType());
        }
    }
}
