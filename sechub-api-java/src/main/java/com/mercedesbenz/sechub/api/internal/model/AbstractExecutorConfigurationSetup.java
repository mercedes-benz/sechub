// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * AbstractExecutorConfigurationSetup is a model class for SecHubClient. It uses
 * internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup.<br>
 * <br>
 * The abstract wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.AbstractModelFileGenerator and is not
 * intended to be changed manually!
 */
public abstract class AbstractExecutorConfigurationSetup {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup delegate;

    protected AbstractExecutorConfigurationSetup() {
        this(null);
    }

    protected AbstractExecutorConfigurationSetup(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup delegate) {
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

    protected String getBaseURL() {
        return delegate.getBaseURL();
    }

    protected void setBaseURL(String baseURL) {
        delegate.setBaseURL(baseURL);
    }

    public boolean equals(Object object) {
        if (object instanceof AbstractExecutorConfigurationSetup) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetup) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
