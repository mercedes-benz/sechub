// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import com.daimler.sechub.adapter.AbstractWebScanAdapterConfig;
import com.daimler.sechub.adapter.AbstractWebScanAdapterConfigBuilder;

public class PDSWebScanConfigImpl extends AbstractWebScanAdapterConfig implements PDSWebScanConfig{

    private String websiteName;

    Map<String, String> jobParameters;

    public String getWebsiteName() {
        return websiteName;
    }
    
    @Override
    public Map<String, String> getJobParameters() {
        return jobParameters;
    }

    private PDSWebScanConfigImpl() {
    }

    public static PDSWebScanConfigBuilder builder() {
        return new PDSWebScanConfigBuilder();
    }


    public static class PDSWebScanConfigBuilder
            extends AbstractWebScanAdapterConfigBuilder<PDSWebScanConfigBuilder, PDSWebScanConfigImpl> {

        private String targetType;
        private Map<String, String> jobParameters;

        private PDSWebScanConfigBuilder() {
        }

        public PDSWebScanConfigBuilder setWebScanTargetType(String targetType) {
            this.targetType = targetType;
            return this;
        }
        
        /**
         * Set job parameters - mandatory
         *
         * @param  jobParameters a map with key values
         * @return builder
         */
        public final PDSWebScanConfigBuilder setJobParameters(Map<String,String> jobParameters) {
            this.jobParameters = jobParameters;
            return this;
        }

        @Override
        protected void customBuild(PDSWebScanConfigImpl config) {
            
            config.jobParameters=Collections.unmodifiableMap(jobParameters);
            int size = config.getRootTargetURIs().size();
            if (size!=1) {
                /* netsparker needs ONE root uri */
                throw new IllegalStateException("netsparker must have ONE unique root target uri and not many!");
            }
            String websiteURLAsString = config.getRootTargetURIasString();
            if (websiteURLAsString==null) {
                throw new IllegalStateException("website url (root target url ) may not be null at this point!");
            }
            try {
                URL url = new URL(websiteURLAsString);
                StringBuilder sb = new StringBuilder();
                sb.append(url.getHost());
                sb.append("_");
                int port = url.getPort();
                if (port<1) {
                    sb.append("default");
                }else {
                    sb.append(port);
                }

                config.websiteName= sb.toString().toLowerCase();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("website root url '"+websiteURLAsString+"' is not a valid URL!",e);
            }
        }

        @Override
        protected void customValidate() {
            assertUserSet();
            assertPasswordSet();
            assertProductBaseURLSet();
            
            if (jobParameters==null) {
                throw new IllegalStateException("job parameters not set!");
            }
        }

        @Override
        protected PDSWebScanConfigImpl buildInitialConfig() {
            return new PDSWebScanConfigImpl();
        }

    }

}
