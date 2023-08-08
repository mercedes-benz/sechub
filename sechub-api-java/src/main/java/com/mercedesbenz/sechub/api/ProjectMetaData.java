// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.ArrayList;
import java.util.List;

/**
 * ProjectMetaData is a model class for SecHubClient. It uses internally the
 * generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData.<br>
 * <br>
 * The wrapper class itself was initial generated with
 * com.mercedesbenz.sechub.api.generator.PublicModelFileGenerator.
 */
public class ProjectMetaData {
    // only for usage by SecHubClient
    static List<ProjectMetaData> fromDelegates(List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData> delegates) {
        List<ProjectMetaData> resultList = new ArrayList<>();
        if (delegates != null) {
            for (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData delegate : delegates) {
                resultList.add(new ProjectMetaData(delegate));
            }
        }
        return resultList;
    }

    // only for usage by SecHubClient
    static List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData> toDelegates(List<ProjectMetaData> wrappers) {
        List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData> resultList = new ArrayList<>();
        if (wrappers != null) {
            for (ProjectMetaData wrapper : wrappers) {
                resultList.add(wrapper.getDelegate());
            }
        }
        return resultList;
    }

    private com.mercedesbenz.sechub.api.internal.model.InternalAccessProjectMetaData internalAccess;

    public ProjectMetaData() {
        this(null);
    }

    ProjectMetaData(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData delegate) {
        this.internalAccess = new com.mercedesbenz.sechub.api.internal.model.InternalAccessProjectMetaData(delegate);
    }

    // only for usage by SecHubClient
    com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData getDelegate() {
        return internalAccess.getDelegate();
    }

}
