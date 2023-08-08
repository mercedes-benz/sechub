// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * InternalAccessProject is a model class for SecHubClient. It uses internally
 * the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject.<br>
 * <br>
 * The internal access wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.InternalAccessModelFileGenerator and is
 * not intended to be changed manually!
 */
public class InternalAccessProject {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject delegate;

    protected InternalAccessProject() {
        this(null);
    }

    public InternalAccessProject(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject delegate) {
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

    public String getApiVersion() {
        return delegate.getApiVersion();
    }

    public String getDescription() {
        return delegate.getDescription();
    }

    public String getName() {
        return delegate.getName();
    }

    public String getOwner() {
        return delegate.getOwner();
    }

    public void setApiVersion(String apiVersion) {
        delegate.setApiVersion(apiVersion);
    }

    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public void setOwner(String owner) {
        delegate.setOwner(owner);
    }

    public com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject getDelegate() {
        return delegate;
    }

    public boolean equals(Object object) {
        if (object instanceof InternalAccessProject) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProject) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
