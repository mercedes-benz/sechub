// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import com.mercedesbenz.sechub.adapter.AbstractAnalyticsAdapterConfig;
import com.mercedesbenz.sechub.adapter.AbstractAnalyticsAdapterConfigBuilder;

public class PDSAnalyticsConfigImpl extends AbstractAnalyticsAdapterConfig implements PDSAnalyticsConfig {

    private PDSAdapterConfigData configData;

    private PDSAnalyticsConfigImpl() {
    }

    @Override
    public PDSAdapterConfigData getPDSAdapterConfigData() {
        return configData;
    }

    public static PDSAnalyzerConfigBuilder builder() {
        return new PDSAnalyzerConfigBuilder();
    }

    public static class PDSAnalyzerConfigBuilder extends AbstractAnalyticsAdapterConfigBuilder<PDSAnalyzerConfigBuilder, PDSAnalyticsConfigImpl>
            implements PDSAdapterConfigBuilder {

        private PDSAdapterDataConfigurator configurator = new PDSAdapterDataConfigurator();

        @Override
        protected PDSAnalyticsConfigImpl buildInitialConfig() {
            return new PDSAnalyticsConfigImpl();
        }

        @Override
        public PDSAdapterConfigurator getPDSAdapterConfigurator() {
            return configurator;
        }

        @Override
        protected void customBuild(PDSAnalyticsConfigImpl config) {
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
