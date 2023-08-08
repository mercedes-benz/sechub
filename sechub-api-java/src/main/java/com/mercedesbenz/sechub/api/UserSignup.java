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
public class UserSignup {
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

    private com.mercedesbenz.sechub.api.internal.model.InternalAccessUserSignup internalAccess;

    public UserSignup() {
        this(null);
    }

    UserSignup(com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup delegate) {
        this.internalAccess = new com.mercedesbenz.sechub.api.internal.model.InternalAccessUserSignup(delegate);
    }

    // only for usage by SecHubClient
    com.mercedesbenz.sechub.api.internal.gen.model.OpenApiUserSignup getDelegate() {
        return internalAccess.getDelegate();
    }

    public String getApiVersion() {
        return internalAccess.getApiVersion();
    }

    public String getEmailAdress() {
        return internalAccess.getEmailAdress();
    }

    public String getUserId() {
        return internalAccess.getUserId();
    }

    public void setApiVersion(String apiVersion) {
        internalAccess.setApiVersion(apiVersion);
    }

    public void setEmailAdress(String emailAdress) {
        internalAccess.setEmailAdress(emailAdress);
    }

    public void setUserId(String userId) {
        internalAccess.setUserId(userId);
    }

}
