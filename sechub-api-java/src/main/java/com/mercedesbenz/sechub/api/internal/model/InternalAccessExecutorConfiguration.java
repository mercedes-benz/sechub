// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * InternalAccessExecutorConfiguration is a model class for SecHubClient. It
 * uses internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration.<br>
 * <br>
 * The internal access wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.InternalAccessModelFileGenerator and is
 * not intended to be changed manually!
 */
public class InternalAccessExecutorConfiguration {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration delegate;

    protected InternalAccessExecutorConfiguration() {
        this(null);
    }

    public InternalAccessExecutorConfiguration(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration delegate) {
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

    public Boolean getEnabled() {
        return delegate.getEnabled();
    }

    public String getName() {
        return delegate.getName();
    }

    public String getProductIdentifier() {
        return delegate.getProductIdentifier();
    }

    public java.math.BigDecimal getExecutorVersion() {
        return delegate.getExecutorVersion();
    }

    public void setEnabled(Boolean enabled) {
        delegate.setEnabled(enabled);
    }

    public void setExecutorVersion(java.math.BigDecimal executorVersion) {
        delegate.setExecutorVersion(executorVersion);
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public void setProductIdentifier(String productIdentifier) {
        delegate.setProductIdentifier(productIdentifier);
    }

    public com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration getDelegate() {
        return delegate;
    }

    public boolean equals(Object object) {
        if (object instanceof InternalAccessExecutorConfiguration) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfiguration) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
