// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

import java.util.ArrayList;

/**
 * InternalAccessExecutionProfileCreate is a model class for SecHubClient. It
 * uses internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate.<br>
 * <br>
 * The internal access wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.InternalAccessModelFileGenerator and is
 * not intended to be changed manually!
 */
public class InternalAccessExecutionProfileCreate {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate delegate;

    protected InternalAccessExecutionProfileCreate() {
        this(null);
    }

    public InternalAccessExecutionProfileCreate(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate delegate) {
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

    public Boolean getEnabled() {
        return delegate.getEnabled();
    }

    public String getDescription() {
        return delegate.getDescription();
    }

    public java.util.List<String> getConfigurations() {
        if (delegate.getConfigurations() == null) {
            setConfigurations(new ArrayList<>());
        }
        return delegate.getConfigurations();
    }

    public java.util.List<String> getProjectIds() {
        if (delegate.getProjectIds() == null) {
            setProjectIds(new ArrayList<>());
        }
        return delegate.getProjectIds();
    }

    public void setConfigurations(java.util.List<String> configurations) {
        delegate.setConfigurations(configurations);
    }

    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    public void setEnabled(Boolean enabled) {
        delegate.setEnabled(enabled);
    }

    public void setProjectIds(java.util.List<String> projectIds) {
        delegate.setProjectIds(projectIds);
    }

    public com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate getDelegate() {
        return delegate;
    }

    public boolean equals(Object object) {
        if (object instanceof InternalAccessExecutionProfileCreate) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileCreate) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
