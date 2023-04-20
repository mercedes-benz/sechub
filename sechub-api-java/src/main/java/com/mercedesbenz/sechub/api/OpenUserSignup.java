// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenUserSignup is a model class for SecHubClient. It uses internally the
 * generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner.<br>
 * <br>
 * The wrapper class itself was initial generated with
 * com.mercedesbenz.sechub.api.generator.PublicModelFileGenerator.
 */
public class OpenUserSignup extends com.mercedesbenz.sechub.api.internal.model.AbstractOpenUserSignup {

    // only for usage by SecHubClient
    static List<OpenUserSignup> fromDelegates(List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner> delegates) {
        List<OpenUserSignup> resultList = new ArrayList<>();
        if (delegates != null) {
            for (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner delegate : delegates) {
                resultList.add(new OpenUserSignup(delegate));
            }
        }
        return resultList;
    }

    // only for usage by SecHubClient
    static List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner> toDelegates(List<OpenUserSignup> wrappers) {
        List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner> resultList = new ArrayList<>();
        if (wrappers != null) {
            for (OpenUserSignup wrapper : wrappers) {
                resultList.add(wrapper.getDelegate());
            }
        }
        return resultList;
    }

    public OpenUserSignup() {
        super();
    }

    public String getEmailAdress() {
        return super.getEmailAdress();
    }

    public String getUserId() {
        return super.getUserId();
    }

    public void setEmailAdress(String emailAdress) {
        super.setEmailAdress(emailAdress);
    }

    public void setUserId(String userId) {
        super.setUserId(userId);
    }

    // only for usage by SecHubClient
    OpenUserSignup(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner delegate) {
        super(delegate);
    }

    // only for usage by SecHubClient
    com.mercedesbenz.sechub.api.internal.gen.model.OpenApiListOfSignupsInner getDelegate() {
        return delegate;
    }

}
