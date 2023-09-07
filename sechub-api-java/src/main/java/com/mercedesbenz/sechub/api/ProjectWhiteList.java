// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.ArrayList;
import java.util.List;

/**
 * ProjectWhiteList is a model class for SecHubClient. It uses internally the
 * generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList.<br>
 * <br>
 * The wrapper class itself was initial generated with
 * com.mercedesbenz.sechub.api.generator.PublicModelFileGenerator.
 */
public class ProjectWhiteList {
    // only for usage by SecHubClient
    static List<ProjectWhiteList> fromDelegates(List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList> delegates) {
        List<ProjectWhiteList> resultList = new ArrayList<>();
        if (delegates != null) {
            for (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList delegate : delegates) {
                resultList.add(new ProjectWhiteList(delegate));
            }
        }
        return resultList;
    }

    // only for usage by SecHubClient
    static List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList> toDelegates(List<ProjectWhiteList> wrappers) {
        List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList> resultList = new ArrayList<>();
        if (wrappers != null) {
            for (ProjectWhiteList wrapper : wrappers) {
                resultList.add(wrapper.getDelegate());
            }
        }
        return resultList;
    }

    private com.mercedesbenz.sechub.api.internal.model.InternalAccessProjectWhiteList internalAccess;

    public ProjectWhiteList() {
        this(null);
    }

    ProjectWhiteList(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList delegate) {
        this.internalAccess = new com.mercedesbenz.sechub.api.internal.model.InternalAccessProjectWhiteList(delegate);
    }

    // only for usage by SecHubClient
    com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList getDelegate() {
        return internalAccess.getDelegate();
    }

    public java.util.List<String> getUris() {
        return internalAccess.getUris();
    }

    public void setUris(java.util.List<String> uris) {
        internalAccess.setUris(uris);
    }

}
