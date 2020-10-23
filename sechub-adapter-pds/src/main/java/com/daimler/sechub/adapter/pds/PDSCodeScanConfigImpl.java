// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.daimler.sechub.adapter.AbstractCodeScanAdapterConfig;
import com.daimler.sechub.adapter.AbstractCodeScanAdapterConfigBuilder;

public class PDSCodeScanConfigImpl extends AbstractCodeScanAdapterConfig implements PDSCodeScanConfig{

    private InputStream sourceCodeZipFileInputStream;
    private String sourceZipFileChecksum;
    
    private Map<String, String> jobParameters;

    private UUID sechubJobUUID;
    private String pdsProductIdentifier;

    
    private PDSCodeScanConfigImpl() {
    }
    
    public String getPdsProductIdentifier() {
        return pdsProductIdentifier;
    }

    @Override
    public InputStream getSourceCodeZipFileInputStream() {
        return sourceCodeZipFileInputStream;
    }
    
    @Override
    public String getSourceCodeZipFileChecksum() {
        return sourceZipFileChecksum;
    }

    public static PDSCodeScanConfigBuilder builder() {
        return new PDSCodeScanConfigBuilder();
    }

    public static class PDSCodeScanConfigBuilder extends AbstractCodeScanAdapterConfigBuilder<PDSCodeScanConfigBuilder, PDSCodeScanConfigImpl> {

        private InputStream sourceCodeZipFileInputStream;
        private Map<String, String> jobParameters;
        private UUID sechubJobUUID;
        private String sourceZipFileChecksum;
        private String pdsProductIdentifier;
        
        public PDSCodeScanConfigBuilder setSourceCodeZipFileInputStream(InputStream sourceCodeZipFileInputStream) {
            this.sourceCodeZipFileInputStream = sourceCodeZipFileInputStream;
            return this;
        }
        
        public PDSCodeScanConfigBuilder setSecHubJobUUID(UUID sechubJobUUID) {
            this.sechubJobUUID=sechubJobUUID;
            return this;
        }
        
        public PDSCodeScanConfigBuilder setSourceZipFileChecksum(String sourceZipFileChecksum) {
            this.sourceZipFileChecksum = sourceZipFileChecksum;
            return this;
        }
        
        public PDSCodeScanConfigBuilder setPDSProductIdentifier(String productIdentifier) {
            this.pdsProductIdentifier=productIdentifier;
            return this;
        }

        

        @Override
        protected void customBuild(PDSCodeScanConfigImpl config) {
            config.sourceCodeZipFileInputStream = sourceCodeZipFileInputStream;
            config.sourceZipFileChecksum=sourceZipFileChecksum;
            
            jobParameters.put(PDSAdapterConstants.PARAM_KEY_TARGET_TYPE, config.getTargetType());
            config.jobParameters=Collections.unmodifiableMap(jobParameters);
            config.sechubJobUUID=sechubJobUUID;
            config.pdsProductIdentifier=pdsProductIdentifier;
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
            if (sourceCodeZipFileInputStream!= null && sourceZipFileChecksum==null) {
                throw new IllegalStateException("sourceZipFileChecksum not set but zipfile inputstream!");
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
