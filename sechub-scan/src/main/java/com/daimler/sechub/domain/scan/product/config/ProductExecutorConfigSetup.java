// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.commons.model.JSONable;

public class ProductExecutorConfigSetup implements JSONable<ProductExecutorConfigSetup> {
    
    private static final ProductExecutorConfigSetup IMPORTER = new ProductExecutorConfigSetup();
    
    public static final String PROPERTY_BASEURL = "baseURL";
    public static final String PROPERTY_JOBPARAMETERS = "jobParameters";
    public static final String PROPERTY_CREDENTIALS= "credentials";
    
    private String baseURL;

    private ProductExecutorConfigSetupCredentials credentials = new ProductExecutorConfigSetupCredentials();

    private List<ProductExecutorConfigSetupJobParameter> jobParameters = new ArrayList<>();

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public ProductExecutorConfigSetupCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(ProductExecutorConfigSetupCredentials credentials) {
        this.credentials = credentials;
    }

    
    public List<ProductExecutorConfigSetupJobParameter> getJobParameters() {
        return jobParameters;
    }
    
    public void setJobParameters(List<ProductExecutorConfigSetupJobParameter> jobParameters) {
        this.jobParameters = jobParameters;
    }

    @Override
    public Class<ProductExecutorConfigSetup> getJSONTargetClass() {
        return ProductExecutorConfigSetup.class;
    }
    
    public static ProductExecutorConfigSetup fromJSONString(String json) {
        return IMPORTER.fromJSON(json);
    }

}
