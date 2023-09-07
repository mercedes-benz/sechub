// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.pdsclient.internal;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import com.mercedesbenz.sechub.systemtest.pdsclient.PDSClient;

public class PDSClientAuthenticator extends Authenticator {
    private PasswordAuthentication paswordAuthentication;

    public PDSClientAuthenticator(PDSClient client) {
        this.paswordAuthentication = new PasswordAuthentication(client.getUsername(), client.getSealedApiToken().toCharArray());
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return paswordAuthentication;
    }

}