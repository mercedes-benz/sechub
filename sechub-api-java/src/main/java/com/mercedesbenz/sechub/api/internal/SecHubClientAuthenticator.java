package com.mercedesbenz.sechub.api.internal;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import com.mercedesbenz.sechub.api.SecHubClient;

public class SecHubClientAuthenticator extends Authenticator {
    private PasswordAuthentication paswordAuthentication;

    public SecHubClientAuthenticator(SecHubClient access) {
        this.paswordAuthentication = new PasswordAuthentication(access.getUsername(), access.getSealedApiToken().toCharArray());
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return paswordAuthentication;
    }
}