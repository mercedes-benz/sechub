// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

public enum BrowserId {

    FIREFOX_HEADLESS("firefox-headless"),

    ;

    private String browserId;

    private BrowserId(String browserId) {
        this.browserId = browserId;
    }

    public String getBrowserId() {
        return browserId;
    }

}
