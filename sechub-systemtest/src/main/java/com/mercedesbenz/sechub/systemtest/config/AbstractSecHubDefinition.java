package com.mercedesbenz.sechub.systemtest.config;

import java.net.URL;
import java.util.Optional;

public class AbstractSecHubDefinition extends AbstractDefinition {

    private Optional<Boolean> waitForAvailable = Optional.ofNullable(null);

    private URL url;

    public void setUrl(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    public void setWaitForAvailable(Optional<Boolean> waitForAvailable) {
        this.waitForAvailable = waitForAvailable;
    }

    public Optional<Boolean> getWaitForAvailable() {
        return waitForAvailable;
    }
}
