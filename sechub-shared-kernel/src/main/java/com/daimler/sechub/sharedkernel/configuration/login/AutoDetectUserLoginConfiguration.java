package com.daimler.sechub.sharedkernel.configuration.login;

public class AutoDetectUserLoginConfiguration{

	private char[] user;
	private char[] password;

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