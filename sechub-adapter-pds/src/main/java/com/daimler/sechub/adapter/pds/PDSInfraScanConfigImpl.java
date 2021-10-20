// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.daimler.sechub.adapter.AbstractAdapterConfig;
import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.commons.model.SecHubConfigurationModel;
import com.daimler.sechub.commons.model.SecHubConfigurationModelReducedCloningSupport;
import com.daimler.sechub.commons.pds.PDSDefaultParameterKeyConstants;

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
        private SecHubConfigurationModel configurationModel;
        
        public PDSInfraScanConfigBuilder setPDSProductIdentifier(String productIdentifier) {
            this.pdsProductIdentifier=productIdentifier;
            return this;
        }
        
        @Override
        protected void customBuild(PDSInfraScanConfigImpl config) {
            if (configurationModel==null) {
                throw new IllegalStateException("configuration model not set!");
            }
            String reducedConfigJSON = SecHubConfigurationModelReducedCloningSupport.DEFAULT
                    .createReducedScanConfigurationCloneJSON(configurationModel, ScanType.INFRA_SCAN);
            
            jobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_TARGET_TYPE, config.getTargetType());
            jobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_TARGET_TYPE, config.getTargetType());
            jobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_SCAN_CONFIGURATION, reducedConfigJSON);

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
        
        public PDSInfraScanConfigBuilder setSecHubConfigModel(SecHubConfigurationModel model) {
            this.configurationModel = model;
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
