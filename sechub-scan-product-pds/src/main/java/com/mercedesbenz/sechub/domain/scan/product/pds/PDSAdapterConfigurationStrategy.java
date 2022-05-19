package com.mercedesbenz.sechub.domain.scan.product.pds;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.mercedesbenz.sechub.adapter.AdapterConfig;
import com.mercedesbenz.sechub.adapter.AdapterConfigBuilder;
import com.mercedesbenz.sechub.adapter.AdapterConfigurationStrategy;
import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.pds.PDSAdapterConfigBuilder;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.domain.scan.DefaultAdapterConfigurationStrategy;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.sharedkernel.execution.SecHubExecutionContext;

/**
 * This strategy will configure
 * <ul>
 * <li>defaults</li> - delegated to {@link DefaultAdapterConfigurationStrategy}
 * <li>PDS product identifier</li> - from strategyConfig support
 * <li>trust all certificates</li> - from strategyConfig support
 * <li>SecHub job UUID</li> - from context
 * <li>SecHub configuration model</li> - from context configuration
 * <li>SecHub configuration model</li> - from context configuration
 *
 * <ul>
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSAdapterConfigurationStrategy implements AdapterConfigurationStrategy {

    private PDSAdapterConfigurationStrategyConfig strategyConfig;

    public static class PDSAdapterConfigurationStrategyBuilder {
        private PDSAdapterConfigurationStrategyConfig strategyConfig;

        private PDSAdapterConfigurationStrategyBuilder() {
            strategyConfig = new PDSAdapterConfigurationStrategyConfig();
        }

        public PDSAdapterConfigurationStrategyBuilder setProductExecutorData(ProductExecutorData productExecutorData) {
            strategyConfig.productExecutorData = productExecutorData;
            return this;
        }

        public PDSAdapterConfigurationStrategyBuilder setConfigSupport(PDSExecutorConfigSuppport configSupport) {
            strategyConfig.configSupport = configSupport;
            return this;
        }

        public PDSAdapterConfigurationStrategyBuilder setInstallSetup(PDSInstallSetup installSetup) {
            strategyConfig.installSetup = installSetup;
            return this;
        }

        public PDSAdapterConfigurationStrategyBuilder setScanType(ScanType scanType) {
            strategyConfig.scanType = scanType;
            return this;
        }

        public PDSAdapterConfigurationStrategyBuilder setContentProvider(PDSStorageContentProvider contentProvider) {
            strategyConfig.contentProvider = contentProvider;
            return this;
        }

        public PDSAdapterConfigurationStrategyBuilder setSourceCodeZipFileInputStreamOrNull(InputStream sourceCodeZipFileInputStream) {
            strategyConfig.sourceCodeZipFileInputStreamOrNull = sourceCodeZipFileInputStream;
            return this;
        }

        public PDSAdapterConfigurationStrategyBuilder setBinariesTarFileInputStreamOrNull(InputStream binariesTarFileInputStream) {
            strategyConfig.binariesTarFileInputStreamOrNull = binariesTarFileInputStream;
            return this;
        }

        public PDSAdapterConfigurationStrategyBuilder setSourceZipFileChecksum(String sourceZipFileChecksum) {
            strategyConfig.sourceZipFileChecksumOrNull = sourceZipFileChecksum;
            return this;
        }

        /**
         * @return {@link PDSAdapterConfigurationStrategy}
         * @throws IllegalStateException when mandatory parameters are not defined (only
         *                               streams and checksums can be null)
         */
        public PDSAdapterConfigurationStrategy build() {
            /* validate manditory parts set */
            if (strategyConfig == null) {
                throw new IllegalStateException("Strategy strategyConfig may not be null");
            }
            if (strategyConfig.productExecutorData == null) {
                throw new IllegalStateException("Data may not be null");
            }
            if (strategyConfig.scanType == null) {
                throw new IllegalStateException("Scan type may not be null");
            }
            if (strategyConfig.configSupport == null) {
                throw new IllegalStateException("Config support not be null");
            }
            if (strategyConfig.installSetup == null) {
                throw new IllegalStateException("PDS install installSetup may not be null");
            }
            if (strategyConfig.contentProvider == null) {
                throw new IllegalStateException("PDS content provier may not be null");
            }
            return new PDSAdapterConfigurationStrategy(strategyConfig);
        }

        public PDSAdapterConfigurationStrategyBuilder setMetaDataOrNull(AdapterMetaData metaDataOrNull) {
            strategyConfig.metaDataOrNull = metaDataOrNull;
            return this;
        }

    }

    private static class PDSAdapterConfigurationStrategyConfig {
        private AdapterMetaData metaDataOrNull;
        private ProductExecutorData productExecutorData;
        private PDSStorageContentProvider contentProvider;
        private PDSExecutorConfigSuppport configSupport;
        private PDSInstallSetup installSetup;
        private ScanType scanType;
        private InputStream sourceCodeZipFileInputStreamOrNull;
        private InputStream binariesTarFileInputStreamOrNull;
        private String sourceZipFileChecksumOrNull;

    }

    public static PDSAdapterConfigurationStrategyBuilder builder() {
        return new PDSAdapterConfigurationStrategyBuilder();
    }

    private PDSAdapterConfigurationStrategy(PDSAdapterConfigurationStrategyConfig config) {
        this.strategyConfig = config;
    }

    @Override
    public <B extends AdapterConfigBuilder, C extends AdapterConfig> void configure(B configBuilder) {
        if (!(configBuilder instanceof PDSAdapterConfigBuilder)) {
            throw new IllegalStateException("This strategy can only configure builders which implement PDSAdapterConfigBuilder!");
        }
        configBuilder
                .configure(new DefaultAdapterConfigurationStrategy(strategyConfig.productExecutorData, strategyConfig.configSupport, strategyConfig.scanType));

        SecHubExecutionContext context = strategyConfig.productExecutorData.getSechubExecutionContext();

        PDSAdapterConfigBuilder pdsConfigBuilder = (PDSAdapterConfigBuilder) configBuilder;

        pdsConfigBuilder.setPDSProductIdentifier(strategyConfig.configSupport.getPDSProductIdentifier());
        pdsConfigBuilder.setTrustAllCertificates(strategyConfig.configSupport.isTrustAllCertificatesEnabled());
        pdsConfigBuilder.setSecHubJobUUID(context.getSechubJobUUID());
        pdsConfigBuilder.setSecHubConfigModel(context.getConfiguration());

        pdsConfigBuilder.setTimeToWaitForNextCheckOperationInMilliseconds(
                strategyConfig.configSupport.getTimeToWaitForNextCheckOperationInMilliseconds(strategyConfig.installSetup));
        pdsConfigBuilder.setTimeOutInMinutes(strategyConfig.configSupport.getTimeoutInMinutes(strategyConfig.installSetup));

        pdsConfigBuilder.setSourceCodeZipFileInputStream(strategyConfig.sourceCodeZipFileInputStreamOrNull);
        pdsConfigBuilder.setSourceZipFileChecksum(strategyConfig.sourceZipFileChecksumOrNull);

        pdsConfigBuilder.setBinariesTarFileInputStream(strategyConfig.binariesTarFileInputStreamOrNull);

        Map<String, String> jobParams = strategyConfig.configSupport.createJobParametersToSendToPDS(context.getConfiguration());
        pdsConfigBuilder.setJobParameters(jobParams);

        try {
            String sourceZipFileChecksum = strategyConfig.contentProvider.getSourceZipFileUploadChecksumOrNull(strategyConfig.metaDataOrNull);
            pdsConfigBuilder.setSourceZipFileChecksum(sourceZipFileChecksum);

        } catch (IOException e) {
            throw new SecHubRuntimeException("Was not able to retrieve source zip upload checksum", e);
        }
    }

}
