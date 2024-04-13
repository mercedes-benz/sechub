// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * InternalAccessProjectMetaData is a model class for SecHubClient. It uses
 * internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData.<br>
 * <br>
 * The internal access wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.InternalAccessModelFileGenerator and is
 * not intended to be changed manually!
 */
public class InternalAccessProjectMetaData {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData delegate;

    protected InternalAccessProjectMetaData() {
        this(null);
    }

    public InternalAccessProjectMetaData(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData delegate) {
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

    public String getStar() {
        return delegate.getStar();
    }

    public void setStar(String star) {
        delegate.setStar(star);
    }

    public com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData getDelegate() {
        return delegate;
    }

    public boolean equals(Object object) {
        if (object instanceof InternalAccessProjectMetaData) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectMetaData) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
