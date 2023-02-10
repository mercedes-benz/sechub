// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IncludeExcludeToOwaspZapURIHelperTest {

    private IncludeExcludeToOwaspZapURLHelper helperToTest;

    @BeforeEach
    void beforeEach() {
        helperToTest = new IncludeExcludeToOwaspZapURLHelper();
    }

    @Test
    void returns_empty_list_if_list_of_subSites_is_null() {
        /* prepare */
        String targetUrl = "https://127.0.0.1:8080";
        List<String> sites = null;

        /* execute */
        List<String> urls = helperToTest.createListOfOwaspZapCompatibleUrls(targetUrl, sites);

        /* test */
        assertTrue(urls.isEmpty());
    }

    @Test
    void returns_empty_list_if_list_of_subSites_is_empty() {
        /* prepare */
        String targetUrl = "https://127.0.0.1:8080";
        List<String> sites = new ArrayList<>();

        /* execute */
        List<String> urls = helperToTest.createListOfOwaspZapCompatibleUrls(targetUrl, sites);

        /* test */
        assertTrue(urls.isEmpty());
    }

    @Test
    void returns_list_of_url_conform_for_owasp_zap_includes_or_excludes() {
        /* prepare */
        String targetUrl = "https://127.0.0.1:8080";
        List<String> sites = createExampleListOfSites();

        /* execute */
        List<String> urls = helperToTest.createListOfOwaspZapCompatibleUrls(targetUrl, sites);

        /* test */
        assertTrue(urls.contains("https://127.0.0.1:8080/sub"));
        assertTrue(urls.contains("https://127.0.0.1:8080/sub1/directory"));
        assertTrue(urls.contains("https://127.0.0.1:8080/sub2/directory"));
        assertTrue(urls.contains("https://127.0.0.1:8080/sub3/directory"));
        assertTrue(urls.contains("https://127.0.0.1:8080/sub4/directory"));
    }

    private List<String> createExampleListOfSites() {
        List<String> subSites = new ArrayList<>();
        subSites.add("/sub/");
        subSites.add("/sub1/directory/");
        subSites.add("/sub2/directory");
        subSites.add("sub3/directory/");
        subSites.add("sub4/directory");
        return subSites;
    }

}
