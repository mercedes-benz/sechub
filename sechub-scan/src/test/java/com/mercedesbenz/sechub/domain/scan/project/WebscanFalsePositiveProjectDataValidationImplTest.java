// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.sharedkernel.validation.ValidationResult;

class WebscanFalsePositiveProjectDataValidationImplTest {

    private static final String METHODS_FIELD_NAME = FalsePositiveProjectData.PROPERTY_WEBSCAN + "." + WebscanFalsePositiveProjectData.PROPERTY_METHODS + "[]";
    private static final String URL_PATTERN_FIELD_NAME = FalsePositiveProjectData.PROPERTY_WEBSCAN + "." + WebscanFalsePositiveProjectData.PROPERTY_URLPATTERN;

    private WebscanFalsePositiveProjectDataValidationImpl validationToTest;

    @BeforeEach
    void beforeEach() {
        validationToTest = new WebscanFalsePositiveProjectDataValidationImpl();
    }

    @Test
    void no_webscan_inside_project_data_validation_returns_valid_result() {
        /* prepare */
        WebscanFalsePositiveProjectData webScan = null;

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        // is valid because we might have other types inside
        // FalsePositiveProjectDataValidation.java besides
        // WebscanFalsePositiveProjectData.java in the future so webScan can be null if
        // not needed
        /* test */
        assertTrue(result.isValid());
    }

    @Test
    void without_optional_parts_returns_valid_result() {
        /* prepare */
        WebscanFalsePositiveProjectData webScan = createWebscanFalsePositiveProjectDataWithValidMandatoryParts();

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        /* test */
        assertTrue(result.isValid());
        assertEquals(0, webScan.getCweId());
    }

    @Test
    void too_long_optional_lists_returns_invalid_result() {
        /* prepare */
        List<String> methods = createTooLongListOfStrings();

        WebscanFalsePositiveProjectData webScan = createWebscanFalsePositiveProjectDataWithValidMandatoryParts();
        webScan.setMethods(methods);

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        /* test */
        assertFalse(result.isValid());
        List<String> errors = result.getErrors();
        assertEquals(1, errors.size());

        assertTrue(errors.get(0).contains(METHODS_FIELD_NAME));
    }

    @Test
    void too_long_list_entry_in_optional_lists_returns_invalid_result() {
        /* prepare */
        String tooLongEntry = "a".repeat(41);

        List<String> methods = new ArrayList<>();
        methods.add(tooLongEntry);

        WebscanFalsePositiveProjectData webScan = createWebscanFalsePositiveProjectDataWithValidMandatoryParts();
        webScan.setMethods(methods);

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        /* test */
        assertFalse(result.isValid());
        List<String> errors = result.getErrors();
        assertEquals(1, errors.size());

        assertTrue(errors.get(0).contains(METHODS_FIELD_NAME));
    }

    @Test
    void mandatory_urlPattern_null_returns_invalid_result() {
        /* prepare */
        WebscanFalsePositiveProjectData webScan = new WebscanFalsePositiveProjectData();
        webScan.setUrlPattern(null);

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        /* test */
        assertFalse(result.isValid());
        List<String> errors = result.getErrors();
        assertEquals(1, errors.size());

        assertTrue(errors.get(0).contains(URL_PATTERN_FIELD_NAME));
    }

    @Test
    void mandatory_mandatory_urlPattern_blank_returns_invalid_result() {
        /* prepare */
        WebscanFalsePositiveProjectData webScan = new WebscanFalsePositiveProjectData();
        webScan.setUrlPattern("       ");

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        /* test */
        assertFalse(result.isValid());
        List<String> errors = result.getErrors();
        assertEquals(1, errors.size());

        assertTrue(errors.get(0).contains(URL_PATTERN_FIELD_NAME));
    }

    @Test
    void urlPattern_containing_backslashes_return_invalid_result() {
        /* prepare */
        WebscanFalsePositiveProjectData webScan = createWebscanFalsePositiveProjectDataWithValidMandatoryParts();
        webScan.setUrlPattern("https://myapp-*.example.com:80*/rest/*/search?\\*");

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        /* test */
        assertFalse(result.isValid());
        List<String> errors = result.getErrors();
        assertEquals(1, errors.size());

        assertTrue(errors.get(0).contains("no backslashes are allowed"));
    }

    private WebscanFalsePositiveProjectData createWebscanFalsePositiveProjectDataWithValidMandatoryParts() {
        WebscanFalsePositiveProjectData webScan = new WebscanFalsePositiveProjectData();
        webScan.setUrlPattern("https://myapp-*.example.com:80*/rest/*/search?*");
        return webScan;
    }

    private List<String> createTooLongListOfStrings() {
        List<String> list = new LinkedList<>();
        for (int i = 0; i < 31; i++) {
            list.add("a");
        }
        return list;
    }
}
