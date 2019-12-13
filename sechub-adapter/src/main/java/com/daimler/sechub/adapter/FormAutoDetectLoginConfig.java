// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import javax.crypto.SealedObject;

public class FormAutoDetectLoginConfig extends AbstractLoginConfig{

	SealedObject user;
	SealedObject password;

	public String getUser() {
		return decrypt(user);
	}

	public String getPassword() {
		return decrypt(password);
	}


}
