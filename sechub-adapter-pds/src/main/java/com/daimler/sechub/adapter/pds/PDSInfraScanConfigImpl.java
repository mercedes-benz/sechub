// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import com.daimler.sechub.adapter.AbstractAdapterConfig;
import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;

public class PDSInfraScanConfigImpl extends AbstractAdapterConfig implements PDSInfraScanConfig{

    Map<String, String> jobParameters;
    
    private PDSInfraScanConfigImpl() {
    }

    public static PDSCodeScanConfigBuilder builder() {
        return new PDSCodeScanConfigBuilder();
    }

    public static class PDSCodeScanConfigBuilder extends AbstractAdapterConfigBuilder<PDSCodeScanConfigBuilder, PDSInfraScanConfigImpl> {

        private Map<String, String> jobParameters;
        
        public PDSCodeScanConfigBuilder setSourceCodeZipFileInputStream(InputStream sourceCodeZipFileInputStream) {
            return this;
        }

        @Override
        protected void customBuild(PDSInfraScanConfigImpl config) {
            config.jobParameters=Collections.unmodifiableMap(jobParameters);
        }

        @Override
        protected PDSInfraScanConfigImpl buildInitialConfig() {
            return new PDSInfraScanConfigImpl();
        }

        /**
         * Set job parameters - mandatory
         *
         * @param  jobParameters a map with key values
         * @return builder
         */
        public final PDSCodeScanConfigBuilder setJobParameters(Map<String,String> jobParameters) {
            this.jobParameters = jobParameters;
            return this;
        }
        
        @Override
        protected void customValidate() {
            assertUserSet();
            assertPasswordSet();
            assertProjectIdSet();
            
            if (jobParameters==null) {
                throw new IllegalStateException("job parameters not set!");
            }
        }

    }
    
    @Override
    public Map<String, String> getJobParameters() {
        return jobParameters;
    }
}
