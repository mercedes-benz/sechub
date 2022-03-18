// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config;

public class OwaspZapServerConfiguration {

    private String zaproxyAddress;
    private int zaproxyPort;
    private String zaproxyApiKey;

    OwaspZapServerConfiguration(String zaproxyAddress, int zaproxyPort, String zaproxyApiKey) {
        this.zaproxyAddress = zaproxyAddress;
        this.zaproxyPort = zaproxyPort;
        this.zaproxyApiKey = zaproxyApiKey;
    }

    public String getZaproxyHost() {
        return zaproxyAddress;
    }

    public int getZaproxyPort() {
        return zaproxyPort;
    }

    public String getZaproxyApiKey() {
        return zaproxyApiKey;
    }

}
