// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import com.daimler.sechub.adapter.AbstractAdapterConfig;
import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.adapter.support.MessageDigestSupport;

public class NetsparkerConfig extends AbstractAdapterConfig implements NetsparkerAdapterConfig{

	private String licenseID;

	private String agentName;
	private String agentGroupName;

	private String websiteName;
	
	@Override
	public String getLicenseID() {
		return licenseID;
	}

	@Override
	public String getWebsiteName() {
		return websiteName;
	}

	@Override
	public String getAgentName() {
		return agentName;
	}

	@Override
	public String getAgentGroupName() {
		return agentGroupName;
	}

	@Override
	public boolean hasAgentGroup() {
		return agentGroupName != null && !agentGroupName.isEmpty();
	}

	private NetsparkerConfig() {
	}

	public static NetsparkerConfigBuilder builder() {
		return new NetsparkerConfigBuilder();
	}

	public static class NetsparkerConfigBuilder
			extends AbstractAdapterConfigBuilder<NetsparkerConfigBuilder, NetsparkerAdapterConfig> {

		MessageDigestSupport md5Builder = new MessageDigestSupport();

		private String licenseID;
		private String agentName;
		private String agentGroupName;

		private NetsparkerConfigBuilder() {
		}

		public NetsparkerConfigBuilder setAgentName(String agentName) {
			this.agentName = agentName;
			return this;
		}

		public NetsparkerConfigBuilder setAgentGroupName(String agentGroupName) {
			this.agentGroupName = agentGroupName;
			return this;
		}

		public NetsparkerConfigBuilder setLicenseID(String licenseID) {
			this.licenseID = licenseID;
			return this;
		}

		@Override
		protected void customBuild(NetsparkerAdapterConfig adapterConfig) {
			if (! (adapterConfig instanceof NetsparkerConfig)) {
				throw new IllegalArgumentException("not a netsparker config:"+adapterConfig);
			}
			NetsparkerConfig config = (NetsparkerConfig)adapterConfig;
			String websiteURLAsString = config.getRootTargetURIasString();
			if (websiteURLAsString==null) {
				throw new IllegalStateException("website url (root target url ) may not be null at this point!");
			}
			config.websiteName= md5Builder.createMD5(websiteURLAsString);
			config.licenseID = licenseID;
			config.agentName = agentName;
			config.agentGroupName = agentGroupName;
		}

		@Override
		protected void customValidate() {
			assertUserSet();
			assertAPITokenSet();
			assertLicenseIDSet();
			assertProductBaseURLSet();
		}

		private void assertLicenseIDSet() {
			if (licenseID == null) {
				throw new IllegalStateException("no licenseID given");
			}
		}

		@Override
		protected NetsparkerAdapterConfig buildInitialConfig() {
			return new NetsparkerConfig();
		}

	}

}
