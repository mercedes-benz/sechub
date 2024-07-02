// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

public enum ZAPAcceptedBrowserId {

    FIREFOX_HEADLESS("firefox-headless"),

    FIREFOX("firefox"),

    CHROME_HEADLESS("chrome-headless"),

    CHROME("chrome"),

    HTMLUNIT("htmlunit"),

    SAFARI("safari"),

    ;

    private String browserId;

    private ZAPAcceptedBrowserId(String browserId) {
        this.browserId = browserId;
    }

    public String getBrowserId() {
        return browserId;
    }

}