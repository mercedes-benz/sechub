// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.Optional;

public class AbstractSecHubDefinition extends AbstractDefinition {

    private Optional<Boolean> waitForAvailable = Optional.ofNullable(null);

    private String url;

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setWaitForAvailable(Optional<Boolean> waitForAvailable) {
        this.waitForAvailable = waitForAvailable;
    }

    public Optional<Boolean> getWaitForAvailable() {
        return waitForAvailable;
    }
}
