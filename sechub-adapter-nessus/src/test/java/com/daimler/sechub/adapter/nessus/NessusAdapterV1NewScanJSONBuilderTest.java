// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.nessus;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.URI;
import java.util.Collections;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class NessusAdapterV1NewScanJSONBuilderTest {

    private NessusAdapterV1NewScanJSONBuilder builderToTest;

    @Before
    public void before() throws Exception {
        builderToTest = new NessusAdapterV1NewScanJSONBuilder();
    }

    @Test
    public void standard_setup_with_one_url_build_is_json_and_has_expected_content() throws Exception {

        /* execute */
        /* @formatter:off */
		String result = builderToTest.
			description("description1").
			name("name1").
			uuid("uuid1").
			targetsURIs(Collections.singleton(URI.create("http://www.example.com"))).
			build();
		/* @formatter:on */

        /* test */
        JSONObject asJson = new JSONObject(result);// so valid json..
        assertEquals("uuid1", asJson.getString("uuid"));

        JSONObject settings = asJson.getJSONObject("settings");
        assertEquals("name1", settings.get("name"));
        assertEquals("http://www.example.com", settings.getString("text_targets"));
        assertEquals("description1", settings.get("description"));

    }

    @Test
    public void standard_setup_with_one_ip_build_is_json_and_has_expected_content() throws Exception {

        /* execute */
        /* @formatter:off */
		String result = builderToTest.
			description("description1").
			name("name1").
			uuid("uuid1").
			targetIPs(Collections.singleton(InetAddress.getByName("192.168.1.1"))).
			build();
		/* @formatter:on */

        /* test */
        JSONObject asJson = new JSONObject(result);// so valid JSON..
        assertEquals("uuid1", asJson.getString("uuid"));

        JSONObject settings = asJson.getJSONObject("settings");
        assertEquals("name1", settings.get("name"));
        assertEquals("192.168.1.1", settings.getString("text_targets"));
        assertEquals("description1", settings.get("description"));

    }

    @Test
    public void standard_setup_with_one_url_and_another_IP_build_is_json_and_has_expected_content_uris_first() throws Exception {

        /* execute */
        /* @formatter:off */
		String result = builderToTest.
			description("description1").
			name("name1").
			uuid("uuid1").
			targetIPs(Collections.singleton(InetAddress.getByName("192.168.1.1"))).
			targetsURIs(Collections.singleton(URI.create("http://www.example.com"))).
			build();
		/* @formatter:on */

        /* test */
        JSONObject asJson = new JSONObject(result);// so valid json..
        assertEquals("uuid1", asJson.getString("uuid"));

        JSONObject settings = asJson.getJSONObject("settings");
        assertEquals("name1", settings.get("name"));
        assertEquals("http://www.example.com,192.168.1.1", settings.getString("text_targets"));
        assertEquals("description1", settings.get("description"));

    }

    @Test
    public void nothing_set_results_simply_in_valid_json_but_without_any_text_targets_and_field_as_null() throws Exception {
        /* execute */
        String result = builderToTest.build();

        /* test */
        JSONObject asJson = new JSONObject(result);// so valid json..
        assertEquals("null", asJson.getString("uuid"));

        JSONObject settings = asJson.getJSONObject("settings");
        assertEquals("null", settings.get("name"));
        assertEquals("", settings.getString("text_targets"));
        assertEquals("null", settings.get("description"));
    }
}
