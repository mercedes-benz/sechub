// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

public class ZapServerConfiguration {

    private String zaproxyAddress;
    private int zaproxyPort;
    private String zaproxyApiKey;

    ZapServerConfiguration(String zaproxyAddress, int zaproxyPort, String zaproxyApiKey) {
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
