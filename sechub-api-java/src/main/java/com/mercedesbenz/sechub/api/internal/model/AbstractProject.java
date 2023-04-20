// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * AbstractProject is a model class for SecHubClient. It uses internally the
 * generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject.<br>
 * <br>
 * The abstract wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.AbstractModelFileGenerator and is not
 * intended to be changed manually!
 */
public abstract class AbstractProject {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject delegate;

    protected AbstractProject() {
        this(null);
    }

    protected AbstractProject(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject delegate) {
        if (delegate == null) {
            this.delegate = new com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject();
            initDelegateWithDefaults();
        } else {
            this.delegate = delegate;
        }
    }

    protected void initDelegateWithDefaults() {
        /* child classes can override this */
    }

    protected String getApiVersion() {
        return delegate.getApiVersion();
    }

    protected String getDescription() {
        return delegate.getDescription();
    }

    protected String getName() {
        return delegate.getName();
    }

    protected String getOwner() {
        return delegate.getOwner();
    }

    protected void setApiVersion(String apiVersion) {
        delegate.setApiVersion(apiVersion);
    }

    protected void setDescription(String description) {
        delegate.setDescription(description);
    }

    protected void setName(String name) {
        delegate.setName(name);
    }

    protected void setOwner(String owner) {
        delegate.setOwner(owner);
    }

    public boolean equals(Object object) {
        if (object instanceof AbstractProject) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
