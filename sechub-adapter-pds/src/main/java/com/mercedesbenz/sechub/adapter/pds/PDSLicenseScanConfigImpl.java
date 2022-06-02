// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import com.mercedesbenz.sechub.adapter.AbstractCodeScanAdapterConfig;
import com.mercedesbenz.sechub.adapter.AbstractCodeScanAdapterConfigBuilder;

public class PDSLicenseScanConfigImpl extends AbstractCodeScanAdapterConfig implements PDSLicenseScanConfig {

    private PDSAdapterConfigData configData;

    private PDSLicenseScanConfigImpl() {
    }

    @Override
    public PDSAdapterConfigData getPDSAdapterConfigData() {
        return configData;
    }

    public static PDSLicenseScanConfigBuilder builder() {
        return new PDSLicenseScanConfigBuilder();
    }

    public static class PDSLicenseScanConfigBuilder extends AbstractCodeScanAdapterConfigBuilder<PDSLicenseScanConfigBuilder, PDSLicenseScanConfigImpl>
            implements PDSAdapterConfigBuilder {
        private PDSAdapterDataConfigurator configurator = new PDSAdapterDataConfigurator();

        @Override
        protected PDSLicenseScanConfigImpl buildInitialConfig() {
            return new PDSLicenseScanConfigImpl();
        }

        @Override
        protected void customBuild(PDSLicenseScanConfigImpl config) {
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
