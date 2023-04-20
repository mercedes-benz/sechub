// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.ArrayList;
import java.util.List;

/**
 * ExecutorConfiguration is a model class for SecHubClient. It uses internally
 * the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration.<br>
 * <br>
 * The wrapper class itself was initial generated with
 * com.mercedesbenz.sechub.api.generator.PublicModelFileGenerator.
 */
public class ExecutorConfiguration extends com.mercedesbenz.sechub.api.internal.model.AbstractExecutorConfiguration {

    // only for usage by SecHubClient
    static List<ExecutorConfiguration> fromDelegates(List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration> delegates) {
        List<ExecutorConfiguration> resultList = new ArrayList<>();
        if (delegates != null) {
            for (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration delegate : delegates) {
                resultList.add(new ExecutorConfiguration(delegate));
            }
        }
        return resultList;
    }

    // only for usage by SecHubClient
    static List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration> toDelegates(List<ExecutorConfiguration> wrappers) {
        List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration> resultList = new ArrayList<>();
        if (wrappers != null) {
            for (ExecutorConfiguration wrapper : wrappers) {
                resultList.add(wrapper.getDelegate());
            }
        }
        return resultList;
    }

    private ExecutorConfigurationSetup setup;

    public ExecutorConfiguration() {
        super();
    }

    public Boolean getEnabled() {
        return super.getEnabled();
    }

    public String getName() {
        return super.getName();
    }

    public String getProductIdentifier() {
        return super.getProductIdentifier();
    }

    public java.math.BigDecimal getExecutorVersion() {
        return super.getExecutorVersion();
    }

    public void setEnabled(Boolean enabled) {
        super.setEnabled(enabled);
    }

    public void setExecutorVersion(java.math.BigDecimal executorVersion) {
        super.setExecutorVersion(executorVersion);
    }

    public void setName(String name) {
        super.setName(name);
    }

    public void setProductIdentifier(String productIdentifier) {
        super.setProductIdentifier(productIdentifier);
    }

    public ExecutorConfigurationSetup getSetup() {
        if (setup == null) {
            setup = new ExecutorConfigurationSetup(delegate.getSetup());
            delegate.setSetup(setup.getDelegate()); // necessary if delegate had no content, but wrapper created one
        }
        return setup;
    }

    public void setSetup(ExecutorConfigurationSetup setup) {
        this.setup = setup;
        this.delegate.setSetup(setup.getDelegate());
    }

    // only for usage by SecHubClient
    ExecutorConfiguration(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration delegate) {
        super(delegate);
    }

    // only for usage by SecHubClient
    com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration getDelegate() {
        return delegate;
    }

}
