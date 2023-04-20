// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.util.ArrayList;
import java.util.List;

/**
 * UserSignup is a model class for SecHubClient. It uses internally the
 * generated class
 * com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup.<br>
 * <br>
 * The wrapper class itself was initial generated with
 * com.mercedesbenz.sechub.api.generator.PublicModelFileGenerator.
 */
public class UserSignup extends com.mercedesbenz.sechub.api.internal.model.AbstractUserSignup {

    // only for usage by SecHubClient
    static List<UserSignup> fromDelegates(List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup> delegates) {
        List<UserSignup> resultList = new ArrayList<>();
        if (delegates != null) {
            for (com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup delegate : delegates) {
                resultList.add(new UserSignup(delegate));
            }
        }
        return resultList;
    }

    // only for usage by SecHubClient
    static List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup> toDelegates(List<UserSignup> wrappers) {
        List<com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup> resultList = new ArrayList<>();
        if (wrappers != null) {
            for (UserSignup wrapper : wrappers) {
                resultList.add(wrapper.getDelegate());
            }
        }
        return resultList;
    }

    public UserSignup() {
        super();
    }

    public String getApiVersion() {
        return super.getApiVersion();
    }

    public String getEmailAdress() {
        return super.getEmailAdress();
    }

    public String getUserId() {
        return super.getUserId();
    }

    public void setApiVersion(String apiVersion) {
        super.setApiVersion(apiVersion);
    }

    public void setEmailAdress(String emailAdress) {
        super.setEmailAdress(emailAdress);
    }

    public void setUserId(String userId) {
        super.setUserId(userId);
    }

    // only for usage by SecHubClient
    UserSignup(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup delegate) {
        super(delegate);
    }

    // only for usage by SecHubClient
    com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup getDelegate() {
        return delegate;
    }

}
