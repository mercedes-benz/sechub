// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.domain.scan.project.WebscanFalsePositiveProjectData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

class SerecoProjectDataWebScanFalsePositiveSupportTest {

    private static final String MATCHING_URL = "https://prod.example.com/rest/profile/search";

    private static final Pattern MOCKED_PATTERN = mock();
    private static final Matcher MOCKED_MATCHER = mock();

    private SerecoProjectDataWebScanFalsePositiveSupport supportToTest;
    private Map<String, Pattern> patternMap;

    @BeforeEach
    void beforeEach() {
        Mockito.reset(MOCKED_PATTERN, MOCKED_MATCHER);

        supportToTest = new SerecoProjectDataWebScanFalsePositiveSupport();

        patternMap = new HashMap<>();
        patternMap.put("id", MOCKED_PATTERN);

        when(MOCKED_PATTERN.matcher(MATCHING_URL)).thenReturn(MOCKED_MATCHER);
    }

    /*-------------------------------------CWE-IDs----------------------------------------------*/
    @ParameterizedTest
    @ValueSource(strings = { "1", "-1", "0", "4711" })
    void both_having_the_same_cwe_id_returns_true(String cweId) {
        /* prepare */
        WebscanFalsePositiveProjectData webScanData = new WebscanFalsePositiveProjectData();
        webScanData.setCweId(createIntegerFromString(cweId));
        SerecoVulnerability vulnerability = new SerecoVulnerability();
        vulnerability.getClassification().setCwe(cweId);

        /* execute */
        boolean result = supportToTest.areBothHavingSameCweIdOrBothNoCweId(webScanData, vulnerability);

        /* test */
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "1", "-1", "0", "4711" })
    void cwe_id_of_webscan_data_is_one_more_returns_false(String cweId) {
        /* prepare */
        WebscanFalsePositiveProjectData webScanData = new WebscanFalsePositiveProjectData();
        webScanData.setCweId(createAsIntButPlusOne(cweId));

        SerecoVulnerability vulnerability = new SerecoVulnerability();
        vulnerability.getClassification().setCwe(cweId);

        /* execute */
        boolean areBothHavingSameCweIdOrBothNoCweId = supportToTest.areBothHavingSameCweIdOrBothNoCweId(webScanData, vulnerability);

        /* test */
        assertFalse(areBothHavingSameCweIdOrBothNoCweId);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, -1, 0, 4711 })
    void cwe_id_of_vulnerability_is_one_more_returns_false(int cweId) {
        /* prepare */
        WebscanFalsePositiveProjectData webScanData = new WebscanFalsePositiveProjectData();
        webScanData.setCweId(cweId);

        SerecoVulnerability vulnerability = new SerecoVulnerability();
        vulnerability.getClassification().setCwe(createIntAsStringButPlusOne(cweId));

        /* execute */
        boolean areBothHavingSameCweIdOrBothNoCweId = supportToTest.areBothHavingSameCweIdOrBothNoCweId(webScanData, vulnerability);

        /* test */
        assertFalse(areBothHavingSameCweIdOrBothNoCweId);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void cwe_id_of_projectData_is_zero_and_cwe_of_sereco_vulnerability_is_unset_returns_true(String serecoCweId) {
        /* prepare */
        WebscanFalsePositiveProjectData webScanData = new WebscanFalsePositiveProjectData();
        webScanData.setCweId(0);

        SerecoVulnerability vulnerability = new SerecoVulnerability();
        vulnerability.getClassification().setCwe(serecoCweId);

        /* execute */
        boolean areBothHavingSameCweIdOrBothNoCweId = supportToTest.areBothHavingSameCweIdOrBothNoCweId(webScanData, vulnerability);

        /* test */
        assertTrue(areBothHavingSameCweIdOrBothNoCweId);
    }

    /*-----------------------------------------------METHODS-----------------------------------------------*/

    @Test
    void methods_not_set_in_webscan_data_being_null_returns_true() {
        /* execute */
        boolean result = supportToTest.isMatchingMethodOrIgnoreIfNotSet("anything", null);

        /* test */
        assertTrue(result);
    }

    @Test
    void methods_empty_in_webscan_data_being_null_returns_true() {
        /* execute */
        boolean result = supportToTest.isMatchingMethodOrIgnoreIfNotSet("anything", Collections.emptyList());

        /* test */
        assertTrue(result);
    }

    @Test
    void methods_containing_required_string_returns_true() {
        /* prepare */
        List<String> methods = List.of("POST", "GET", "DELETE");

        /* execute */
        boolean result = supportToTest.isMatchingMethodOrIgnoreIfNotSet("GET", methods);

        /* test */
        assertTrue(result);
    }

    @Test
    void methods_not_containing_required_string_returns_false() {
        /* prepare */
        List<String> methods = List.of("POST", "GET", "DELETE");

        /* execute */
        boolean result = supportToTest.isMatchingMethodOrIgnoreIfNotSet("no-in-list", methods);

        /* test */
        assertFalse(result);
    }

    /*----------------------------------------URLPATTERNS-----------------------------------------------*/

    @Test
    void for_urlPattern_not_matching_returns_false() {
        /* prepare */
        when(MOCKED_MATCHER.matches()).thenReturn(false);

        /* execute */
       boolean result = supportToTest.isMatchingUrlPattern(MATCHING_URL, patternMap);

       /* test */
       assertFalse(result);
    }

    @Test
    void for_urlPattern_is_matching_returns_true() {
        /* prepare */
        when(MOCKED_MATCHER.matches()).thenReturn(true);

        /* execute */
       boolean result = supportToTest.isMatchingUrlPattern(MATCHING_URL, patternMap);

       /* test */
       assertTrue(result);
    }

    /*-------------------------------------HELPERS----------------------------------------------*/

    private int createIntegerFromString(String cweId) {
        if (cweId == null) {
            return 0;
        }
        if (cweId.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(cweId);
    }

    private String createIntAsStringButPlusOne(int cweId) {
        return String.valueOf(cweId + 1);
    }

    private int createAsIntButPlusOne(String cweId) {
        int intvalue = createIntegerFromString(cweId);
        return intvalue + 1;
    }

}
