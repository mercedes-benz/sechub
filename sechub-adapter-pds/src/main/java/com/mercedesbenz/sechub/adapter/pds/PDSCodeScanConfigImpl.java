// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import com.mercedesbenz.sechub.adapter.AbstractCodeScanAdapterConfig;
import com.mercedesbenz.sechub.adapter.AbstractCodeScanAdapterConfigBuilder;

public class PDSCodeScanConfigImpl extends AbstractCodeScanAdapterConfig implements PDSCodeScanConfig {

    private PDSAdapterConfigData configData;

    private PDSCodeScanConfigImpl() {
    }

    @Override
    public PDSAdapterConfigData getPDSAdapterConfigData() {
        return configData;
    }

    public static PDSCodeScanConfigBuilder builder() {
        return new PDSCodeScanConfigBuilder();
    }

    public static class PDSCodeScanConfigBuilder extends AbstractCodeScanAdapterConfigBuilder<PDSCodeScanConfigBuilder, PDSCodeScanConfigImpl>
            implements PDSAdapterConfigBuilder {

        private PDSAdapterDataConfigurator configurator = new PDSAdapterDataConfigurator();

        @Override
        protected PDSCodeScanConfigImpl buildInitialConfig() {
            return new PDSCodeScanConfigImpl();
        }

        @Override
        public PDSAdapterConfigurator getPDSAdapterConfigurator() {
            return configurator;
        }

        @Override
        protected void customBuild(PDSCodeScanConfigImpl config) {
            configurator.configure();
            config.configData = configurator;
        }

        @Override
        protected void customValidate() {
            assertUserSet();
            assertPasswordSet();
            assertProjectIdSet();
            assertProductBaseURLSet();
        }

    }

}
