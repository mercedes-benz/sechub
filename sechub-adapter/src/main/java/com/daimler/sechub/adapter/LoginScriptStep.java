// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import javax.crypto.SealedObject;

import com.daimler.sechub.commons.core.security.CryptoAccess;

public class LoginScriptStep {

	LoginScriptStepAction action;
	String selector;
	String description;

	SealedObject value;

	public String getSelector() {
		return selector;
	}

	public String getValue() {
		return CryptoAccess.CRYPTO_STRING.unseal(value);
	}

	public LoginScriptStepAction getAction() {
		return action;
	}

	public boolean isInput() {
		return "input".equalsIgnoreCase(action.name());
	}

	public boolean isUserName() {
		return "username".equalsIgnoreCase(action.name());
	}

	public boolean isPassword() {
		return "password".equalsIgnoreCase(action.name());
	}

	public boolean isClick() {
		return "click".equalsIgnoreCase(action.name());
	}
	
    public String getDescription() {
        return description;
    }
}
