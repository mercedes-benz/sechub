// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.mercedesbenz.sechub.adapter.AbstractInfraScanAdapterConfig;
import com.mercedesbenz.sechub.adapter.AbstractInfraScanAdapterConfigBuilder;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelReducedCloningSupport;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;

public class PDSInfraScanConfigImpl extends AbstractInfraScanAdapterConfig implements PDSInfraScanConfig {

    Map<String, String> jobParameters;
    UUID sechubJobUUID;
    String pdsProductIdentifier;

    private PDSInfraScanConfigImpl() {
    }

    public String getPdsProductIdentifier() {
        return pdsProductIdentifier;
    }

    public static PDSInfraScanConfigBuilder builder() {
        return new PDSInfraScanConfigBuilder();
    }

    public static class PDSInfraScanConfigBuilder extends AbstractInfraScanAdapterConfigBuilder<PDSInfraScanConfigBuilder, PDSInfraScanConfigImpl> {

        private Map<String, String> jobParameters;
        private UUID sechubJobUUID;
        private String pdsProductIdentifier;
        private SecHubConfigurationModel configurationModel;

        public PDSInfraScanConfigBuilder setPDSProductIdentifier(String productIdentifier) {
            this.pdsProductIdentifier = productIdentifier;
            return this;
        }

        @Override
        protected void customBuild(PDSInfraScanConfigImpl config) {
            jobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_TYPE, config.getTargetType());

            if (configurationModel != null) {
                String reducedConfigJSON = SecHubConfigurationModelReducedCloningSupport.DEFAULT.createReducedScanConfigurationCloneJSON(configurationModel,
                        ScanType.INFRA_SCAN);
                jobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_CONFIGURATION, reducedConfigJSON);
            }

            config.jobParameters = Collections.unmodifiableMap(jobParameters);
            config.sechubJobUUID = sechubJobUUID;
            config.pdsProductIdentifier = pdsProductIdentifier;
        }

        @Override
        protected PDSInfraScanConfigImpl buildInitialConfig() {
            return new PDSInfraScanConfigImpl();
        }

        public PDSInfraScanConfigBuilder setSecHubJobUUID(UUID sechubJobUUID) {
            this.sechubJobUUID = sechubJobUUID;
            return this;
        }

        public PDSInfraScanConfigBuilder setSecHubConfigModel(SecHubConfigurationModel model) {
            this.configurationModel = model;
            return this;
        }

        /**
         * Set job parameters - mandatory
         *
         * @param jobParameters a map with key values
         * @return builder
         */
        public final PDSInfraScanConfigBuilder setJobParameters(Map<String, String> jobParameters) {
            this.jobParameters = jobParameters;
            return this;
        }

        @Override
        protected void customValidate() {
            assertUserSet();
            assertPasswordSet();
            assertProjectIdSet();
            assertProductBaseURLSet();

            if (pdsProductIdentifier == null) {
                throw new IllegalStateException("pds product identifier not set!");
            }
            if (jobParameters == null) {
                throw new IllegalStateException("job parameters not set!");
            }
            if (sechubJobUUID == null) {
                throw new IllegalStateException("sechubJobUUID not set!");
            }
        }

    }

    @Override
    public Map<String, String> getJobParameters() {
        return jobParameters;
    }

    @Override
    public UUID getSecHubJobUUID() {
        return sechubJobUUID;
    }
}
