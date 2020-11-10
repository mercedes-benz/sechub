// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.daimler.sechub.adapter.AbstractAdapterConfig;
import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;

public class PDSInfraScanConfigImpl extends AbstractAdapterConfig implements PDSInfraScanConfig{

    Map<String, String> jobParameters;
    UUID sechubJobUUID;
    String pdsProductIdentifier;
    
    private PDSInfraScanConfigImpl() {
    }

    public String getPdsProductIdentifier() {
        return pdsProductIdentifier;
    }
    
    public static PDSInfraScanConfigBuilder builder() {
        return new PDSInfraScanConfigBuilder();
    }

    public static class PDSInfraScanConfigBuilder extends AbstractAdapterConfigBuilder<PDSInfraScanConfigBuilder, PDSInfraScanConfigImpl> {

        private Map<String, String> jobParameters;
        private UUID sechubJobUUID;
        private String pdsProductIdentifier;
        
        public PDSInfraScanConfigBuilder setPDSProductIdentifier(String productIdentifier) {
            this.pdsProductIdentifier=productIdentifier;
            return this;
        }
        
        @Override
        protected void customBuild(PDSInfraScanConfigImpl config) {
            jobParameters.put(PDSAdapterConstants.PARAM_KEY_TARGET_TYPE, config.getTargetType());
            config.jobParameters=Collections.unmodifiableMap(jobParameters);
            config.sechubJobUUID=sechubJobUUID;
            config.pdsProductIdentifier=pdsProductIdentifier;
        }

        @Override
        protected PDSInfraScanConfigImpl buildInitialConfig() {
            return new PDSInfraScanConfigImpl();
        }

        public PDSInfraScanConfigBuilder setSecHubJobUUID(UUID sechubJobUUID) {
            this.sechubJobUUID=sechubJobUUID;
            return this;
        }
        
        /**
         * Set job parameters - mandatory
         *
         * @param  jobParameters a map with key values
         * @return builder
         */
        public final PDSInfraScanConfigBuilder setJobParameters(Map<String,String> jobParameters) {
            this.jobParameters = jobParameters;
            return this;
        }
        
        @Override
        protected void customValidate() {
            assertUserSet();
            assertPasswordSet();
            assertProjectIdSet();
            assertProductBaseURLSet();
            
            if (pdsProductIdentifier==null) {
                throw new IllegalStateException("pds product identifier not set!");
            }
            if (jobParameters==null) {
                throw new IllegalStateException("job parameters not set!");
            }
            if (sechubJobUUID==null) {
                throw new IllegalStateException("sechubJobUUID not set!");
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
