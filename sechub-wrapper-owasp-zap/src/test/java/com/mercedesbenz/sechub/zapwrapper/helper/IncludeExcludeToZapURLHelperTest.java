// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IncludeExcludeToZapURLHelperTest {

    private IncludeExcludeToZapURLHelper helperToTest;

    @BeforeEach
    void beforeEach() {
        helperToTest = new IncludeExcludeToZapURLHelper();
    }

    @Test
    void list_of_subSites_is_null_returns_empty_list() throws MalformedURLException {
        /* prepare */
        URL targetUrl = new URL("https://127.0.0.1:8080");
        List<String> sites = null;

        /* execute */
        List<String> urls = helperToTest.createListOfUrls(targetUrl, sites);

        /* test */
        assertTrue(urls.isEmpty());
    }

    @Test
    void list_of_subSites_is_empty_returns_empty_list() throws MalformedURLException {
        /* prepare */
        URL targetUrl = new URL("https://127.0.0.1:8080");
        List<String> sites = new ArrayList<>();

        /* execute */
        List<String> urls = helperToTest.createListOfUrls(targetUrl, sites);

        /* test */
        assertTrue(urls.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://127.0.0.1:8080", "https://127.0.0.1:8080/" })
    void list_of_subsites_and_target_url_returns_list_of_combined_urls(String url) throws MalformedURLException {
        /* prepare */
        URL targetUrl = new URL(url);
        List<String> sites = createExampleListOfSites();

        /* execute */
        List<String> urls = helperToTest.createListOfUrls(targetUrl, sites);

        /* test */
        assertEquals(15, urls.size());
        assertTrue(urls.contains("https://127.0.0.1:8080/sub/"));
        assertTrue(urls.contains("https://127.0.0.1:8080/sub1/directory/"));
        assertTrue(urls.contains("https://127.0.0.1:8080/sub2/directory"));
        assertTrue(urls.contains("https://127.0.0.1:8080/.*sub3/directory/.*"));
        assertTrue(urls.contains("https://127.0.0.1:8080/.*sub4/directory.*"));
        assertTrue(urls.contains("https://127.0.0.1:8080/"));

        assertTrue(urls.contains("https://127.0.0.1:8080/"));
        assertTrue(urls.contains("https://127.0.0.1:8080/.*"));
        assertTrue(urls.contains("https://127.0.0.1:8080/.*"));
        assertTrue(urls.contains("https://127.0.0.1:8080/.*/.*"));
        assertTrue(urls.contains("https://127.0.0.1:8080/en/contacts/.*"));
        assertTrue(urls.contains("https://127.0.0.1:8080/.*/en/contacts/.*"));
        assertTrue(urls.contains("https://127.0.0.1:8080/.*/en/.*/.*/contacts/.*"));
        assertTrue(urls.contains("https://127.0.0.1:8080/.*/en.*.*contacts/.*"));
        assertTrue(urls.contains("https://127.0.0.1:8080/en/contacts/.*"));
    }

    private List<String> createExampleListOfSites() {
        List<String> subSites = new ArrayList<>();
        subSites.add("/sub/");
        subSites.add("/sub1/directory/");
        subSites.add("/sub2/directory");
        subSites.add("sub3/directory/");
        subSites.add("sub4/directory");
        subSites.add("/");
        subSites.add("<*>");
        subSites.add("/<*>");
        subSites.add("<*>/<*>");
        subSites.add("/en/contacts/<*>");
        subSites.add("<*>/en/contacts/<*>");
        subSites.add("<*>/en/<*>/contacts/<*>");
        subSites.add("<*>/en/<*>/<*>/contacts/<*>");
        subSites.add("<*>/en<*><*>contacts/<*>");
        subSites.add("en/contacts/<*>");
        return subSites;
    }

}
