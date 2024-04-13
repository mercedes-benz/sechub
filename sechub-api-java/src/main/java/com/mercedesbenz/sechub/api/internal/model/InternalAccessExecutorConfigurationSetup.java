// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * InternalAccessExecutorConfigurationSetup is a model class for SecHubClient.
 * It uses internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup.<br>
 * <br>
 * The internal access wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.InternalAccessModelFileGenerator and is
 * not intended to be changed manually!
 */
public class InternalAccessExecutorConfigurationSetup {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup delegate;

    protected InternalAccessExecutorConfigurationSetup() {
        this(null);
    }

    public InternalAccessExecutorConfigurationSetup(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup delegate) {
        if (delegate == null) {
            this.delegate = new com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup();
            initDelegateWithDefaults();
        } else {
            this.delegate = delegate;
        }
    }

    protected void initDelegateWithDefaults() {
        /* child classes can override this */
    }

    public String getBaseURL() {
        return delegate.getBaseURL();
    }

    public void setBaseURL(String baseURL) {
        delegate.setBaseURL(baseURL);
    }

    public com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup getDelegate() {
        return delegate;
    }

    public boolean equals(Object object) {
        if (object instanceof InternalAccessExecutorConfigurationSetup) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
