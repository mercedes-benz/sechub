// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.adapter.testclasses.TestAdapter;
import com.mercedesbenz.sechub.adapter.testclasses.TestAdapterConfigInterface;

public class AbstractAdapterTest {

    private TestAdapter adapterToTest;

    @Before
    public void before() throws Exception {
        adapterToTest = new TestAdapter();
    }

    @Test
    public void get_api_for_website_slash_create_returns_full_url1() {
        /* prepare */
        TestAdapterConfigInterface config = mock(TestAdapterConfigInterface.class);
        when(config.getProductBaseURL()).thenReturn("https://mynetsparker.intranet.com:8080");

        /* execute */
        String url = adapterToTest.createAPIURL("website/create", config);

        /* test */
        assertEquals("https://mynetsparker.intranet.com:8080/website/create", url);

    }

    @Test
    public void get_api_for_slash_website_slash_create_returns_full_url_without_double_slashes1() {
        /* prepare */
        TestAdapterConfigInterface config = mock(TestAdapterConfigInterface.class);
        when(config.getProductBaseURL()).thenReturn("https://mynessus.intranet.com:8080");

        /* execute */
        String url = adapterToTest.createAPIURL("/website/create", config);

        /* test */
        assertEquals("https://mynessus.intranet.com:8080/website/create", url);

    }

    @Test
    public void get_api_for_website_slash_create_returns_full_url() {
        /* prepare */
        adapterToTest.setApiPrefix("api/1.0");
        TestAdapterConfigInterface config = mock(TestAdapterConfigInterface.class);
        when(config.getProductBaseURL()).thenReturn("https://mynetsparker.intranet.com:8080");

        /* execute */
        String url = adapterToTest.createAPIURL("website/create", config);

        /* test */
        assertEquals("https://mynetsparker.intranet.com:8080/api/1.0/website/create", url);

    }

    @Test
    public void get_api_for_slash_website_slash_create_returns_full_url_without_double_slashes() {
        /* prepare */
        adapterToTest.setApiPrefix("api/1.0");
        TestAdapterConfigInterface config = mock(TestAdapterConfigInterface.class);
        when(config.getProductBaseURL()).thenReturn("https://mynetsparker.intranet.com:8080");

        /* execute */
        String url = adapterToTest.createAPIURL("/website/create", config);

        /* test */
        assertEquals("https://mynetsparker.intranet.com:8080/api/1.0/website/create", url);

    }

    @Test
    public void api_prefix_with_start_and_end_base_has_also_end_api_has_start_and_end() {
        /* prepare */
        adapterToTest.setApiPrefix("/api/1.0/");
        TestAdapterConfigInterface config = mock(TestAdapterConfigInterface.class);
        when(config.getProductBaseURL()).thenReturn("https://mynetsparker.intranet.com:8080/");

        /* execute */
        String url = adapterToTest.createAPIURL("/website/create/", config);

        /* test */
        assertEquals("https://mynetsparker.intranet.com:8080/api/1.0/website/create", url);

    }

}
