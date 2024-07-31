// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import com.mercedesbenz.sechub.adapter.AbstractCodeScanAdapterConfig;
import com.mercedesbenz.sechub.adapter.AbstractCodeScanAdapterConfigBuilder;

public class PDSSecretScanConfigImpl extends AbstractCodeScanAdapterConfig implements PDSSecretScanConfig {

    private PDSAdapterConfigData configData;

    private PDSSecretScanConfigImpl() {
    }

    @Override
    public PDSAdapterConfigData getPDSAdapterConfigData() {
        return configData;
    }

    public static PDSSecretScanConfigBuilder builder() {
        return new PDSSecretScanConfigBuilder();
    }

    public static class PDSSecretScanConfigBuilder extends AbstractCodeScanAdapterConfigBuilder<PDSSecretScanConfigBuilder, PDSSecretScanConfigImpl>
            implements PDSAdapterConfigBuilder {
        private PDSAdapterDataConfigurator configurator = new PDSAdapterDataConfigurator();

        @Override
        protected PDSSecretScanConfigImpl buildInitialConfig() {
            return new PDSSecretScanConfigImpl();
        }

        @Override
        protected void customBuild(PDSSecretScanConfigImpl config) {
            configurator.configure();
            config.configData = configurator;
        }

        @Override
        public PDSAdapterConfigurator getPDSAdapterConfigurator() {
            return configurator;
        }

        @Override
        protected void customValidate() {
            assertUserSet();
            assertPasswordSet();
            assertProjectIdSet();
            assertProductBaseURLSet();

            configurator.validateNonCalculatedParts();
        }

    }

}
