// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.encryption;

public class SecHubPasswordSource {
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_DATA = "data";

    private SecHubCipherPasswordSourceType type;
    private String data;

    public void setData(String data) {
        this.data = data;
    }

    public void setType(SecHubCipherPasswordSourceType type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public SecHubCipherPasswordSourceType getType() {
        return type;
    }
}