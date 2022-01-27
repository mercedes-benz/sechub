// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.nessus;

import com.daimler.sechub.adapter.AbstractInfraScanAdapterConfig;
import com.daimler.sechub.adapter.AbstractInfraScanAdapterConfigBuilder;

public class NessusConfig extends AbstractInfraScanAdapterConfig implements NessusAdapterConfig {

    private NessusConfig() {
    }

    public static NessusConfigBuilder builder() {
        return new NessusConfigBuilder();
    }
    
    public static class NessusConfigBuilder extends AbstractInfraScanAdapterConfigBuilder<NessusConfigBuilder, NessusAdapterConfig> {

        @Override
        protected void customBuild(NessusAdapterConfig config) {

        }

        @Override
        protected NessusAdapterConfig buildInitialConfig() {
            return new NessusConfig();
        }

        @Override
        protected void customValidate() {
            assertUserSet();
            assertPasswordSet();

        }

    }

}
