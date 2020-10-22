// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import com.daimler.sechub.adapter.AbstractCodeScanAdapterConfig;
import com.daimler.sechub.adapter.AbstractCodeScanAdapterConfigBuilder;

public class PDSCodeScanConfigImpl extends AbstractCodeScanAdapterConfig implements PDSCodeScanConfig{

    private InputStream sourceCodeZipFileInputStream;
    
    Map<String, String> jobParameters;
    
    private PDSCodeScanConfigImpl() {
    }


    @Override
    public InputStream getSourceCodeZipFileInputStream() {
        return sourceCodeZipFileInputStream;
    }

    public static PDSCodeScanConfigBuilder builder() {
        return new PDSCodeScanConfigBuilder();
    }

    public static class PDSCodeScanConfigBuilder extends AbstractCodeScanAdapterConfigBuilder<PDSCodeScanConfigBuilder, PDSCodeScanConfigImpl> {

        private InputStream sourceCodeZipFileInputStream;
        private Map<String, String> jobParameters;
        
        public PDSCodeScanConfigBuilder setSourceCodeZipFileInputStream(InputStream sourceCodeZipFileInputStream) {
            this.sourceCodeZipFileInputStream = sourceCodeZipFileInputStream;
            return this;
        }

        @Override
        protected void customBuild(PDSCodeScanConfigImpl config) {
            config.sourceCodeZipFileInputStream = sourceCodeZipFileInputStream;
            config.jobParameters=Collections.unmodifiableMap(jobParameters);
        }

        @Override
        protected PDSCodeScanConfigImpl buildInitialConfig() {
            return new PDSCodeScanConfigImpl();
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
