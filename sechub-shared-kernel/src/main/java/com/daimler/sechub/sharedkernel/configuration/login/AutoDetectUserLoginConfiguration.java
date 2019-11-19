package com.daimler.sechub.sharedkernel.configuration.login;

import javax.crypto.SealedObject;

import com.daimler.sechub.sharedkernel.SharedKernelCryptoAccess;

public class AutoDetectUserLoginConfiguration{

	private SharedKernelCryptoAccess<char[]> cryptoAccess = new SharedKernelCryptoAccess<>();
	private char[] user;
	SealedObject password;

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