// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.signup;

import com.daimler.sechub.commons.model.JSONable;
import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by frontends to self register a user It has to be backward compatible. To afford this we will NOT remove older parts since final API releases")
public class SignupJsonInput implements JSONable<SignupJsonInput> {

	public static final String PROPERTY_API_VERSION = "apiVersion";
	public static final String PROPERTY_USER_ID = "userId";
	public static final String PROPERTY_EMAIL_ADRESS = "emailAdress";

	private String apiVersion;
	private String userId;
	private String emailAdress;


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


	public String getEmailAdress() {
		return emailAdress;
	}


	public void setEmailAdress(String emailAdress) {
		this.emailAdress = emailAdress;
	}

}
