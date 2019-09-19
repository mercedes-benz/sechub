package com.daimler.sechub.adapter;

import javax.crypto.SealedObject;

public class FormAutomatedLoginConfig implements LoginConfig{

	SealedObject user;
	SealedObject password;

	public String getUser() {
		return decrypt(user);
	}

	public String getPassword() {
		return decrypt(password);
	}


}
