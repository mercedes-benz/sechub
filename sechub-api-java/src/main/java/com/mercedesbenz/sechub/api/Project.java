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
public class Project {
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

    private com.mercedesbenz.sechub.api.internal.model.InternalAccessProject internalAccess;
    private ProjectMetaData metaData;
    private ProjectWhiteList whiteList;

    public Project() {
        this(null);
    }

    Project(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject delegate) {
        this.internalAccess = new com.mercedesbenz.sechub.api.internal.model.InternalAccessProject(delegate);
    }

    // only for usage by SecHubClient
    com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject getDelegate() {
        return internalAccess.getDelegate();
    }

    public String getApiVersion() {
        return internalAccess.getApiVersion();
    }

    public String getDescription() {
        return internalAccess.getDescription();
    }

    public String getName() {
        return internalAccess.getName();
    }

    public String getOwner() {
        return internalAccess.getOwner();
    }

    public void setApiVersion(String apiVersion) {
        internalAccess.setApiVersion(apiVersion);
    }

    public void setDescription(String description) {
        internalAccess.setDescription(description);
    }

    public void setName(String name) {
        internalAccess.setName(name);
    }

    public void setOwner(String owner) {
        internalAccess.setOwner(owner);
    }

    public ProjectMetaData getMetaData() {
        if (metaData == null) {
            metaData = new ProjectMetaData(internalAccess.getDelegate().getMetaData());
            internalAccess.getDelegate().setMetaData(metaData.getDelegate()); // necessary if delegate had no content, but wrapper created one
        }
        return metaData;
    }

    public void setMetaData(ProjectMetaData metaData) {
        this.metaData = metaData;
        this.internalAccess.getDelegate().setMetaData(metaData.getDelegate());
    }

    public ProjectWhiteList getWhiteList() {
        if (whiteList == null) {
            whiteList = new ProjectWhiteList(internalAccess.getDelegate().getWhiteList());
            internalAccess.getDelegate().setWhiteList(whiteList.getDelegate()); // necessary if delegate had no content, but wrapper created one
        }
        return whiteList;
    }

    public void setWhiteList(ProjectWhiteList whiteList) {
        this.whiteList = whiteList;
        this.internalAccess.getDelegate().setWhiteList(whiteList.getDelegate());
    }

}
