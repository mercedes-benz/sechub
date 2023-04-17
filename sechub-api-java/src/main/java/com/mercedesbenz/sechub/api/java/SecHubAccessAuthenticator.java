package com.mercedesbenz.sechub.api.java;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class SecHubAccessAuthenticator extends Authenticator {
    private PasswordAuthentication paswordAuthentication;

    SecHubAccessAuthenticator(SecHubAccess access) {
        this.paswordAuthentication = new PasswordAuthentication(access.getUsername(), access.getSealedApiToken().toCharArray());
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return paswordAuthentication;
    }
}