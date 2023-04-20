// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * AbstractUserSignup is a model class for SecHubClient. It uses internally the
 * generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup.<br>
 * <br>
 * The abstract wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.AbstractModelFileGenerator and is not
 * intended to be changed manually!
 */
public abstract class AbstractUserSignup {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup delegate;

    protected AbstractUserSignup() {
        this(null);
    }

    protected AbstractUserSignup(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup delegate) {
        if (delegate == null) {
            this.delegate = new com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup();
            initDelegateWithDefaults();
        } else {
            this.delegate = delegate;
        }
    }

    protected void initDelegateWithDefaults() {
        /* child classes can override this */
    }

    protected String getApiVersion() {
        return delegate.getApiVersion();
    }

    protected String getEmailAdress() {
        return delegate.getEmailAdress();
    }

    protected String getUserId() {
        return delegate.getUserId();
    }

    protected void setApiVersion(String apiVersion) {
        delegate.setApiVersion(apiVersion);
    }

    protected void setEmailAdress(String emailAdress) {
        delegate.setEmailAdress(emailAdress);
    }

    protected void setUserId(String userId) {
        delegate.setUserId(userId);
    }

    public boolean equals(Object object) {
        if (object instanceof AbstractUserSignup) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
