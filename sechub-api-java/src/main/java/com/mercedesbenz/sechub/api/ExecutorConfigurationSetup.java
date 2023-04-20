// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.ArrayList;
import java.util.List;

/**
 * ExecutorConfigurationSetup is a model class for SecHubClient. It uses
 * internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup.<br>
 * <br>
 * The wrapper class itself was initial generated with
 * com.mercedesbenz.sechub.api.generator.PublicModelFileGenerator.
 */
public class ExecutorConfigurationSetup extends com.mercedesbenz.sechub.api.internal.model.AbstractExecutorConfigurationSetup {

    // only for usage by SecHubClient
    static List<ExecutorConfigurationSetup> fromDelegates(List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup> delegates) {
        List<ExecutorConfigurationSetup> resultList = new ArrayList<>();
        if (delegates != null) {
            for (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup delegate : delegates) {
                resultList.add(new ExecutorConfigurationSetup(delegate));
            }
        }
        return resultList;
    }

    // only for usage by SecHubClient
    static List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup> toDelegates(List<ExecutorConfigurationSetup> wrappers) {
        List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup> resultList = new ArrayList<>();
        if (wrappers != null) {
            for (ExecutorConfigurationSetup wrapper : wrappers) {
                resultList.add(wrapper.getDelegate());
            }
        }
        return resultList;
    }

    private ExecutorConfigurationSetupCredentials credentials;
    private List<ExecutorConfigurationSetupJobParameter> jobParameters;

    public ExecutorConfigurationSetup() {
        super();
    }

    public String getBaseURL() {
        return super.getBaseURL();
    }

    public void setBaseURL(String baseURL) {
        super.setBaseURL(baseURL);
    }

    public ExecutorConfigurationSetupCredentials getCredentials() {
        if (credentials == null) {
            credentials = new ExecutorConfigurationSetupCredentials(delegate.getCredentials());
            delegate.setCredentials(credentials.getDelegate()); // necessary if delegate had no content, but wrapper created one
        }
        return credentials;
    }

    public void setCredentials(ExecutorConfigurationSetupCredentials credentials) {
        this.credentials = credentials;
        this.delegate.setCredentials(credentials.getDelegate());
    }

    public List<ExecutorConfigurationSetupJobParameter> getJobParameters() {
        if (jobParameters == null) {
            jobParameters = ExecutorConfigurationSetupJobParameter.fromDelegates(delegate.getJobParameters());
        }
        return jobParameters;
    }

    public void setJobParameters(List<ExecutorConfigurationSetupJobParameter> jobParameters) {
        this.jobParameters = jobParameters;
        this.delegate.setJobParameters(ExecutorConfigurationSetupJobParameter.toDelegates(jobParameters));
    }

    // only for usage by SecHubClient
    ExecutorConfigurationSetup(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup delegate) {
        super(delegate);
    }

    // only for usage by SecHubClient
    com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup getDelegate() {
        return delegate;
    }

}
