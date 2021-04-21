// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import java.net.MalformedURLException;
import java.net.URL;

import com.daimler.sechub.adapter.AbstractWebScanAdapterConfig;
import com.daimler.sechub.adapter.AbstractWebScanAdapterConfigBuilder;

public class NetsparkerConfig extends AbstractWebScanAdapterConfig implements NetsparkerAdapterConfig{

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
			extends AbstractWebScanAdapterConfigBuilder<NetsparkerConfigBuilder, NetsparkerConfig> {

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
		protected void customBuild(NetsparkerConfig adapterConfig) {
			if (! (adapterConfig instanceof NetsparkerConfig)) {
				throw new IllegalArgumentException("not a netsparker config:"+adapterConfig);
			}
			NetsparkerConfig config = (NetsparkerConfig)adapterConfig;
			int size = config.getRootTargetURIs().size();
			if (size!=1) {
				/* netsparker needs ONE root uri */
				throw new IllegalStateException("netsparker must have ONE unique root target uri and not many!");
			}
			String websiteURLAsString = config.getRootTargetURIasString();
			if (websiteURLAsString==null) {
				throw new IllegalStateException("website url (root target url) may not be null at this point!");
			}
			try {
				URL url = new URL(websiteURLAsString);
				StringBuilder sb = new StringBuilder();
				sb.append(url.getHost());
				sb.append("_");
				int port = url.getPort();
				if (port<1) {
					sb.append("default");
				}else {
					sb.append(port);
				}

				config.websiteName = sb.toString().toLowerCase();
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException("website root url '"+websiteURLAsString+"' is not a valid URL!",e);
			}
			config.licenseID = licenseID;
			config.agentName = agentName;
			config.agentGroupName = agentGroupName;
		}

		@Override
		protected void customValidate() {
			assertUserSet();
			assertPasswordSet();
			assertLicenseIDSet();
			assertProductBaseURLSet();
		}

		private void assertLicenseIDSet() {
			if (licenseID == null) {
				throw new IllegalStateException("no licenseID given");
			}
		}

		@Override
		protected NetsparkerConfig buildInitialConfig() {
			return new NetsparkerConfig();
		}

	}
}
