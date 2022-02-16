// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.whitelist;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

public class ProjectWhiteListSupportTest {

    private ProjectWhiteListSupport validationToTest;

    @Before
    public void before() {
        validationToTest = new ProjectWhiteListSupport();
    }

    @Test
    public void when_no_uri_defined_https_google_de__is_not_valid() throws Exception {
        assertFalse(validationToTest.isWhitelisted("https://www.google.de", Collections.emptySet()));
    }

    @Test
    public void when_uri_https_amazon_com_defined_https_google_de__is_not_valid() throws Exception {
        assertFalse(validationToTest.isWhitelisted("https://www.google.de", Arrays.asList(new URI("https://www.amazon.com"))));
    }

    @Test
    public void when_uri_https_www_google_de_defined_https_www_google_de__is_valid() throws Exception {
        assertTrue(validationToTest.isWhitelisted("https://www.google.de", Arrays.asList(new URI("https://www.google.de"))));
    }

    @Test
    public void when_uri_https_amazon_com_and_https_www_google_de_defined_https_www_google_de__is_not_valid() throws Exception {
        assertTrue(validationToTest.isWhitelisted("https://www.google.de", Arrays.asList(new URI("https://www.amazon.com"), new URI("https://www.google.de"))));
    }

    @Test
    public void when_uri_https_www_google_de_defined_https_www_google_de_slash_nerdytest__is_valid() throws Exception {
        assertTrue(validationToTest.isWhitelisted("https://www.google.de/nerdytest", Arrays.asList(new URI("https://www.google.de"))));
    }

    @Test
    public void when_uri_192_168_1_is_defined_https_192_168_1_1_slash_nerdytest__is_valid() throws Exception {
        assertTrue(validationToTest.isWhitelisted("https://192.168.1.1/nerdytest", Arrays.asList(new URI("https://192.168.1.1"))));
    }

}
