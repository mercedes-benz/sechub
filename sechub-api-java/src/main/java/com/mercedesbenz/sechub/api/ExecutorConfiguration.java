// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.math.BigDecimal;
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
public class ExecutorConfiguration {
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

    private com.mercedesbenz.sechub.api.internal.model.InternalAccessExecutorConfiguration internalAccess;
    private ExecutorConfigurationSetup setup;

    public ExecutorConfiguration() {
        this(null);
    }

    ExecutorConfiguration(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration delegate) {
        this.internalAccess = new com.mercedesbenz.sechub.api.internal.model.InternalAccessExecutorConfiguration(delegate);
    }

    // only for usage by SecHubClient
    com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration getDelegate() {
        return internalAccess.getDelegate();
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(internalAccess.getEnabled());
    }

    public String getName() {
        return internalAccess.getName();
    }

    public String getProductIdentifier() {
        return internalAccess.getProductIdentifier();
    }

    public int getExecutorVersion() {
        BigDecimal version = internalAccess.getExecutorVersion();
        if (version == null) {
            return 0;
        }
        return version.intValue();
    }

    public void setEnabled(Boolean enabled) {
        internalAccess.setEnabled(enabled);
    }

    public void setExecutorVersion(int executorVersion) {
        internalAccess.setExecutorVersion(BigDecimal.valueOf(executorVersion));
    }

    public void setName(String name) {
        internalAccess.setName(name);
    }

    public void setProductIdentifier(String productIdentifier) {
        internalAccess.setProductIdentifier(productIdentifier);
    }

    public ExecutorConfigurationSetup getSetup() {
        if (setup == null) {
            setup = new ExecutorConfigurationSetup(internalAccess.getDelegate().getSetup());
            internalAccess.getDelegate().setSetup(setup.getDelegate()); // necessary if delegate had no content, but wrapper created one
        }
        return setup;
    }

    public void setSetup(ExecutorConfigurationSetup setup) {
        this.setup = setup;
        this.internalAccess.getDelegate().setSetup(setup.getDelegate());
    }

}
