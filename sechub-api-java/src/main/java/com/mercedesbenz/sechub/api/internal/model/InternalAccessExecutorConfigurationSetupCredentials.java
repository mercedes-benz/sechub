// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * InternalAccessExecutorConfigurationSetupCredentials is a model class for
 * SecHubClient. It uses internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials.<br>
 * <br>
 * The internal access wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.InternalAccessModelFileGenerator and is
 * not intended to be changed manually!
 */
public class InternalAccessExecutorConfigurationSetupCredentials {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials delegate;

    protected InternalAccessExecutorConfigurationSetupCredentials() {
        this(null);
    }

    public InternalAccessExecutorConfigurationSetupCredentials(
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials delegate) {
        if (delegate == null) {
            this.delegate = new com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials();
            initDelegateWithDefaults();
        } else {
            this.delegate = delegate;
        }
    }

    protected void initDelegateWithDefaults() {
        /* child classes can override this */
    }

    public String getPassword() {
        return delegate.getPassword();
    }

    public String getUser() {
        return delegate.getUser();
    }

    public void setPassword(String password) {
        delegate.setPassword(password);
    }

    public void setUser(String user) {
        delegate.setUser(user);
    }

    public com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials getDelegate() {
        return delegate;
    }

    public boolean equals(Object object) {
        if (object instanceof InternalAccessExecutorConfigurationSetupCredentials) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
