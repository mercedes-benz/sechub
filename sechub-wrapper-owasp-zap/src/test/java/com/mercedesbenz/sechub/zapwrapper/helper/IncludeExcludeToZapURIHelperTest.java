// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;

class IncludeExcludeToZapURIHelperTest {

    private IncludeExcludeToZapURLHelper helperToTest;

    private List<SecHubMessage> userMessages;

    @BeforeEach
    void beforeEach() {
        helperToTest = new IncludeExcludeToZapURLHelper();
        userMessages = new LinkedList<>();
    }

    @Test
    void returns_empty_list_if_list_of_subSites_is_null() throws MalformedURLException {
        /* prepare */
        URL targetUrl = new URL("https://127.0.0.1:8080");
        List<String> sites = null;

        /* execute */
        List<URL> urls = helperToTest.createListOfUrls(ZapURLType.INCLUDE, targetUrl, sites, userMessages);

        /* test */
        assertTrue(urls.isEmpty());
        assertTrue(userMessages.isEmpty());
    }

    @Test
    void returns_empty_list_if_list_of_subSites_is_empty() throws MalformedURLException {
        /* prepare */
        URL targetUrl = new URL("https://127.0.0.1:8080");
        List<String> sites = new ArrayList<>();

        /* execute */
        List<URL> urls = helperToTest.createListOfUrls(ZapURLType.INCLUDE, targetUrl, sites, userMessages);

        /* test */
        assertTrue(urls.isEmpty());
        assertTrue(userMessages.isEmpty());
    }

    @Test
    void returns_list_of_url_conform_for_zap_includes_or_excludes() throws MalformedURLException {
        /* prepare */
        URL targetUrl = new URL("https://127.0.0.1:8080");
        List<String> sites = createExampleListOfSites();

        /* execute */
        List<URL> urls = helperToTest.createListOfUrls(ZapURLType.EXCLUDE, targetUrl, sites, userMessages);

        /* test */
        assertTrue(urls.contains(new URL("https://127.0.0.1:8080/sub")));
        assertTrue(urls.contains(new URL("https://127.0.0.1:8080/sub1/directory")));
        assertTrue(urls.contains(new URL("https://127.0.0.1:8080/sub2/directory")));
        assertTrue(urls.contains(new URL("https://127.0.0.1:8080/sub3/directory")));
        assertTrue(urls.contains(new URL("https://127.0.0.1:8080/sub4/directory")));

        assertTrue(userMessages.isEmpty());
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
