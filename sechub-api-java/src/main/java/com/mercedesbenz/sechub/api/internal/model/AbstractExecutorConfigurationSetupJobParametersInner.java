// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * AbstractExecutorConfigurationSetupJobParametersInner is a model class for
 * SecHubClient. It uses internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner.<br>
 * <br>
 * The wrapper class itself was initial generated with
 * com.mercedesbenz.sechub.api.generator.AbstractModelFileGenerator.
 */
public class AbstractExecutorConfigurationSetupJobParametersInner {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner delegate;

    public AbstractExecutorConfigurationSetupJobParametersInner() {
        this(null);
    }

    public AbstractExecutorConfigurationSetupJobParametersInner(
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

    public String getValue() {
        return delegate.getValue();
    }

    public String getKey() {
        return delegate.getKey();
    }

    public void setValue(String value) {
        delegate.setValue(value);
    }

    public void setKey(String key) {
        delegate.setKey(key);
    }

    public boolean equals(Object object) {
        if (object instanceof AbstractExecutorConfigurationSetupJobParametersInner) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutorConfigurationSetupJobParametersInner) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
