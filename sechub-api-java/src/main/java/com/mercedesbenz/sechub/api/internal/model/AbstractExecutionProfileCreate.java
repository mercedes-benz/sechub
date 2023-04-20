// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

import java.util.ArrayList;

/**
 * AbstractExecutionProfileCreate is a model class for SecHubClient. It uses
 * internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate.<br>
 * <br>
 * The abstract wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.AbstractModelFileGenerator and is not
 * intended to be changed manually!
 */
public abstract class AbstractExecutionProfileCreate {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate delegate;

    protected AbstractExecutionProfileCreate() {
        this(null);
    }

    protected AbstractExecutionProfileCreate(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate delegate) {
        if (delegate == null) {
            this.delegate = new com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate();
            initDelegateWithDefaults();
        } else {
            this.delegate = delegate;
        }
    }

    protected void initDelegateWithDefaults() {
        /* child classes can override this */
    }

    protected Boolean getEnabled() {
        return delegate.getEnabled();
    }

    protected String getDescription() {
        return delegate.getDescription();
    }

    protected java.util.List<String> getConfigurations() {
        if (delegate.getConfigurations() == null) {
            setConfigurations(new ArrayList<>());
        }
        return delegate.getConfigurations();
    }

    protected java.util.List<String> getProjectIds() {
        if (delegate.getProjectIds() == null) {
            setProjectIds(new ArrayList<>());
        }
        return delegate.getProjectIds();
    }

    protected void setConfigurations(java.util.List<String> configurations) {
        delegate.setConfigurations(configurations);
    }

    protected void setDescription(String description) {
        delegate.setDescription(description);
    }

    protected void setEnabled(Boolean enabled) {
        delegate.setEnabled(enabled);
    }

    protected void setProjectIds(java.util.List<String> projectIds) {
        delegate.setProjectIds(projectIds);
    }

    public boolean equals(Object object) {
        if (object instanceof AbstractExecutionProfileCreate) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
