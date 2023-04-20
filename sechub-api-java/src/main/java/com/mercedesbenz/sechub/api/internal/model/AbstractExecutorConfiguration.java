// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * AbstractExecutorConfiguration is a model class for SecHubClient. It uses
 * internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration.<br>
 * <br>
 * The abstract wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.AbstractModelFileGenerator and is not
 * intended to be changed manually!
 */
public abstract class AbstractExecutorConfiguration {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration delegate;

    protected AbstractExecutorConfiguration() {
        this(null);
    }

    protected AbstractExecutorConfiguration(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration delegate) {
        if (delegate == null) {
            this.delegate = new com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration();
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

    protected String getName() {
        return delegate.getName();
    }

    protected String getProductIdentifier() {
        return delegate.getProductIdentifier();
    }

    protected java.math.BigDecimal getExecutorVersion() {
        return delegate.getExecutorVersion();
    }

    protected void setEnabled(Boolean enabled) {
        delegate.setEnabled(enabled);
    }

    protected void setExecutorVersion(java.math.BigDecimal executorVersion) {
        delegate.setExecutorVersion(executorVersion);
    }

    protected void setName(String name) {
        delegate.setName(name);
    }

    protected void setProductIdentifier(String productIdentifier) {
        delegate.setProductIdentifier(productIdentifier);
    }

    public boolean equals(Object object) {
        if (object instanceof AbstractExecutorConfiguration) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
