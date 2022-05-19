// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.mercedesbenz.sechub.adapter.AbstractCodeScanAdapterConfig;
import com.mercedesbenz.sechub.adapter.AbstractCodeScanAdapterConfigBuilder;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelReducedCloningSupport;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;

public class PDSCodeScanConfigImpl extends AbstractCodeScanAdapterConfig implements PDSCodeScanConfig {

    private InputStream sourceCodeZipFileInputStream;
    private InputStream binariesTarFileInputStream;

    private String sourceZipFileChecksum;

    private Map<String, String> jobParameters;

    private UUID sechubJobUUID;
    private String pdsProductIdentifier;

    private PDSCodeScanConfigImpl() {
    }

    public String getPdsProductIdentifier() {
        return pdsProductIdentifier;
    }

    @Override
    public InputStream getSourceCodeZipFileInputStreamOrNull() {
        return sourceCodeZipFileInputStream;
    }

    @Override
    public String getSourceCodeZipFileChecksumOrNull() {
        return sourceZipFileChecksum;
    }

    @Override
    public InputStream getBinaryTarFileInputStreamOrNull() {
        return binariesTarFileInputStream;
    }

    public static PDSCodeScanConfigBuilder builder() {
        return new PDSCodeScanConfigBuilder();
    }

    public static class PDSCodeScanConfigBuilder extends AbstractCodeScanAdapterConfigBuilder<PDSCodeScanConfigBuilder, PDSCodeScanConfigImpl>
            implements PDSAdapterConfigBuilder {

        private InputStream sourceCodeZipFileInputStream;
        private Map<String, String> jobParameters;
        private UUID sechubJobUUID;
        private String sourceZipFileChecksum;
        private String pdsProductIdentifier;
        private SecHubConfigurationModel configurationModel;
        private InputStream binariesTarFileInputStream;

        @Override
        public PDSCodeScanConfigBuilder setBinariesTarFileInputStream(InputStream binariesTarFileInputStream) {
            this.binariesTarFileInputStream = binariesTarFileInputStream;
            return this;
        }

        @Override
        public PDSCodeScanConfigBuilder setSourceCodeZipFileInputStream(InputStream sourceCodeZipFileInputStream) {
            this.sourceCodeZipFileInputStream = sourceCodeZipFileInputStream;
            return this;
        }

        @Override
        public PDSCodeScanConfigBuilder setSecHubJobUUID(UUID sechubJobUUID) {
            this.sechubJobUUID = sechubJobUUID;
            return this;
        }

        @Override
        public PDSCodeScanConfigBuilder setSecHubConfigModel(SecHubConfigurationModel model) {
            this.configurationModel = model;
            return this;
        }

        @Override
        public PDSCodeScanConfigBuilder setSourceZipFileChecksum(String sourceZipFileChecksum) {
            this.sourceZipFileChecksum = sourceZipFileChecksum;
            return this;
        }

        @Override
        public PDSCodeScanConfigBuilder setPDSProductIdentifier(String productIdentifier) {
            this.pdsProductIdentifier = productIdentifier;
            return this;
        }

        @Override
        protected void customBuild(PDSCodeScanConfigImpl config) {
            config.sourceCodeZipFileInputStream = sourceCodeZipFileInputStream;
            config.sourceZipFileChecksum = sourceZipFileChecksum;
            config.binariesTarFileInputStream = binariesTarFileInputStream;

            if (configurationModel != null) {
                String reducedConfigJSON = SecHubConfigurationModelReducedCloningSupport.DEFAULT.createReducedScanConfigurationCloneJSON(configurationModel,
                        ScanType.CODE_SCAN);
                jobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_CONFIGURATION, reducedConfigJSON);
            }

            config.jobParameters = Collections.unmodifiableMap(jobParameters);
            config.sechubJobUUID = sechubJobUUID;
            config.pdsProductIdentifier = pdsProductIdentifier;
        }

        @Override
        protected PDSCodeScanConfigImpl buildInitialConfig() {
            return new PDSCodeScanConfigImpl();
        }

        /**
         * Set job parameters - mandatory
         *
         * @param jobParameters a map with key values
         * @return builder
         */
        @Override
        public final PDSCodeScanConfigBuilder setJobParameters(Map<String, String> jobParameters) {
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
            if (sourceCodeZipFileInputStream != null && sourceZipFileChecksum == null) {
                throw new IllegalStateException("sourceZipFileChecksum not set but zipfile inputstream!");
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
