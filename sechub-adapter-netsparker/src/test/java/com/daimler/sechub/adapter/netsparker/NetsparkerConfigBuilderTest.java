// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.adapter.AbstractAdapterConfig;
import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.adapter.SecHubTimeUnit;
import com.daimler.sechub.adapter.SecHubTimeUnitData;
import com.daimler.sechub.adapter.netsparker.NetsparkerConfig.NetsparkerConfigBuilder;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

/**
 * Name handling is tested here because its very important when using NETSPARKER (each name produces costs)
 * @author Albert Tregnaghi
 *
 */
public class NetsparkerConfigBuilderTest {

	@Rule
    public ExpectedException expectedException = ExpectedExceptionFactory.none();

	@Test
	public void uris_with_different_roots_are_not_accepted() throws Exception {
		/* prepare */
		expectedException.expect(IllegalStateException.class);

		/* execute */
		Set<URI> collection = new LinkedHashSet<>();
		collection.add(URI.create("https://www.example1.com"));
		collection.add(URI.create("https://www.example2.com"));


		validConfigAnd().setTargetURIs(collection).build();

	}

	@Test
	public void uris_with_different_roots_are_not_accepted_even_when_same_main_domain() throws Exception {
		/* prepare */
		expectedException.expect(IllegalStateException.class);

		/* execute */
		Set<URI> collection = new LinkedHashSet<>();
		collection.add(URI.create("https://www.example1.com"));
		collection.add(URI.create("https://other.example1.com"));


		validConfigAnd().setTargetURIs(collection).build();

	}

	@Test
	public void uris_with_same_roots_are_accepteden() throws Exception {

		/* execute */
		Set<URI> collection = new LinkedHashSet<>();
		collection.add(URI.create("https://www.example1.com/app1"));
		collection.add(URI.create("https://www.example1.com/app2"));

		NetsparkerConfig cfg = validConfigAnd().setTargetURIs(collection).build();

		/* test */
		String websiteName = cfg.getWebsiteName();
		assertNotNull(websiteName);
		assertEquals("www.example1.com_default", websiteName);

	}

	@Test
	/* reason of fail: this is not a valid java url */
	public void xxx_www_example_com__is_throwing_illegal_argument() throws Exception {
		/* prepare */
		expectedException.expect(IllegalArgumentException.class);

		/* execute */
		validConfigAnd().setTargetURI(URI.create("xxx://www.example.com")).build();

	}

	@Test
	/* reason of to not fail: valid url, port can be determined. Its up to the product to fail
	 * or to support this combination - and may change in future. So keep stupid config as is.*/
	public void ftp_www_example_com_8080_is_not_throwing_illegal_argumentx() throws Exception {
		/* prepare */

		/* execute */
		NetsparkerAdapterConfig cfg = validConfigAnd().setTargetURI(URI.create("ftp://www.example.com:8080")).build();

		/* test */
		String websiteName = cfg.getWebsiteName();
		assertNotNull(websiteName);
		assertEquals("www.example.com_8080", websiteName);

	}

	@Test
	public void http_www_example_com__is_websitename__www_example_com_underscore_default() throws Exception {
		/* prepare */
		/* execute */
		NetsparkerAdapterConfig cfg = validConfigAnd().setTargetURI(URI.create("http://www.example.com")).build();

		/* test */
		String websiteName = cfg.getWebsiteName();
		assertNotNull(websiteName);
		assertEquals("www.example.com_default", websiteName);
	}

	@Test
	public void http_www_EXAMPLE_com__is_websitename__www_example_com_underscore_default() throws Exception {
		/* prepare */
		/* execute */
		NetsparkerAdapterConfig cfg = validConfigAnd().setTargetURI(URI.create("http://www.EXAMPLE.com")).build();

		/* test */
		String websiteName = cfg.getWebsiteName();
		assertNotNull(websiteName);
		assertEquals("www.example.com_default", websiteName);
	}

	@Test
	public void https_www_example_com__is_websitename__www_example_com_underscore_default() throws Exception {
		/* prepare */
		/* execute */
		NetsparkerAdapterConfig cfg = validConfigAnd().setTargetURI(URI.create("https://www.example.com")).build();

		/* test */
		String websiteName = cfg.getWebsiteName();
		assertNotNull(websiteName);
		assertEquals("www.example.com_default", websiteName);
	}

	@Test
	public void http_www_example_com_8080_is_websitename__www_example_com_underscore_8080() throws Exception {
		/* prepare */
		/* execute */
		NetsparkerAdapterConfig cfg = validConfigAnd().setTargetURI(URI.create("http://www.example.com:8080")).build();

		/* test */
		String websiteName = cfg.getWebsiteName();
		assertNotNull(websiteName);
		assertEquals("www.example.com_8080", websiteName);
	}

	@Test
	public void https_www_example_com_8443_is_websitename__www_example_com_underscore_8443() throws Exception {
		/* prepare */
		/* execute */
		NetsparkerAdapterConfig cfg = validConfigAnd().setTargetURI(URI.create("http://www.example.com:8443")).build();

		/* test */
		String websiteName = cfg.getWebsiteName();
		assertNotNull(websiteName);
		assertEquals("www.example.com_8443", websiteName);
	}

	@Test
	public void emptyAgentGroupSet_returns_has_agentgroup_false() throws Exception {
		/* prepare */
		/* execute */
		NetsparkerAdapterConfig cfg = validConfigAnd().setAgentGroupName("").build();

		/* test */
		assertFalse(cfg.hasAgentGroup());
	}

	@Test
	public void nullAgentGroupSet_returns_has_agentgroup_false() throws Exception {
		/* prepare */
		/* execute */
		NetsparkerAdapterConfig cfg = validConfigAnd().setAgentGroupName(null).build();

		/* test */
		assertFalse(cfg.hasAgentGroup());
	}

	@Test
	public void agentGroupSet_returns_has_agentgroup_true() throws Exception {
		/* prepare */
		/* execute */
		NetsparkerAdapterConfig cfg = validConfigAnd().setAgentGroupName("agentGroup1").build();

		/* test */
		assertTrue(cfg.hasAgentGroup());
	}


	@Test
	public void getAgentGroup_returns_builder_value() throws Exception {
		/* prepare */
		/* execute */
		NetsparkerAdapterConfig cfg = validConfigAnd().setAgentGroupName("agentGroup1").build();

		/* test */
		assertEquals("agentGroup1", cfg.getAgentGroupName());
	}

	@Test
	public void getAgent_returns_builder_value() throws Exception {
		/* prepare */
		/* execute */
		NetsparkerAdapterConfig cfg = validConfigAnd().setAgentName("agent1").build();

		/* test */
		assertEquals("agent1", cfg.getAgentName());
	}

	@Test
	public void configBuilder_is_child_of_abstract_adapter_config_builder() {
		assertTrue(AbstractAdapterConfigBuilder.class.isAssignableFrom(NetsparkerConfigBuilder.class));
	}
	@Test
	public void config_is_child_of_abstract_adapter_config() {
		assertTrue(AbstractAdapterConfig.class.isAssignableFrom(NetsparkerConfig.class));
	}
	
	@Test
	public void getMaxScanDuration_returns_builder_value() {
	    /* prepare */
	    SecHubTimeUnitData maxScanDuration = SecHubTimeUnitData.of(98, SecHubTimeUnit.MINUTE);
	    
	    /* execute */
	    NetsparkerAdapterConfig adapterConfig = validConfigAnd().setMaxScanDuration(maxScanDuration).build();
	    
	    /* test */
        assertEquals(maxScanDuration, adapterConfig.getMaxScanDuration());
    }

    @Test
    public void hasMaxScanDuration_returns_true() {
        /* prepare */
        SecHubTimeUnitData maxScanDuration = SecHubTimeUnitData.of(1, SecHubTimeUnit.HOUR);

        /* execute */
        NetsparkerAdapterConfig adapterConfig = validConfigAnd().setMaxScanDuration(maxScanDuration).build();

        /* test */
        assertTrue(adapterConfig.hasMaxScanDuration());
    }
	   
    @Test
    public void hasMaxScanDuration_returns_false() throws Exception {
        /* prepare */
        /* execute */
        NetsparkerAdapterConfig adapterConfig = validConfigAnd().build();

        /* test */
        assertFalse(adapterConfig.hasMaxScanDuration());
    }

    private NetsparkerConfigBuilder validConfigAnd() {
		/* @formatter:off */
		return NetsparkerConfig.builder().
					setPasswordOrAPIToken("apiToken").
					setLicenseID("licenseId").
					setProductBaseUrl("https://netsparker.test.example.org").
					setPolicyID("policyId").
					setUser("userId").
					setTargetURIs(Collections.singleton(URI.create("https://www.unknown.de")));
		/* @formatter:on */
	}

}
