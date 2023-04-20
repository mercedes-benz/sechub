// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * AbstractExecutorConfigurationSetupCredentials is a model class for
 * SecHubClient. It uses internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials.<br>
 * <br>
 * The abstract wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.AbstractModelFileGenerator and is not
 * intended to be changed manually!
 */
public abstract class AbstractExecutorConfigurationSetupCredentials {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials delegate;

    protected AbstractExecutorConfigurationSetupCredentials() {
        this(null);
    }

    protected AbstractExecutorConfigurationSetupCredentials(
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

    protected String getPassword() {
        return delegate.getPassword();
    }

    protected String getUser() {
        return delegate.getUser();
    }

    protected void setPassword(String password) {
        delegate.setPassword(password);
    }

    protected void setUser(String user) {
        delegate.setUser(user);
    }

    public boolean equals(Object object) {
        if (object instanceof AbstractExecutorConfigurationSetupCredentials) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupCredentials) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
