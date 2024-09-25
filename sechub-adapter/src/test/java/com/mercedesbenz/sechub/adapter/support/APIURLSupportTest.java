// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.support;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.adapter.AdapterConfig;

public class APIURLSupportTest {
    private APIURLSupport supportToTest;

    @Before
    public void before() {
        supportToTest = new APIURLSupport();
    }

    @Test
    public void noOtherBaseUrl_nomap() {
        /* prepare */
        AdapterConfig config = mock(AdapterConfig.class);
        when(config.getProductBaseURL()).thenReturn("baseUrl");

        /* execute */
        String result = supportToTest.createAPIURL("apiPath", config, "apiPrefix", null, null);

        /* test */
        assertEquals("baseUrl/apiPrefix/apiPath", result);
    }

    @Test
    public void noOtherBaseUrl_butmapset() {
        /* prepare */
        Map<String, String> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        AdapterConfig config = mock(AdapterConfig.class);
        when(config.getProductBaseURL()).thenReturn("baseUrl");

        /* execute */
        String result = supportToTest.createAPIURL("apiPath", config, "apiPrefix", null, map);

        /* test */
        assertEquals("baseUrl/apiPrefix/apiPath?key1=value1&key2=value2", result);
    }

    @Test
    public void otherBaseUrl_mapset() {
        /* prepare */
        Map<String, String> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        AdapterConfig config = mock(AdapterConfig.class);
        when(config.getProductBaseURL()).thenReturn("baseUrl");

        /* execute */
        String result = supportToTest.createAPIURL("apiPath", config, "apiPrefix", "otherBaseUrl", map);

        /* test */
        assertEquals("otherBaseUrl/apiPrefix/apiPath?key1=value1&key2=value2", result);
    }

    @Test
    public void otherBaseUrl_empty_mapset() {
        /* prepare */
        Map<String, String> map = new LinkedHashMap<>();
        AdapterConfig config = mock(AdapterConfig.class);
        when(config.getProductBaseURL()).thenReturn("baseUrl");

        /* execute */
        String result = supportToTest.createAPIURL("apiPath", config, "apiPrefix", "otherBaseUrl", map);

        /* test */
        assertEquals("otherBaseUrl/apiPrefix/apiPath", result);
    }

}
