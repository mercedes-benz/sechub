// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveEntry;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveJobData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectData;
import com.mercedesbenz.sechub.domain.scan.project.WebscanFalsePositiveProjectData;

class SerecoProjectDataPatternMapFactoryTest {

    private SerecoProjectDataPatternMapFactory factoryToTest;

    @BeforeEach
    void beforeEach() {
        factoryToTest = new SerecoProjectDataPatternMapFactory();
    }

    /*-------------------------------------TEST PATTERN CREATION-------------------------------------------*/

    @Test
    void factory_throws_illegal_argument_exception_if_list_of_false_positives_is_null() {
        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> factoryToTest.create(null));
    }

    @Test
    void factory_returns_empty_map_if_list_of_false_positives_is_null() {
        /* execute */
        Map<String, Pattern> map = factoryToTest.create(new ArrayList<>());

        /* test */
        assertTrue(map.isEmpty());
    }

    @Test
    void factory_returns_empty_map_if_list_of_false_positives_only_contains_jobData_entries() {
        /* prepare */
        List<FalsePositiveEntry> falsePositives = new ArrayList<>();

        FalsePositiveEntry entry1 = new FalsePositiveEntry();
        entry1.setJobData(new FalsePositiveJobData());
        falsePositives.add(entry1);

        FalsePositiveEntry entry2 = new FalsePositiveEntry();
        entry2.setJobData(new FalsePositiveJobData());
        falsePositives.add(entry2);

        /* execute */
        Map<String, Pattern> map = factoryToTest.create(falsePositives);

        /* test */
        assertTrue(map.isEmpty());
    }

    @Test
    void factory_throws_illegal_argument_exception_if_list_of_projectData_false_positives_contains_webscan_with_null_hostPatterns() {
        /* prepare */
        List<FalsePositiveEntry> falsePositives = new ArrayList<>();

        FalsePositiveEntry entry = new FalsePositiveEntry();
        FalsePositiveProjectData projectData = new FalsePositiveProjectData();
        WebscanFalsePositiveProjectData webScan = new WebscanFalsePositiveProjectData();
        webScan.setHostPatterns(new ArrayList<>());
        projectData.setWebScan(webScan);
        entry.setProjectData(projectData);
        falsePositives.add(entry);

        /* execute + test */
        // This should never happen, because invalid entries should never get inside the
        // database
        assertThrows(IllegalArgumentException.class, () -> factoryToTest.create(falsePositives));
    }

    @Test
    void factory_throws_illegal_argument_exception_if_list_of_projectData_false_positives_contains_webscan_with_null_urlPathPatterns() {
        List<FalsePositiveEntry> falsePositives = createFalsePositiveEntriesForProjectDataWebscan(null, null);

        /* execute + test */
        // This should never happen, because invalid entries should never get inside the
        // database
        assertThrows(IllegalArgumentException.class, () -> factoryToTest.create(falsePositives));
    }

    @Test
    void for_projectData_webscan_factory_returns_expected_map_with_patterns_for_each_mandatory_that_parameter() {
        /* prepare */
        List<String> urlPathPatterns = new ArrayList<>();
        urlPathPatterns.add("/rest/api/*/users");
        urlPathPatterns.add("*/rest/api*");
        urlPathPatterns.add("/rest/api/profile/users");

        List<String> hostPatterns = new ArrayList<>();
        hostPatterns.add("127.0.0.1");
        hostPatterns.add("*localhost*");
        hostPatterns.add("prod.example.com");
        hostPatterns.add("*.example.com");
        hostPatterns.add("2001:db8:ffff:ffff:ffff:ffff:ffff:ffff");
        hostPatterns.add("*.*.example.com*");

        List<FalsePositiveEntry> falsePositives = createFalsePositiveEntriesForProjectDataWebscan(urlPathPatterns, hostPatterns);

        /* execute */
        Map<String, Pattern> patternMap = factoryToTest.create(falsePositives);

        /* test */
        assertEquals("^\\Q/rest/api/\\E.*\\Q/users\\E$", patternMap.get("/rest/api/*/users").toString());
        assertEquals("^.*\\Q/rest/api\\E.*$", patternMap.get("*/rest/api*").toString());
        assertEquals("^\\Q/rest/api/profile/users\\E$", patternMap.get("/rest/api/profile/users").toString());

        assertEquals("^\\Q127.0.0.1\\E$", patternMap.get("127.0.0.1").toString());
        assertEquals("^.*\\Qlocalhost\\E.*$", patternMap.get("*localhost*").toString());
        assertEquals("^\\Qprod.example.com\\E$", patternMap.get("prod.example.com").toString());
        assertEquals("^.*\\Q.example.com\\E$", patternMap.get("*.example.com").toString());
        assertEquals("^\\Q2001:db8:ffff:ffff:ffff:ffff:ffff:ffff\\E$", patternMap.get("2001:db8:ffff:ffff:ffff:ffff:ffff:ffff").toString());
        assertEquals("^.*\\Q.\\E.*\\Q.example.com\\E.*$", patternMap.get("*.*.example.com*").toString());
    }

    /*---------------------------TEST THE CREATED HOST PATTERNS FOR EXPECTED BEHAVIOUR---------------------------------*/

    @Test
    void host_patterns_ipv4_without_wildcards_match_expected_strings() {
        /* prepare */
        List<String> urlPathPatterns = new ArrayList<>();

        String ipv4PatternAsString = "127.0.0.1";
        List<String> hostPatterns = List.of(ipv4PatternAsString);

        List<String> mustMatchHostnames = List.of(ipv4PatternAsString);
        List<String> mustNotMatchHostnames = List.of("127.0.0.2", "127.0.0.11", "1127.0.0.1", "localhost");

        List<FalsePositiveEntry> falsePositives = createFalsePositiveEntriesForProjectDataWebscan(urlPathPatterns, hostPatterns);

        /* execute */
        Map<String, Pattern> patternMap = factoryToTest.create(falsePositives);

        /* test */
        Pattern pattern = patternMap.get(ipv4PatternAsString);

        assertMatches(mustMatchHostnames, pattern);
        assertDoesNotMatch(mustNotMatchHostnames, pattern);
    }

    @Test
    void host_patterns_hostname_without_wildcards_match_expected_strings() {
        /* prepare */
        List<String> urlPathPatterns = new ArrayList<>();

        String hostnamePattern = "example.com";
        List<String> hostPatterns = List.of(hostnamePattern);

        List<String> mustMatchHostnames = List.of(hostnamePattern);
        List<String> mustNotMatchHostnames = List.of("api.example.cor", "api.example.comm", "aapi.example.com", "api.example.com");

        List<FalsePositiveEntry> falsePositives = createFalsePositiveEntriesForProjectDataWebscan(urlPathPatterns, hostPatterns);

        /* execute */
        Map<String, Pattern> patternMap = factoryToTest.create(falsePositives);

        /* test */
        Pattern pattern = patternMap.get(hostnamePattern);

        assertMatches(mustMatchHostnames, pattern);
        assertDoesNotMatch(mustNotMatchHostnames, pattern);
    }

    @Test
    void host_patterns_ipv6_without_wildcards_match_expected_strings() {
        /* prepare */
        List<String> urlPathPatterns = new ArrayList<>();

        String ipv6PatternAsString = "2001:db8:ffff:ffff:ffff:ffff:ffff:ffff";
        List<String> hostPatterns = List.of(ipv6PatternAsString);

        List<String> mustMatchHostnames = List.of(ipv6PatternAsString);
        List<String> mustNotMatchHostnames = List.of("2001:db8:ffff:ffff:ffff:ffff:ffff:fffa", "2001:db8:ffff:ffff:ffff:ffff:ffff:fffff",
                "22001:db8:ffff:ffff:ffff:ffff:ffff:ffff", "::::");

        List<FalsePositiveEntry> falsePositives = createFalsePositiveEntriesForProjectDataWebscan(urlPathPatterns, hostPatterns);

        /* execute */
        Map<String, Pattern> patternMap = factoryToTest.create(falsePositives);

        /* test */
        Pattern pattern = patternMap.get(ipv6PatternAsString);

        assertMatches(mustMatchHostnames, pattern);
        assertDoesNotMatch(mustNotMatchHostnames, pattern);
    }

    @Test
    void host_patterns_with_wildcards_match_expected_strings() {
        /* prepare */
        List<String> urlPathPatterns = new ArrayList<>();

        String hostPattern = "*.*.0.1";
        List<String> hostPatterns = List.of(hostPattern);

        List<String> mustMatchHostnames = List.of("127.0.0.1", "127..0..0.1", "192.89.0.1", "1127.0.0.1", "prod.host.0.1", "prod1.host3.0.1",
                "longer-host.name-for-testing.0.1");
        List<String> mustNotMatchHostnames = List.of("127.0.0.2", "127.0.0.11", "localhost", "127.0.1.1", "127.0.0..1");

        List<FalsePositiveEntry> falsePositives = createFalsePositiveEntriesForProjectDataWebscan(urlPathPatterns, hostPatterns);

        /* execute */
        Map<String, Pattern> patternMap = factoryToTest.create(falsePositives);

        /* test */
        Pattern pattern = patternMap.get(hostPattern);

        assertMatches(mustMatchHostnames, pattern);
        assertDoesNotMatch(mustNotMatchHostnames, pattern);
    }

    @Test
    void host_patterns_pv6_separator_with_wildcards_match_expected_strings() {
        /* prepare */
        List<String> urlPathPatterns = new ArrayList<>();

        String hostPattern = "2001:*:ffff:ffff:ffff:ffff:*:*";
        List<String> hostPatterns = List.of(hostPattern);

        List<String> mustMatchHostnames = List.of("2001::db8:ffff:ffff:ffff:ffff:ffff:fffa", "2001:db8:ffff:ffff:ffff:ffff:ffff:fffff",
                "2001:db8:ffff:ffff:ffff:ffff:ffff:ffff");
        List<String> mustNotMatchHostnames = List.of("::::", "2001:db8:ffff:ffff::ffff:ffff:ffff:fffff", "2001:db8:ffff:ffff::ffff:ffff::ffff:fffff");

        List<FalsePositiveEntry> falsePositives = createFalsePositiveEntriesForProjectDataWebscan(urlPathPatterns, hostPatterns);

        /* execute */
        Map<String, Pattern> patternMap = factoryToTest.create(falsePositives);

        /* test */
        Pattern pattern = patternMap.get(hostPattern);

        assertMatches(mustMatchHostnames, pattern);
        assertDoesNotMatch(mustNotMatchHostnames, pattern);
    }

    /*---------------------------TEST THE CREATED URL PATH PATTERNS FOR EXPECTED BEHAVIOUR---------------------------------*/

    @Test
    void url_path_patterns_without_wildcards_match_expected_strings() {
        /* prepare */
        List<String> hostNames = new ArrayList<>();

        String urlPathPattern = "/rest/api/user/profile";
        List<String> urlPathPatterns = List.of(urlPathPattern);

        List<String> mustMatchUrlPaths = List.of(urlPathPattern);
        List<String> mustNotMatchUrlPaths = List.of("a/rest/api/user/profile", "/rest/api/user/profile/", "/rest/api/user/profile/b", "/rest/api/user/profilee",
                "//rest/api/user/profile");

        List<FalsePositiveEntry> falsePositives = createFalsePositiveEntriesForProjectDataWebscan(urlPathPatterns, hostNames);

        /* execute */
        Map<String, Pattern> patternMap = factoryToTest.create(falsePositives);

        /* test */
        Pattern pattern = patternMap.get(urlPathPattern);

        assertMatches(mustMatchUrlPaths, pattern);
        assertDoesNotMatch(mustNotMatchUrlPaths, pattern);
    }

    @Test
    void url_path_patterns_with_wildcards_match_expected_strings() {
        /* prepare */
        List<String> hostNames = new ArrayList<>();

        String urlPathPattern = "*/rest/api/*/profile";
        List<String> urlPathPatterns = List.of(urlPathPattern);

        List<String> mustMatchUrlPaths = List.of("a/rest/api/user/profile", "//rest/api/user/profile", "/rest/api/user12/profile", "dev/rest/api/user/profile");
        List<String> mustNotMatchUrlPaths = List.of("/rest/api/user/profile/", "/rest/api/user/profile/b", "/rest/api/user/profilee");

        List<FalsePositiveEntry> falsePositives = createFalsePositiveEntriesForProjectDataWebscan(urlPathPatterns, hostNames);

        /* execute */
        Map<String, Pattern> patternMap = factoryToTest.create(falsePositives);

        /* test */
        Pattern pattern = patternMap.get(urlPathPattern);

        assertMatches(mustMatchUrlPaths, pattern);
        assertDoesNotMatch(mustNotMatchUrlPaths, pattern);
    }

    /*----------------------------------------------HELPERS------------------------------------------------------*/

    private List<FalsePositiveEntry> createFalsePositiveEntriesForProjectDataWebscan(List<String> urlPathPatterns, List<String> hostPatterns) {
        List<FalsePositiveEntry> falsePositives = new ArrayList<>();

        FalsePositiveProjectData projectData = new FalsePositiveProjectData();
        WebscanFalsePositiveProjectData webScan = new WebscanFalsePositiveProjectData();
        webScan.setUrlPathPatterns(urlPathPatterns);
        webScan.setHostPatterns(hostPatterns);
        projectData.setWebScan(webScan);

        FalsePositiveEntry entry = new FalsePositiveEntry();
        entry.setProjectData(projectData);
        falsePositives.add(entry);
        return falsePositives;
    }

    private void assertMatches(List<String> mustMatchList, Pattern pattern) {
        for (String mustMatch : mustMatchList) {
            if (!pattern.matcher(mustMatch).matches()) {
                fail("Expected pattern: " + pattern.toString() + " to match: " + mustMatch);
            }
        }
    }

    private void assertDoesNotMatch(List<String> mustNotMatchList, Pattern pattern) {
        for (String mustNotMatch : mustNotMatchList) {
            if (pattern.matcher(mustNotMatch).matches()) {
                fail("Expected pattern: " + pattern.toString() + " to NOT match: " + mustNotMatch);
            }
        }
    }
}
