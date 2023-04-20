// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * AbstractProjectMetaData is a model class for SecHubClient. It uses internally
 * the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData.<br>
 * <br>
 * The abstract wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.AbstractModelFileGenerator and is not
 * intended to be changed manually!
 */
public abstract class AbstractProjectMetaData {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData delegate;

    protected AbstractProjectMetaData() {
        this(null);
    }

    protected AbstractProjectMetaData(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData delegate) {
        if (delegate == null) {
            this.delegate = new com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData();
            initDelegateWithDefaults();
        } else {
            this.delegate = delegate;
        }
    }

    protected void initDelegateWithDefaults() {
        /* child classes can override this */
    }

    protected String getStar() {
        return delegate.getStar();
    }

    protected void setStar(String star) {
        delegate.setStar(star);
    }

    public boolean equals(Object object) {
        if (object instanceof AbstractProjectMetaData) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
