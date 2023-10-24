// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.ArrayList;
import java.util.List;

/**
 * ExecutorConfigurationSetupCredentials is a model class for SecHubClient. It
 * uses internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials.<br>
 * <br>
 * The wrapper class itself was initial generated with
 * com.mercedesbenz.sechub.api.generator.PublicModelFileGenerator.
 */
public class ExecutorConfigurationSetupCredentials {
    // only for usage by SecHubClient
    static List<ExecutorConfigurationSetupCredentials> fromDelegates(
            List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials> delegates) {
        List<ExecutorConfigurationSetupCredentials> resultList = new ArrayList<>();
        if (delegates != null) {
            for (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials delegate : delegates) {
                resultList.add(new ExecutorConfigurationSetupCredentials(delegate));
            }
        }
        return resultList;
    }

    // only for usage by SecHubClient
    static List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials> toDelegates(
            List<ExecutorConfigurationSetupCredentials> wrappers) {
        List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials> resultList = new ArrayList<>();
        if (wrappers != null) {
            for (ExecutorConfigurationSetupCredentials wrapper : wrappers) {
                resultList.add(wrapper.getDelegate());
            }
        }
        return resultList;
    }

    private com.mercedesbenz.sechub.api.internal.model.InternalAccessExecutorConfigurationSetupCredentials internalAccess;

    public ExecutorConfigurationSetupCredentials() {
        this(null);
    }

    ExecutorConfigurationSetupCredentials(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials delegate) {
        this.internalAccess = new com.mercedesbenz.sechub.api.internal.model.InternalAccessExecutorConfigurationSetupCredentials(delegate);
    }

    // only for usage by SecHubClient
    com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials getDelegate() {
        return internalAccess.getDelegate();
    }

    public String getPassword() {
        return internalAccess.getPassword();
    }

    public String getUser() {
        return internalAccess.getUser();
    }

    public void setPassword(String password) {
        internalAccess.setPassword(password);
    }

    public void setUser(String user) {
        internalAccess.setUser(user);
    }

}
