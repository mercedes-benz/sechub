// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Project is a model class for SecHubClient. It uses internally the generated
 * class com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject.<br>
 * <br>
 * The wrapper class itself was initial generated with
 * com.mercedesbenz.sechub.api.generator.PublicModelFileGenerator.
 */
public class Project extends com.mercedesbenz.sechub.api.internal.model.AbstractProject {

    // only for usage by SecHubClient
    static List<Project> fromDelegates(List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject> delegates) {
        List<Project> resultList = new ArrayList<>();
        if (delegates != null) {
            for (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject delegate : delegates) {
                resultList.add(new Project(delegate));
            }
        }
        return resultList;
    }

    // only for usage by SecHubClient
    static List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject> toDelegates(List<Project> wrappers) {
        List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject> resultList = new ArrayList<>();
        if (wrappers != null) {
            for (Project wrapper : wrappers) {
                resultList.add(wrapper.getDelegate());
            }
        }
        return resultList;
    }

    private ProjectMetaData metaData;
    private ProjectWhiteList whiteList;

    public Project() {
        super();
    }

    public String getApiVersion() {
        return super.getApiVersion();
    }

    public String getDescription() {
        return super.getDescription();
    }

    public String getName() {
        return super.getName();
    }

    public String getOwner() {
        return super.getOwner();
    }

    public void setApiVersion(String apiVersion) {
        super.setApiVersion(apiVersion);
    }

    public void setDescription(String description) {
        super.setDescription(description);
    }

    public void setName(String name) {
        super.setName(name);
    }

    public void setOwner(String owner) {
        super.setOwner(owner);
    }

    public ProjectMetaData getMetaData() {
        if (metaData == null) {
            metaData = new ProjectMetaData(delegate.getMetaData());
            delegate.setMetaData(metaData.getDelegate()); // necessary if delegate had no content, but wrapper created one
        }
        return metaData;
    }

    public void setMetaData(ProjectMetaData metaData) {
        this.metaData = metaData;
        this.delegate.setMetaData(metaData.getDelegate());
    }

    public ProjectWhiteList getWhiteList() {
        if (whiteList == null) {
            whiteList = new ProjectWhiteList(delegate.getWhiteList());
            delegate.setWhiteList(whiteList.getDelegate()); // necessary if delegate had no content, but wrapper created one
        }
        return whiteList;
    }

    public void setWhiteList(ProjectWhiteList whiteList) {
        this.whiteList = whiteList;
        this.delegate.setWhiteList(whiteList.getDelegate());
    }

    // only for usage by SecHubClient
    Project(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject delegate) {
        super(delegate);
    }

    // only for usage by SecHubClient
    com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject getDelegate() {
        return delegate;
    }

}
