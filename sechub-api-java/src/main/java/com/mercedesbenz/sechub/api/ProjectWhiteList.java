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
public class ProjectWhiteList extends com.mercedesbenz.sechub.api.internal.model.AbstractProjectWhiteList {

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

    public ProjectWhiteList() {
        super();
    }

    public java.util.List<String> getUris() {
        return super.getUris();
    }

    public void setUris(java.util.List<String> uris) {
        super.setUris(uris);
    }

    // only for usage by SecHubClient
    ProjectWhiteList(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList delegate) {
        super(delegate);
    }

    // only for usage by SecHubClient
    com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList getDelegate() {
        return delegate;
    }

}
