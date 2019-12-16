// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration.login;

import java.util.Optional;

import javax.crypto.SealedObject;

import com.daimler.sechub.sharedkernel.SharedKernelCryptoAccess;

public class BasicLoginConfiguration {
	private SharedKernelCryptoAccess<char[]> cryptoAccess = SharedKernelCryptoAccess.CRYPTO_CHAR_ARRAY;

	private Optional<String> realm;
	private char[] user;
	SealedObject password;

	public Optional<String> getRealm() {
		return realm;
	}

	public void setUser(char[] user) {
		this.user = user;
	}

	public char[] getUser() {
		return user;
	}

	public void setPassword(char[] password) {
		this.password= cryptoAccess.seal(password);
	}

	public char[] getPassword() {
		return cryptoAccess.unseal(password);
	}

}