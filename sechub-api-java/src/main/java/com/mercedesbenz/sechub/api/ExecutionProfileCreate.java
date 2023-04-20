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
public class ExecutionProfileCreate extends com.mercedesbenz.sechub.api.internal.model.AbstractExecutionProfileCreate {

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

    public ExecutionProfileCreate() {
        super();
    }

    public Boolean getEnabled() {
        return super.getEnabled();
    }

    public String getDescription() {
        return super.getDescription();
    }

//    removed, because this is not a string but product executor configurations
//    public java.util.List<String> getConfigurations() {
//        return super.getConfigurations();
//    }

    public java.util.List<String> getProjectIds() {
        return super.getProjectIds();
    }

//  removed, because this is not a string but product executor configurations
//    public void setConfigurations(java.util.List<String> configurations) {
//        super.setConfigurations(configurations);
//    }

    public void setDescription(String description) {
        super.setDescription(description);
    }

    public void setEnabled(Boolean enabled) {
        super.setEnabled(enabled);
    }

    public void setProjectIds(java.util.List<String> projectIds) {
        super.setProjectIds(projectIds);
    }

    // only for usage by SecHubClient
    ExecutionProfileCreate(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate delegate) {
        super(delegate);
    }

    // only for usage by SecHubClient
    com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate getDelegate() {
        return delegate;
    }

}
