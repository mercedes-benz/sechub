// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.ArrayList;
import java.util.List;

/**
 * ExecutionProfileCreate is a model class for SecHubClient. It uses internally
 * the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate.<br>
 * <br>
 * The wrapper class itself was initial generated with
 * com.mercedesbenz.sechub.api.generator.PublicModelFileGenerator.
 */
public class ExecutionProfileCreate {
    // only for usage by SecHubClient
    static List<ExecutionProfileCreate> fromDelegates(List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate> delegates) {
        List<ExecutionProfileCreate> resultList = new ArrayList<>();
        if (delegates != null) {
            for (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate delegate : delegates) {
                resultList.add(new ExecutionProfileCreate(delegate));
            }
        }
        return resultList;
    }

    // only for usage by SecHubClient
    static List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate> toDelegates(List<ExecutionProfileCreate> wrappers) {
        List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate> resultList = new ArrayList<>();
        if (wrappers != null) {
            for (ExecutionProfileCreate wrapper : wrappers) {
                resultList.add(wrapper.getDelegate());
            }
        }
        return resultList;
    }

    private com.mercedesbenz.sechub.api.internal.model.InternalAccessExecutionProfileCreate internalAccess;

    public ExecutionProfileCreate() {
        this(null);
    }

    ExecutionProfileCreate(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate delegate) {
        this.internalAccess = new com.mercedesbenz.sechub.api.internal.model.InternalAccessExecutionProfileCreate(delegate);
    }

    // only for usage by SecHubClient
    com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate getDelegate() {
        return internalAccess.getDelegate();
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(internalAccess.getEnabled());
    }

    public String getDescription() {
        return internalAccess.getDescription();
    }

//    removed, because this is not a string but product executor configurations
//    public java.util.List<String> getConfigurations() {
//    return internalAccess.getConfigurations();
//
//    }

    public java.util.List<String> getProjectIds() {
        return internalAccess.getProjectIds();
    }

//    removed, because this is not a string but product executor configurations
//    public void setConfigurations(java.util.List<String> configurations) {
//        internalAccess.setConfigurations(configurations);
//    }

    public void setDescription(String description) {
        internalAccess.setDescription(description);
    }

    public void setEnabled(boolean enabled) {
        internalAccess.setEnabled(enabled);
    }

    public void setProjectIds(java.util.List<String> projectIds) {
        internalAccess.setProjectIds(projectIds);
    }

}
