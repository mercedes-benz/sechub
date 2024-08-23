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

import com.mercedesbenz.sechub.domain.scan.project.WebscanFalsePositiveProjectData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

class SerecoWebScanFalsePositiveProjectDataSupportTest {

    private static List<String> urlPathPatterns = List.of("*/rest/api/", "/rest/api/user/profile");
    private static String matchingUrlPathPattern = "/rest/api/user/profile";

    private static List<String> hostPatterns = List.of("*.example.com", "test.exampleapp.com");
    private static String matchingHost = "prod.example.com";

    private static Pattern mockedPattern = mock(Pattern.class);
    private static Matcher mockedMatcher = mock(Matcher.class);

    private SerecoProjectDataWebScanFalsePositiveSupport supportToTest;
    private Map<String, Pattern> patternMap;

    @BeforeEach
    void beforeEach() {
        supportToTest = new SerecoProjectDataWebScanFalsePositiveSupport();
        patternMap = createPatternMapWithMocks();

        when(mockedPattern.matcher(matchingHost)).thenReturn(mockedMatcher);
        when(mockedPattern.matcher(matchingUrlPathPattern)).thenReturn(mockedMatcher);
    }

    /*-------------------------------------CWE-IDs----------------------------------------------*/
    @ParameterizedTest
    @EmptySource
    @NullSource
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
    @EmptySource
    @NullSource
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
    @NullSource
    @ValueSource(ints = { 1, -1, 0, 4711 })
    void cwe_id_of_vulnerability_is_one_more_returns_false(Integer cweId) {
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

    /*--------------------------------------------PORTS-----------------------------------------------*/

    @Test
    void ports_not_set_in_webscan_data_being_null_returns_true() {
        /* execute */
        boolean result = supportToTest.isMatchingPortOrIgnoreIfNotSet("anything", null);

        /* test */
        assertTrue(result);
    }

    @Test
    void ports_empty_in_webscan_data_being_null_returns_true() {
        /* execute */
        boolean result = supportToTest.isMatchingPortOrIgnoreIfNotSet("anything", Collections.emptyList());

        /* test */
        assertTrue(result);
    }

    @Test
    void port_containing_required_string_returns_true() {
        /* prepare */
        List<String> ports = List.of("8080", "443", "80");

        /* execute */
        boolean result = supportToTest.isMatchingPortOrIgnoreIfNotSet("80", ports);

        /* test */
        assertTrue(result);
    }

    @Test
    void ports_not_containing_required_string_returns_false() {
        /* prepare */
        List<String> ports = List.of("8080", "443", "80");

        /* execute */
        boolean result = supportToTest.isMatchingPortOrIgnoreIfNotSet("no-in-list", ports);

        /* test */
        assertFalse(result);
    }

    /*----------------------------------------PROTOCOLS-----------------------------------------------*/

    @Test
    void protocols_not_set_in_webscan_data_being_null_returns_true() {
        /* execute */
        boolean result = supportToTest.isMatchingProtocolOrIgnoreIfNotSet("anything", null);

        /* test */
        assertTrue(result);
    }

    @Test
    void protocols_empty_in_webscan_data_being_null_returns_true() {
        /* execute */
        boolean result = supportToTest.isMatchingProtocolOrIgnoreIfNotSet("anything", Collections.emptyList());

        /* test */
        assertTrue(result);
    }

    @Test
    void protocols_containing_required_string_returns_true() {
        /* prepare */
        List<String> protocols = List.of("wss", "https");

        /* execute */
        boolean result = supportToTest.isMatchingProtocolOrIgnoreIfNotSet("https", protocols);

        /* test */
        assertTrue(result);
    }

    @Test
    void protocols_not_containing_required_string_returns_false() {
        /* prepare */
        List<String> protocols = List.of("wss", "https");

        /* execute */
        boolean result = supportToTest.isMatchingProtocolOrIgnoreIfNotSet("no-in-list", protocols);

        /* test */
        assertFalse(result);
    }

    /*----------------------------------------HOSTPATTERNS-----------------------------------------------*/

    @Test
    void for_hostPatterns_pattern_in_map_is_null_throws_exception() {
        /* execute + test */
        // At this point this should never happen because the map is meant to be created
        // by the associated projectData
        assertThrows(IllegalStateException.class, () -> supportToTest.isMatchingHostPattern(matchingHost, hostPatterns, new HashMap<>()));
    }

    @Test
    void for_hostPatterns_not_matching_returns_false() {
        /* prepare */
        when(mockedMatcher.matches()).thenReturn(false);

        /* execute */
       boolean result = supportToTest.isMatchingHostPattern(matchingHost, hostPatterns, patternMap);

       /* test */
       assertFalse(result);
    }

    @Test
    void for_hostPatterns_is_matching_returns_true() {
        /* prepare */
        when(mockedMatcher.matches()).thenReturn(true);

        /* execute */
       boolean result = supportToTest.isMatchingHostPattern(matchingHost, hostPatterns, patternMap);

       /* test */
       assertTrue(result);
    }

    /*---------------------------------------URLPATHPATTERNS--------------------------------------------*/

    @Test
    void for_urlPathPatterns_pattern_in_map_is_null_throws_exception() {
        /* execute + test */
        // At this point this should never happen because the map is meant to be created
        // by the associated projectData
        assertThrows(IllegalStateException.class, () -> supportToTest.isMatchingHostPattern(matchingUrlPathPattern, urlPathPatterns, new HashMap<>()));
    }

    @Test
    void for_urlPathPatterns_not_matching_returns_false() {
        /* prepare */
        when(mockedMatcher.matches()).thenReturn(false);

        /* execute */
       boolean result = supportToTest.isMatchingHostPattern(matchingUrlPathPattern, urlPathPatterns, patternMap);

       /* test */
       assertFalse(result);
    }

    @Test
    void for_urlPathPatterns_is_matching_returns_true() {
        /* prepare */
        when(mockedMatcher.matches()).thenReturn(true);

        /* execute */
       boolean result = supportToTest.isMatchingHostPattern(matchingUrlPathPattern, urlPathPatterns, patternMap);

       /* test */
       assertTrue(result);
    }

    /*-------------------------------------HELPERS----------------------------------------------*/

    private Integer createIntegerFromString(String cweId) {
        if (cweId == null) {
            return null;
        }
        if (cweId.isEmpty()) {
            return null;
        }
        return Integer.parseInt(cweId);
    }

    private String createIntAsStringButPlusOne(Integer cweId) {
        if (cweId == null) {
            return "1";
        }
        int next = cweId.intValue() + 1;
        return String.valueOf(next);
    }

    private Integer createAsIntButPlusOne(String cweId) {
        Integer intvalue = createIntegerFromString(cweId);
        if (intvalue == null) {
            return 1;
        }
        return intvalue + 1;
    }

    private Map<String, Pattern> createPatternMapWithMocks() {
        Map<String, Pattern> patternMap = new HashMap<>();
        for (String urlPathPattern : urlPathPatterns) {
            patternMap.put(urlPathPattern, mockedPattern);
        }

        for (String hostPattern : hostPatterns) {
            patternMap.put(hostPattern, mockedPattern);
        }
        return patternMap;
    }

}
