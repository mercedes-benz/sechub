// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal.model;

/**
 * AbstractOpenUserSignup is a model class for SecHubClient. It uses internally
 * the generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner.<br>
 * <br>
 * The abstract wrapper class was generated from a developer with
 * com.mercedesbenz.sechub.api.generator.AbstractModelFileGenerator and is not
 * intended to be changed manually!
 */
public abstract class AbstractOpenUserSignup {

    protected com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner delegate;

    protected AbstractOpenUserSignup() {
        this(null);
    }

    protected AbstractOpenUserSignup(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner delegate) {
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

    protected String getEmailAdress() {
        return delegate.getEmailAdress();
    }

    protected String getUserId() {
        return delegate.getUserId();
    }

    protected void setEmailAdress(String emailAdress) {
        delegate.setEmailAdress(emailAdress);
    }

    protected void setUserId(String userId) {
        delegate.setUserId(userId);
    }

    public boolean equals(Object object) {
        if (object instanceof AbstractOpenUserSignup) {
            com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner other = (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner) object;
            return delegate.equals(other);
        }
        return false;
    }

    public int hashCode() {
        return delegate.hashCode();
    }
}
