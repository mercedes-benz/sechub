// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.docgen.DocAnnotationData;

public class AnnotationDataLocationExtractorTest {

    private AnnotationDataLocationExtractor extractorToTest;

    @Before
    public void before() throws Exception {
        extractorToTest = new AnnotationDataLocationExtractor();
    }

    @Test
    public void null_results_in_empty_string() throws Exception {

        /* execute */
        String result = extractorToTest.extractLocation(null);

        /* test */
        assertEquals("", result);

    }

    @Test
    public void empty_data_results_in_empty_string() throws Exception {

        /* prepare */
        DocAnnotationData data = new DocAnnotationData();

        /* execute */
        String result = extractorToTest.extractLocation(data);

        /* test */
        assertEquals("", result);

    }

    @Test
    public void data_with_field_results_in_correct_string() throws Exception {

        /* prepare */
        DocAnnotationData data = new DocAnnotationData();
        data.linkedField = getClass().getDeclaredField("extractorToTest");

        /* execute */
        String result = extractorToTest.extractLocation(data);

        /* test */
        assertEquals("Field:AnnotationDataLocationExtractorTest.extractorToTest", result);

    }

    @Test
    public void data_with_method_results_in_correct_string() throws Exception {

        /* prepare */
        DocAnnotationData data = new DocAnnotationData();
        data.linkedMethod = getClass().getMethod("data_with_method_results_in_correct_string");

        /* execute */
        String result = extractorToTest.extractLocation(data);

        /* test */
        assertEquals("Method:AnnotationDataLocationExtractorTest#data_with_method_results_in_correct_string", result);

    }

    @Test
    public void data_with_class_results_in_correct_string() throws Exception {

        /* prepare */
        DocAnnotationData data = new DocAnnotationData();
        data.linkedClass = AnnotationDataLocationExtractorTest.class;

        /* execute */
        String result = extractorToTest.extractLocation(data);

        /* test */
        assertEquals("Class:AnnotationDataLocationExtractorTest", result);

    }

}
