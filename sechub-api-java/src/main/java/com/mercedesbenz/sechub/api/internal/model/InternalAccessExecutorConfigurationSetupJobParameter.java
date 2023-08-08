// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * InternalAccessExecutorConfigurationSetupJobParameter is a model class for
 * SecHubClient. It uses internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner.<br>
 * <br>
 * The internal access wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.InternalAccessModelFileGenerator and is
 * not intended to be changed manually!
 */
public class InternalAccessExecutorConfigurationSetupJobParameter {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner delegate;

    protected InternalAccessExecutorConfigurationSetupJobParameter() {
        this(null);
    }

    public InternalAccessExecutorConfigurationSetupJobParameter(
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

    public String getKey() {
        return delegate.getKey();
    }

    public String getValue() {
        return delegate.getValue();
    }

    public void setKey(String key) {
        delegate.setKey(key);
    }

    public void setValue(String value) {
        delegate.setValue(value);
    }

    public com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner getDelegate() {
        return delegate;
    }

    public boolean equals(Object object) {
        if (object instanceof InternalAccessExecutorConfigurationSetupJobParameter) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
