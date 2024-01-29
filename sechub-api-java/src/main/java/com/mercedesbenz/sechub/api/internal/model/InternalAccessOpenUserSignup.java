// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * InternalAccessOpenUserSignup is a model class for SecHubClient. It uses
 * internally the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner.<br>
 * <br>
 * The internal access wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.InternalAccessModelFileGenerator and is
 * not intended to be changed manually!
 */
public class InternalAccessOpenUserSignup {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner delegate;

    protected InternalAccessOpenUserSignup() {
        this(null);
    }

    public InternalAccessOpenUserSignup(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner delegate) {
        if (delegate == null) {
            this.delegate = new com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner();
            initDelegateWithDefaults();
        } else {
            this.delegate = delegate;
        }
    }

    protected void initDelegateWithDefaults() {
        /* child classes can override this */
    }

    public String getEmailAdress() {
        return delegate.getEmailAdress();
    }

    public String getUserId() {
        return delegate.getUserId();
    }

    public void setEmailAdress(String emailAdress) {
        delegate.setEmailAdress(emailAdress);
    }

    public void setUserId(String userId) {
        delegate.setUserId(userId);
    }

    public com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner getDelegate() {
        return delegate;
    }

    public boolean equals(Object object) {
        if (object instanceof InternalAccessOpenUserSignup) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
