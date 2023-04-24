package com.mercedesbenz.sechub.systemtest.config;

import java.net.URL;

public class AbstractSecHubDefinition extends AbstractDefinition {

    private URL url = DefaultFallbackUtil.convertToURL(DefaultFallback.FALLBACK_SECHUB_LOCAL_URL);

    private CredentialsDefinition user = new CredentialsDefinition();

    private CredentialsDefinition admin = new CredentialsDefinition();

    public CredentialsDefinition getUser() {
        return user;
    }

    public CredentialsDefinition getAdmin() {
        return admin;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }
}
