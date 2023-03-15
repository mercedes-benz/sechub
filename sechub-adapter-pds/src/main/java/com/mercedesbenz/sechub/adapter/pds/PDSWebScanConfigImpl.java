// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.net.MalformedURLException;
import java.net.URL;

import com.mercedesbenz.sechub.adapter.AbstractWebScanAdapterConfig;
import com.mercedesbenz.sechub.adapter.AbstractWebScanAdapterConfigBuilder;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;

public class PDSWebScanConfigImpl extends AbstractWebScanAdapterConfig implements PDSWebScanConfig {

    private String websiteName;
    private PDSAdapterConfigData configData;

    public String getWebsiteName() {
        return websiteName;
    }

    @Override
    public PDSAdapterConfigData getPDSAdapterConfigData() {
        return configData;
    }

    private PDSWebScanConfigImpl() {
    }

    public static PDSWebScanConfigBuilder builder() {
        return new PDSWebScanConfigBuilder();
    }

    public static class PDSWebScanConfigBuilder extends AbstractWebScanAdapterConfigBuilder<PDSWebScanConfigBuilder, PDSWebScanConfigImpl>
            implements PDSAdapterConfigBuilder {
        private PDSAdapterDataConfigurator configurator = new PDSAdapterDataConfigurator();

        @Override
        public PDSAdapterConfigurator getPDSAdapterConfigurator() {
            return configurator;
        }

        private PDSWebScanConfigBuilder() {
        }

        @Override
        protected void customBuild(PDSWebScanConfigImpl config) {
            /*
             * we must set the target type before calling configurator.configure() !
             * Otherwise job parameters not correct calculated
             */
            configurator.setTargetType(config.getTargetType());

            configurator.configure();
            config.configData = configurator;

            configurator.addJobParameter(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_URL, config.getTargetAsString());

            String websiteURLAsString = config.getRootTargetURIasString();
            if (websiteURLAsString == null) {
                throw new IllegalStateException("website url (root target url ) may not be null at this point!");
            }
            try {
                URL url = new URL(websiteURLAsString);
                StringBuilder sb = new StringBuilder();
                sb.append(url.getHost());
                sb.append("_");
                int port = url.getPort();
                if (port < 1) {
                    sb.append("default");
                } else {
                    sb.append(port);
                }

                config.websiteName = sb.toString().toLowerCase();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("website root url '" + websiteURLAsString + "' is not a valid URL!", e);
            }
        }

        @Override
        protected void customValidate() {
            assertUserSet();
            assertPasswordSet();
            assertProjectIdSet();
            assertProductBaseURLSet();

        }

        @Override
        protected PDSWebScanConfigImpl buildInitialConfig() {
            return new PDSWebScanConfigImpl();
        }

    }

}
