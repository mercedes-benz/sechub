// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

import java.util.ArrayList;

/**
 * InternalAccessProjectWhiteList is a model class for SecHubClient. It uses
 * internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList.<br>
 * <br>
 * The internal access wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.InternalAccessModelFileGenerator and is
 * not intended to be changed manually!
 */
public class InternalAccessProjectWhiteList {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList delegate;

    protected InternalAccessProjectWhiteList() {
        this(null);
    }

    public InternalAccessProjectWhiteList(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList delegate) {
        if (delegate == null) {
            this.delegate = new com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList();
            initDelegateWithDefaults();
        } else {
            this.delegate = delegate;
        }
    }

    protected void initDelegateWithDefaults() {
        /* child classes can override this */
    }

    public java.util.List<String> getUris() {
        if (delegate.getUris() == null) {
            setUris(new ArrayList<>());
        }
        return delegate.getUris();
    }

    public void setUris(java.util.List<String> uris) {
        delegate.setUris(uris);
    }

    public com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList getDelegate() {
        return delegate;
    }

    public boolean equals(Object object) {
        if (object instanceof InternalAccessProjectWhiteList) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
