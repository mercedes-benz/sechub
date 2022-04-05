// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.mercedesbenz.sechub.adapter.AbstractWebScanAdapterConfig;
import com.mercedesbenz.sechub.adapter.AbstractWebScanAdapterConfigBuilder;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelReducedCloningSupport;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;

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
            jobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_TYPE, config.getTargetType());
            jobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_URL, config.getTargetAsString());

            if (configurationModel != null) {
                String reducedConfigJSON = SecHubConfigurationModelReducedCloningSupport.DEFAULT.createReducedScanConfigurationCloneJSON(configurationModel,
                        ScanType.WEB_SCAN);
                jobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_CONFIGURATION, reducedConfigJSON);
            }

            config.pdsProductIdentifier = pdsProductIdentifier;
            config.jobParameters = Collections.unmodifiableMap(jobParameters);

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
