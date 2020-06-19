// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.net.MalformedURLException;
import java.net.URL;

import com.daimler.sechub.adapter.AbstractWebScanAdapterConfig;
import com.daimler.sechub.adapter.AbstractWebScanAdapterConfigBuilder;

public class PDSWebScanConfig extends AbstractWebScanAdapterConfig implements PDSAdapterConfig{

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

	private PDSWebScanConfig() {
	}

	public static PDSWebScanConfigBuilder builder() {
		return new PDSWebScanConfigBuilder();
	}


	public static class PDSWebScanConfigBuilder
			extends AbstractWebScanAdapterConfigBuilder<PDSWebScanConfigBuilder, PDSWebScanConfig> {

		private String licenseID;
		private String agentName;
		private String agentGroupName;

		private PDSWebScanConfigBuilder() {
		}

		public PDSWebScanConfigBuilder setAgentName(String agentName) {
			this.agentName = agentName;
			return this;
		}

		public PDSWebScanConfigBuilder setAgentGroupName(String agentGroupName) {
			this.agentGroupName = agentGroupName;
			return this;
		}

		public PDSWebScanConfigBuilder setLicenseID(String licenseID) {
			this.licenseID = licenseID;
			return this;
		}

		@Override
		protected void customBuild(PDSWebScanConfig adapterConfig) {
			if (! (adapterConfig instanceof PDSWebScanConfig)) {
				throw new IllegalArgumentException("not a netsparker config:"+adapterConfig);
			}
			PDSWebScanConfig config = (PDSWebScanConfig)adapterConfig;
			int size = config.getRootTargetURIs().size();
			if (size!=1) {
				/* netsparker needs ONE root uri */
				throw new IllegalStateException("netsparker must have ONE unique root target uri and not many!");
			}
			String websiteURLAsString = config.getRootTargetURIasString();
			if (websiteURLAsString==null) {
				throw new IllegalStateException("website url (root target url ) may not be null at this point!");
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

				config.websiteName= sb.toString().toLowerCase();
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
		protected PDSWebScanConfig buildInitialConfig() {
			return new PDSWebScanConfig();
		}

	}

}
