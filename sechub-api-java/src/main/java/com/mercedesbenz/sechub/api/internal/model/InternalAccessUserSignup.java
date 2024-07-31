// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * InternalAccessUserSignup is a model class for SecHubClient. It uses
 * internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup.<br>
 * <br>
 * The internal access wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.InternalAccessModelFileGenerator and is
 * not intended to be changed manually!
 */
public class InternalAccessUserSignup {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup delegate;

    protected InternalAccessUserSignup() {
        this(null);
    }

    public InternalAccessUserSignup(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup delegate) {
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

    public String getApiVersion() {
        return delegate.getApiVersion();
    }

    public String getEmailAddress() {
        return delegate.getEmailAddress();
    }

    public String getUserId() {
        return delegate.getUserId();
    }

    public void setApiVersion(String apiVersion) {
        delegate.setApiVersion(apiVersion);
    }

    public void setEmailAddress(String emailAddress) {
        delegate.setEmailAddress(emailAddress);
    }

    public void setUserId(String userId) {
        delegate.setUserId(userId);
    }

    public com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup getDelegate() {
        return delegate;
    }

    public boolean equals(Object object) {
        if (object instanceof InternalAccessUserSignup) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
