// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import com.mercedesbenz.sechub.adapter.AbstractPrepareAdapterConfig;
import com.mercedesbenz.sechub.adapter.AbstractPrepareAdapterConfigBuilder;

public class PDSPrepareConfigImpl extends AbstractPrepareAdapterConfig implements PDSPrepareConfig {

    private PDSAdapterConfigData configData;

    private PDSPrepareConfigImpl() {
    }

    @Override
    public PDSAdapterConfigData getPDSAdapterConfigData() {
        return configData;
    }

    public static PDSPrepareConfigBuilder builder() {
        return new PDSPrepareConfigBuilder();
    }

    public static class PDSPrepareConfigBuilder extends AbstractPrepareAdapterConfigBuilder<PDSPrepareConfigBuilder, PDSPrepareConfigImpl>
            implements PDSAdapterConfigBuilder {

        private PDSAdapterDataConfigurator configurator = new PDSAdapterDataConfigurator();

        @Override
        protected PDSPrepareConfigImpl buildInitialConfig() {
            return new PDSPrepareConfigImpl();
        }

        @Override
        public PDSAdapterConfigurator getPDSAdapterConfigurator() {
            return configurator;
        }

        @Override
        protected void customBuild(PDSPrepareConfigImpl config) {
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
