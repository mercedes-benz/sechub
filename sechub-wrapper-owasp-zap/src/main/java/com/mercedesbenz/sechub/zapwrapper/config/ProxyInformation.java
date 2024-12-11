// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import javax.crypto.SealedObject;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

public class ProxyInformation {
    private String host;
    private int port;

    private String realm;
    private String username;
    private SealedObject password;

    private ProxyInformation(String host, int port, String realm, String username, String password) {
        this.host = host;
        this.port = port;
        this.realm = realm;
        this.username = username;
        this.password = CryptoAccess.CRYPTO_STRING.seal(password);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getRealm() {
        return realm;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return CryptoAccess.CRYPTO_STRING.unseal(password);
    }

    public static ProxyInformationBuilder builder() {
        return new ProxyInformationBuilder();
    }

    public static class ProxyInformationBuilder {
        private String host;
        private int port;
        private String realm;
        private String username;
        private String password;

        public ProxyInformationBuilder setHost(String host) {
            this.host = host;
            return this;
        }

        public ProxyInformationBuilder setPort(int port) {
            this.port = port;
            return this;
        }

        public ProxyInformationBuilder setRealm(String realm) {
            this.realm = realm;
            return this;
        }

        public ProxyInformationBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public ProxyInformationBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public ProxyInformation build() {
            return new ProxyInformation(host, port, realm, username, password);
        }
    }

}
