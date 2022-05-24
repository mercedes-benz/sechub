package com.mercedesbenz.sechub.domain.scan.product.pds;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.mercedesbenz.sechub.adapter.AdapterConfig;
import com.mercedesbenz.sechub.adapter.AdapterConfigBuilder;
import com.mercedesbenz.sechub.adapter.AdapterConfigurationStrategy;
import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.pds.PDSAdapterConfigurator;
import com.mercedesbenz.sechub.adapter.pds.PDSAdapterConfiguratorProvider;
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
 * <li>Input streams</li>
 * <li>Input stream checksums</li>
 * <li>... and all other PDS specific parts</li> - which are common for every
 * PDS adapter
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

    }

    public static PDSAdapterConfigurationStrategyBuilder builder() {
        return new PDSAdapterConfigurationStrategyBuilder();
    }

    private PDSAdapterConfigurationStrategy(PDSAdapterConfigurationStrategyConfig config) {
        this.strategyConfig = config;
    }

    @Override
    public <B extends AdapterConfigBuilder, C extends AdapterConfig> void configure(B configBuilder) {
        PDSAdapterConfigurator pdsConfigurator = null;
        if (configBuilder instanceof PDSAdapterConfiguratorProvider) {
            PDSAdapterConfiguratorProvider provider = (PDSAdapterConfiguratorProvider) configBuilder;
            pdsConfigurator = provider.getPDSAdapterConfigurator();

        } else if (configBuilder instanceof PDSAdapterConfigurator) {
            pdsConfigurator = (PDSAdapterConfigurator) configBuilder;
        }
        if (pdsConfigurator == null) {
            throw new IllegalStateException(
                    "This strategy can only configure builders which implement PDSAdapterConfigurator or PDSAdapterConfiguratorProvider!");
        }
        handleCommonParts(configBuilder);
        handlePdsParts(pdsConfigurator);
    }

    private void handlePdsParts(PDSAdapterConfigurator pdsConfigurable) {
        SecHubExecutionContext context = strategyConfig.productExecutorData.getSechubExecutionContext();
        Map<String, String> jobParameters = strategyConfig.configSupport.createJobParametersToSendToPDS(context.getConfiguration());

        pdsConfigurable.setJobParameters(jobParameters);
        pdsConfigurable.setReusingSecHubStorage(PDSExecutorConfigSuppport.isReusingSecHubStorage(jobParameters));
        pdsConfigurable.setScanType(strategyConfig.scanType);
        pdsConfigurable.setPdsProductIdentifier(strategyConfig.configSupport.getPDSProductIdentifier());
        pdsConfigurable.setSecHubJobUUID(context.getSechubJobUUID());
        pdsConfigurable.setSecHubConfigurationModel(context.getConfiguration());

        pdsConfigurable.setSourceCodeZipFileInputStreamOrNull(strategyConfig.sourceCodeZipFileInputStreamOrNull);

        pdsConfigurable.setBinaryTarFileInputStreamOrNull(strategyConfig.binariesTarFileInputStreamOrNull);

        try {
            String sourceZipFileChecksum = strategyConfig.contentProvider.getSourceZipFileUploadChecksumOrNull(strategyConfig.metaDataOrNull);
            pdsConfigurable.setSourceCodeZipFileChecksumOrNull(sourceZipFileChecksum);

        } catch (IOException e) {
            throw new SecHubRuntimeException("Was not able to retrieve source zip upload checksum", e);
        }
    }

    private <B extends AdapterConfigBuilder> void handleCommonParts(B configBuilder) {
        /* standard configuration */
        /*
         * TODO Albert Tregnaghi, 2022-05-20: We should move the "configBuilder" parts
         * to another configuration strategy and use this in every adapter not only for
         * PDS. But we cannot do this as long as the install setup is different and
         * there are still some static setup - like for checkmarx - and not all
         * available by configuration support.
         */
        configBuilder
                .configure(new DefaultAdapterConfigurationStrategy(strategyConfig.productExecutorData, strategyConfig.configSupport, strategyConfig.scanType));
        configBuilder.setTrustAllCertificates(strategyConfig.configSupport.isTrustAllCertificatesEnabled());
        configBuilder.setTimeToWaitForNextCheckOperationInMilliseconds(
                strategyConfig.configSupport.getTimeToWaitForNextCheckOperationInMilliseconds(strategyConfig.installSetup));
        configBuilder.setTimeOutInMinutes(strategyConfig.configSupport.getTimeoutInMinutes(strategyConfig.installSetup));
    }

}
