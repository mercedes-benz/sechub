// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.signup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.model.JSONable;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by frontends to self register a user It has to be backward compatible. To afford this we will NOT remove older parts since final API releases")
public class SignupJsonInput implements JSONable<SignupJsonInput> {

    public static final String PROPERTY_API_VERSION = "apiVersion";
    public static final String PROPERTY_USER_ID = "userId";
    public static final String PROPERTY_EMAIL_ADDRESS = "emailAddress";

    private String apiVersion;
    private String userId;
    private String emailAddress;

    @Override
    public Class<SignupJsonInput> getJSONTargetClass() {
        return SignupJsonInput.class;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    @Deprecated // This method is only for backward compatibility
    public void setEmailAdress(String emailAddress) {
        this.setEmailAddress(emailAddress);
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

}
