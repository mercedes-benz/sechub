package com.mercedesbenz.sechub.api.java.demo.cli;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import com.mercedesbenz.sechub.api.java.SecHubClient;

public class SecHubClientAuthenticator extends Authenticator{
    private PasswordAuthentication paswordAuthentication;
    
    SecHubClientAuthenticator(SecHubClient client){
        this.paswordAuthentication= new PasswordAuthentication(client.getUsername(),client.getSealedApiToken().toCharArray());
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return paswordAuthentication;
    }
}