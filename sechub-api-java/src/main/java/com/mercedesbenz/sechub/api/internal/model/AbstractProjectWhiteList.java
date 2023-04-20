// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

import java.util.ArrayList;

/**
 * AbstractProjectWhiteList is a model class for SecHubClient. It uses
 * internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList.<br>
 * <br>
 * The abstract wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.AbstractModelFileGenerator and is not
 * intended to be changed manually!
 */
public abstract class AbstractProjectWhiteList {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList delegate;

    protected AbstractProjectWhiteList() {
        this(null);
    }

    protected AbstractProjectWhiteList(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList delegate) {
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

    protected java.util.List<String> getUris() {
        if (delegate.getUris() == null) {
            setUris(new ArrayList<>());
        }
        return delegate.getUris();
    }

    protected void setUris(java.util.List<String> uris) {
        delegate.setUris(uris);
    }

    public boolean equals(Object object) {
        if (object instanceof AbstractProjectWhiteList) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiProjectWhiteList) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
