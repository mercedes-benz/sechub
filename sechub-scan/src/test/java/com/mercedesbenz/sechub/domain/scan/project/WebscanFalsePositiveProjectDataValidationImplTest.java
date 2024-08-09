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

    private static final String methodsFieldName = FalsePositiveProjectData.PROPERTY_WEBSCAN + "." + WebscanFalsePositiveProjectData.PROPERTY_METHODS + "[]";
    private static final String portsFieldName = FalsePositiveProjectData.PROPERTY_WEBSCAN + "." + WebscanFalsePositiveProjectData.PROPERTY_PORTS + "[]";
    private static final String protocolsFieldName = FalsePositiveProjectData.PROPERTY_WEBSCAN + "." + WebscanFalsePositiveProjectData.PROPERTY_PROTOCOLS
            + "[]";
    private static final String hostPatternsFieldName = FalsePositiveProjectData.PROPERTY_WEBSCAN + "." + WebscanFalsePositiveProjectData.PROPERTY_HOSTPATTERNS
            + "[]";
    private static final String urlPatternsFieldName = FalsePositiveProjectData.PROPERTY_WEBSCAN + "."
            + WebscanFalsePositiveProjectData.PROPERTY_URLPATHPATTERNS + "[]";

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
    }

    @Test
    void too_long_optional_lists_returns_invalid_result() {
        /* prepare */
        List<String> methods = createTooLongListOfStrings();
        List<String> ports = createTooLongListOfStrings();
        List<String> protocols = createTooLongListOfStrings();

        WebscanFalsePositiveProjectData webScan = createWebscanFalsePositiveProjectDataWithValidMandatoryParts();
        webScan.setMethods(methods);
        webScan.setPorts(ports);
        webScan.setProtocols(protocols);

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        /* test */
        assertFalse(result.isValid());
        List<String> errors = result.getErrors();
        assertEquals(3, errors.size());

        assertTrue(errors.get(0).contains(methodsFieldName));
        assertTrue(errors.get(1).contains(portsFieldName));
        assertTrue(errors.get(2).contains(protocolsFieldName));
    }

    @Test
    void too_long_list_entry_in_optional_lists_returns_invalid_result() {
        /* prepare */
        String tooLongEntry = "a".repeat(301);

        List<String> methods = new ArrayList<>();
        methods.add(tooLongEntry);
        List<String> ports = new ArrayList<>();
        ports.add(tooLongEntry);
        List<String> protocols = new ArrayList<>();
        protocols.add(tooLongEntry);

        WebscanFalsePositiveProjectData webScan = createWebscanFalsePositiveProjectDataWithValidMandatoryParts();
        webScan.setMethods(methods);
        webScan.setPorts(ports);
        webScan.setProtocols(protocols);

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        /* test */
        assertFalse(result.isValid());
        List<String> errors = result.getErrors();
        assertEquals(3, errors.size());

        assertTrue(errors.get(0).contains(methodsFieldName));
        assertTrue(errors.get(1).contains(portsFieldName));
        assertTrue(errors.get(2).contains(protocolsFieldName));
    }

    @Test
    void mandatory_lists_null_returns_invalid_result() {
        /* prepare */
        WebscanFalsePositiveProjectData webScan = new WebscanFalsePositiveProjectData();
        webScan.setHostPatterns(null);
        webScan.setUrlPathPatterns(null);

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        /* test */
        assertFalse(result.isValid());
        List<String> errors = result.getErrors();
        assertEquals(2, errors.size());

        assertTrue(errors.get(0).contains(hostPatternsFieldName));
        assertTrue(errors.get(1).contains(urlPatternsFieldName));
    }

    @Test
    void mandatory_lists_empty_returns_invalid_result() {
        /* prepare */
        WebscanFalsePositiveProjectData webScan = new WebscanFalsePositiveProjectData();
        webScan.setHostPatterns(new ArrayList<>());
        webScan.setUrlPathPatterns(new ArrayList<>());

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        /* test */
        assertFalse(result.isValid());
        List<String> errors = result.getErrors();
        assertEquals(2, errors.size());

        assertTrue(errors.get(0).contains(hostPatternsFieldName));
        assertTrue(errors.get(1).contains(urlPatternsFieldName));
    }

    @Test
    void too_long_entry_in_mandatory_lists_returns_invalid_result() {
        /* prepare */
        String tooLongServerEntry = "a".repeat(290) + ".*.host.com";
        String tooLongUrlPatternEntry = "a".repeat(290) + "/rest/api/*";

        List<String> hostPatterns = new ArrayList<>();
        hostPatterns.add(tooLongServerEntry);
        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add(tooLongUrlPatternEntry);

        WebscanFalsePositiveProjectData webScan = new WebscanFalsePositiveProjectData();
        webScan.setHostPatterns(hostPatterns);
        webScan.setUrlPathPatterns(urlPatterns);

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        /* test */
        assertFalse(result.isValid());
        List<String> errors = result.getErrors();
        assertEquals(2, errors.size());

        assertTrue(errors.get(0).contains(hostPatternsFieldName));
        assertTrue(errors.get(1).contains(urlPatternsFieldName));
    }

    @Test
    void too_long_mandatory_lists_returns_invalid_result() {
        /* prepare */
        WebscanFalsePositiveProjectData webScan = createWebscanFalsePositiveProjectDataWithTooManyMandatoryListEntries();

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        /* test */
        assertFalse(result.isValid());
        List<String> errors = result.getErrors();
        assertEquals(2, errors.size());

        assertTrue(errors.get(0).contains(hostPatternsFieldName));
        assertTrue(errors.get(1).contains(urlPatternsFieldName));
    }

    @Test
    void invalid_hostPatterns_separators_return_invalid_result() {
        /* prepare */
        WebscanFalsePositiveProjectData webScan = createWebscanFalsePositiveProjectDataWithValidMandatoryParts();
        List<String> hostPatterns = new ArrayList<>();
        hostPatterns.add("sub,host,com");
        hostPatterns.add("sub;host;com");
        hostPatterns.add("subhostcom");
        hostPatterns.add("sub host com");
        hostPatterns.add("sub/host/com");
        // add one valid entry, so 5 errors with 6 list entries
        hostPatterns.add("sub.host.com");
        webScan.setHostPatterns(hostPatterns);

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        /* test */
        assertFalse(result.isValid());
        List<String> errors = result.getErrors();
        assertEquals(5, errors.size());
    }

    @Test
    void invalid_urlPattern_separators_return_invalid_result() {
        /* prepare */
        WebscanFalsePositiveProjectData webScan = createWebscanFalsePositiveProjectDataWithValidMandatoryParts();
        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("rest,api,*");
        urlPatterns.add("rest;api;*");
        urlPatterns.add("restapi*");
        urlPatterns.add("rest api *");
        urlPatterns.add("rest.api.*");
        // add one valid entry, so 5 errors with 6 list entries
        urlPatterns.add("rest/api/*");
        webScan.setUrlPathPatterns(urlPatterns);

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        /* test */
        assertFalse(result.isValid());
        List<String> errors = result.getErrors();
        assertEquals(5, errors.size());
    }

    @Test
    void urlPathPattern_and_hostPattern_cotnaining_backslashes_return_invalid_result() {
        /* prepare */
        WebscanFalsePositiveProjectData webScan = createWebscanFalsePositiveProjectDataWithValidMandatoryParts();
        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("/rest/ap\\Ei/use\\Qrs/*");
        webScan.setUrlPathPatterns(urlPatterns);

        List<String> hostPatterns = new ArrayList<>();
        hostPatterns.add("*.su\\Eb.ho\\Qst.com");
        webScan.setHostPatterns(hostPatterns);

        /* execute */
        ValidationResult result = validationToTest.validate(webScan);

        /* test */
        assertFalse(result.isValid());
        List<String> errors = result.getErrors();
        assertEquals(2, errors.size());

        String error1 = errors.get(0);
        assertTrue(error1.contains("no backslashes are allowed"));

        String error2 = errors.get(1);
        assertTrue(error2.contains("no backslashes are allowed"));
    }

    private WebscanFalsePositiveProjectData createWebscanFalsePositiveProjectDataWithValidMandatoryParts() {
        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("api/admin/test/*");
        List<String> hostPatterns = new ArrayList<>();
        hostPatterns.add("*.host.com");
        WebscanFalsePositiveProjectData webScan = new WebscanFalsePositiveProjectData();
        webScan.setUrlPathPatterns(urlPatterns);
        webScan.setHostPatterns(hostPatterns);

        return webScan;
    }

    private WebscanFalsePositiveProjectData createWebscanFalsePositiveProjectDataWithTooManyMandatoryListEntries() {
        List<String> urlPatterns = new ArrayList<>();
        List<String> hostPatterns = new ArrayList<>();

        for (int i = 0; i < 51; i++) {
            urlPatterns.add("api/admin/test/*");
            hostPatterns.add("*.host.com");
        }
        WebscanFalsePositiveProjectData webScan = new WebscanFalsePositiveProjectData();
        webScan.setUrlPathPatterns(urlPatterns);
        webScan.setHostPatterns(hostPatterns);

        return webScan;
    }

    private List<String> createTooLongListOfStrings() {
        List<String> list = new LinkedList<>();
        for (int i = 0; i < 51; i++) {
            list.add("a");
        }
        return list;
    }
}
