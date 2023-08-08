// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.ArrayList;
import java.util.List;

/**
 * ExecutorConfigurationSetupJobParameter is a model class for SecHubClient. It
 * uses internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner.<br>
 * <br>
 * The wrapper class itself was initial generated with
 * com.mercedesbenz.sechub.api.generator.PublicModelFileGenerator.
 */
public class ExecutorConfigurationSetupJobParameter {
    // only for usage by SecHubClient
    static List<ExecutorConfigurationSetupJobParameter> fromDelegates(
            List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner> delegates) {
        List<ExecutorConfigurationSetupJobParameter> resultList = new ArrayList<>();
        if (delegates != null) {
            for (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner delegate : delegates) {
                resultList.add(new ExecutorConfigurationSetupJobParameter(delegate));
            }
        }
        return resultList;
    }

    // only for usage by SecHubClient
    static List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner> toDelegates(
            List<ExecutorConfigurationSetupJobParameter> wrappers) {
        List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner> resultList = new ArrayList<>();
        if (wrappers != null) {
            for (ExecutorConfigurationSetupJobParameter wrapper : wrappers) {
                resultList.add(wrapper.getDelegate());
            }
        }
        return resultList;
    }

    private com.mercedesbenz.sechub.api.internal.model.InternalAccessExecutorConfigurationSetupJobParameter internalAccess;

    public ExecutorConfigurationSetupJobParameter() {
        this(null);
    }

    ExecutorConfigurationSetupJobParameter(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner delegate) {
        this.internalAccess = new com.mercedesbenz.sechub.api.internal.model.InternalAccessExecutorConfigurationSetupJobParameter(delegate);
    }

    // only for usage by SecHubClient
    com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner getDelegate() {
        return internalAccess.getDelegate();
    }

    public String getKey() {
        return internalAccess.getKey();
    }

    public String getValue() {
        return internalAccess.getValue();
    }

    public void setKey(String key) {
        internalAccess.setKey(key);
    }

    public void setValue(String value) {
        internalAccess.setValue(value);
    }

}
