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

public class PDSLicenseScanConfigImpl extends AbstractCodeScanAdapterConfig implements PDSLicenseScanConfig {

    private InputStream sourceCodeZipFileInputStream;
    private String sourceZipFileChecksum;

    private Map<String, String> jobParameters;

    private UUID sechubJobUUID;
    private String pdsProductIdentifier;
    public InputStream binariesTarFileInputStream;

    private PDSLicenseScanConfigImpl() {
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

    public static PDSLicenseScanConfigBuilder builder() {
        return new PDSLicenseScanConfigBuilder();
    }

    public static class PDSLicenseScanConfigBuilder extends AbstractCodeScanAdapterConfigBuilder<PDSLicenseScanConfigBuilder, PDSLicenseScanConfigImpl> {

        private InputStream sourceCodeZipFileInputStream;
        private InputStream binariesTarFileInputStream;

        private Map<String, String> jobParameters;
        private UUID sechubJobUUID;
        private String sourceZipFileChecksum;
        private String pdsProductIdentifier;
        private SecHubConfigurationModel configurationModel;

        public PDSLicenseScanConfigBuilder setBinariesTarFileInputStream(InputStream binariesTarFileInputStream) {
            this.binariesTarFileInputStream = binariesTarFileInputStream;
            return this;
        }

        public PDSLicenseScanConfigBuilder setSourceCodeZipFileInputStream(InputStream sourceCodeZipFileInputStream) {
            this.sourceCodeZipFileInputStream = sourceCodeZipFileInputStream;
            return this;
        }

        public PDSLicenseScanConfigBuilder setSecHubJobUUID(UUID sechubJobUUID) {
            this.sechubJobUUID = sechubJobUUID;
            return this;
        }

        public PDSLicenseScanConfigBuilder setSecHubConfigModel(SecHubConfigurationModel model) {
            this.configurationModel = model;
            return this;
        }

        public PDSLicenseScanConfigBuilder setSourceZipFileChecksum(String sourceZipFileChecksum) {
            this.sourceZipFileChecksum = sourceZipFileChecksum;
            return this;
        }

        public PDSLicenseScanConfigBuilder setPDSProductIdentifier(String productIdentifier) {
            this.pdsProductIdentifier = productIdentifier;
            return this;
        }

        @Override
        protected void customBuild(PDSLicenseScanConfigImpl config) {
            config.sourceCodeZipFileInputStream = sourceCodeZipFileInputStream;
            config.sourceZipFileChecksum = sourceZipFileChecksum;
            config.binariesTarFileInputStream = binariesTarFileInputStream;

            if (configurationModel != null) {
                String reducedConfigJSON = SecHubConfigurationModelReducedCloningSupport.DEFAULT.createReducedScanConfigurationCloneJSON(configurationModel,
                        ScanType.LICENSE_SCAN);
                jobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_CONFIGURATION, reducedConfigJSON);
            }

            config.jobParameters = Collections.unmodifiableMap(jobParameters);
            config.sechubJobUUID = sechubJobUUID;
            config.pdsProductIdentifier = pdsProductIdentifier;
        }

        @Override
        protected PDSLicenseScanConfigImpl buildInitialConfig() {
            return new PDSLicenseScanConfigImpl();
        }

        /**
         * Set job parameters - mandatory
         *
         * @param jobParameters a map with key values
         * @return builder
         */
        public final PDSLicenseScanConfigBuilder setJobParameters(Map<String, String> jobParameters) {
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
