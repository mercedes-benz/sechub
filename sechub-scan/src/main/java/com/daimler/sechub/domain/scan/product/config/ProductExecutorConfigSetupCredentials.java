// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

public class ProductExecutorConfigSetupCredentials {

    public static final String PROPERTY_USER = "user";
    public static final String PROPERTY_PASSWORD = "password";

    private String user;

    private String password;
    

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
