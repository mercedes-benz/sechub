// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * AbstractExecutorConfigurationSetupJobParameter is a model class for
 * SecHubClient. It uses internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner.<br>
 * <br>
 * The abstract wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.AbstractModelFileGenerator and is not
 * intended to be changed manually!
 */
public abstract class AbstractExecutorConfigurationSetupJobParameter {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner delegate;

    protected AbstractExecutorConfigurationSetupJobParameter() {
        this(null);
    }

    protected AbstractExecutorConfigurationSetupJobParameter(
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner delegate) {
        if (delegate == null) {
            this.delegate = new com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner();
            initDelegateWithDefaults();
        } else {
            this.delegate = delegate;
        }
    }

    protected void initDelegateWithDefaults() {
        /* child classes can override this */
    }

    protected String getKey() {
        return delegate.getKey();
    }

    protected String getValue() {
        return delegate.getValue();
    }

    protected void setKey(String key) {
        delegate.setKey(key);
    }

    protected void setValue(String value) {
        delegate.setValue(value);
    }

    public boolean equals(Object object) {
        if (object instanceof AbstractExecutorConfigurationSetupJobParameter) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
