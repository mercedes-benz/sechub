// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import javax.crypto.SealedObject;

import com.daimler.sechub.commons.core.security.CryptoAccess;

public class LoginScriptStep {

	String type;
	String selector;
	SealedObject value;

	public String getSelector() {
		return selector;
	}

	public String getValue() {
		return CryptoAccess.CRYPTO_STRING.unseal(value);
	}

	public String getType() {
		return type;
	}

	public boolean isInput() {
		return "input".equalsIgnoreCase(type);
	}

	public boolean isUserName() {
		return "username".equalsIgnoreCase(type);
	}

	public boolean isPassword() {
		return "password".equalsIgnoreCase(type);
	}

	public boolean isClick() {
		return "click".equalsIgnoreCase(type);
	}
}
