// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.daimler.sechub.adapter.AbstractWebScanAdapterConfig;
import com.daimler.sechub.adapter.AbstractWebScanAdapterConfigBuilder;
import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.commons.model.SecHubConfigurationModel;
import com.daimler.sechub.commons.model.SecHubConfigurationModelReducedCloningSupport;
import com.daimler.sechub.commons.pds.PDSDefaultParameterKeyConstants;

public class PDSWebScanConfigImpl extends AbstractWebScanAdapterConfig implements PDSWebScanConfig {

    private String websiteName;

    private Map<String, String> jobParameters;

    private UUID sechubJobUUID;
    private String pdsProductIdentifier;

    public String getWebsiteName() {
        return websiteName;
    }

    @Override
    public Map<String, String> getJobParameters() {
        return jobParameters;
    }

    public String getPdsProductIdentifier() {
        return pdsProductIdentifier;
    }

    private PDSWebScanConfigImpl() {
    }

    public static PDSWebScanConfigBuilder builder() {
        return new PDSWebScanConfigBuilder();
    }

    public static class PDSWebScanConfigBuilder extends AbstractWebScanAdapterConfigBuilder<PDSWebScanConfigBuilder, PDSWebScanConfigImpl> {

        private Map<String, String> jobParameters;
        private UUID sechubJobUUID;
        private String pdsProductIdentifier;
        private SecHubConfigurationModel configurationModel;

        private PDSWebScanConfigBuilder() {
        }

        public PDSWebScanConfigBuilder setSecHubJobUUID(UUID sechubJobUUID) {
            this.sechubJobUUID = sechubJobUUID;
            return this;
        }

        public PDSWebScanConfigBuilder setPDSProductIdentifier(String productIdentifier) {
            this.pdsProductIdentifier = productIdentifier;
            return this;
        }

        public PDSWebScanConfigBuilder setSecHubConfigModel(SecHubConfigurationModel model) {
            this.configurationModel = model;
            return this;
        }

        /**
         * Set job parameters - mandatory
         *
         * @param jobParameters a map with key values
         * @return builder
         */
        public final PDSWebScanConfigBuilder setJobParameters(Map<String, String> jobParameters) {
            this.jobParameters = jobParameters;
            return this;
        }

        @Override
        protected void customBuild(PDSWebScanConfigImpl config) {
            if (configurationModel==null) {
                throw new IllegalStateException("configuration model not set!");
            }
            String reducedConfigJSON = SecHubConfigurationModelReducedCloningSupport.DEFAULT
                    .createReducedScanConfigurationCloneJSON(configurationModel, ScanType.WEB_SCAN);

            jobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_TARGET_TYPE, config.getTargetType());
            jobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_SCAN_TARGET_URL, config.getTargetAsString());
            jobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_SCAN_CONFIGURATION, reducedConfigJSON);

            config.pdsProductIdentifier = pdsProductIdentifier;
            config.jobParameters = Collections.unmodifiableMap(jobParameters);

            int size = config.getRootTargetURIs().size();
            if (size != 1) {
                /* wee provide ONE root uri for webscans */
                throw new IllegalStateException("netsparker must have ONE unique root target uri and not many!");
            }
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
                config.sechubJobUUID = sechubJobUUID;
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

            if (jobParameters == null) {
                throw new IllegalStateException("job parameters not set!");
            }
            if (sechubJobUUID == null) {
                throw new IllegalStateException("sechubJobUUID not set!");
            }
        }

        @Override
        protected PDSWebScanConfigImpl buildInitialConfig() {
            return new PDSWebScanConfigImpl();
        }

    }

    @Override
    public UUID getSecHubJobUUID() {
        return sechubJobUUID;
    }

}
