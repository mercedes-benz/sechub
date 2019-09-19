package com.daimler.sechub.sharedkernel.configuration.login;

import java.util.Optional;

public class BasicLoginConfiguration {

	private Optional<String> realm;
	private char[] user;
	private char[] password;

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
		this.password = password;
	}

	public char[] getPassword() {
		return password;
	}

}