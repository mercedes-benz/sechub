// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import com.mercedesbenz.sechub.adapter.AbstractInfraScanAdapterConfig;
import com.mercedesbenz.sechub.adapter.AbstractInfraScanAdapterConfigBuilder;

public class PDSInfraScanConfigImpl extends AbstractInfraScanAdapterConfig implements PDSInfraScanConfig {

    private PDSAdapterConfigData configData;

    private PDSInfraScanConfigImpl() {
    }

    @Override
    public PDSAdapterConfigData getPDSAdapterConfigData() {
        return configData;
    }

    public static PDSInfraScanConfigBuilder builder() {
        return new PDSInfraScanConfigBuilder();
    }

    public static class PDSInfraScanConfigBuilder extends AbstractInfraScanAdapterConfigBuilder<PDSInfraScanConfigBuilder, PDSInfraScanConfigImpl>
            implements PDSAdapterConfigBuilder {

        private PDSAdapterDataConfigurator configurator = new PDSAdapterDataConfigurator();

        @Override
        protected PDSInfraScanConfigImpl buildInitialConfig() {
            return new PDSInfraScanConfigImpl();
        }

        @Override
        public PDSAdapterConfigurator getPDSAdapterConfigurator() {
            return configurator;
        }

        @Override
        protected void customBuild(PDSInfraScanConfigImpl config) {
            /*
             * we must set the target type before calling configurator.configure() !
             * Otherwise job parameters not correct calculated
             */
            configurator.setTargetType(config.getTargetType());

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
