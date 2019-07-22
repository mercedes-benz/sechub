// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Collections;

import org.junit.Test;

import com.daimler.sechub.adapter.AbstractAdapterConfig;
import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.adapter.netsparker.NetsparkerConfig.NetsparkerConfigBuilder;

/**
 * Name handling is tested here because its very important when using NETSPARKER (each name produces costs)
 * @author Albert Tregnaghi
 *
 */
public class NetsparkerConfigBuilderTest {
	@Test
	public void websiteName_is_md5_of_root_target_uri() throws Exception {
		/* prepare */
		/* execute */
		NetsparkerAdapterConfig cfg = validConfigAnd().setTargetURI(URI.create("http://www.example.com")).build();

		/* test */
		String websiteName = cfg.getWebsiteName();
		assertNotNull(websiteName);
		assertEquals("847310eb455f9ae37cb56962213c491d", websiteName);
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


	private NetsparkerConfigBuilder validConfigAnd() {
		/* @formatter:off */
		return NetsparkerConfig.builder().
					setApiToken("apiToken").
					setLicenseID("licenseId").
					setProductBaseUrl("https://netsparker.test.example.org").
					setPolicyID("policyId").
					setUser("userId").
					setTargetURIs(Collections.singleton(URI.create("https://www.unknown.de")));
		/* @formatter:on */
	}

}
