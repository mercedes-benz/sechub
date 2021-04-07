// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import javax.crypto.SealedObject;

import com.daimler.sechub.commons.core.security.CryptoAccess;

public class LoginScriptAction {

	ActionType actionType;
	String selector;
	String description;
	SealedObject value;
	SecHubTimeUnit unit;

	public SecHubTimeUnit getUnit() {
        return unit;
    }

    public String getSelector() {
		return selector;
	}

	public String getValue() {
		return CryptoAccess.CRYPTO_STRING.unseal(value);
	}

	public ActionType getActionType() {
		return actionType;
	}
	
	public boolean isWait() {
	    return getActionType() == ActionType.WAIT;
	}

	public boolean isInput() {
	    return getActionType() == ActionType.INPUT;
	}

	public boolean isUserName() {
	    return getActionType() == ActionType.USERNAME;
	}

	public boolean isPassword() {
	    return getActionType() == ActionType.PASSWORD;
	}

	public boolean isClick() {
	    return getActionType() == ActionType.CLICK;
	}
	
    public String getDescription() {
        return description;
    }
}
