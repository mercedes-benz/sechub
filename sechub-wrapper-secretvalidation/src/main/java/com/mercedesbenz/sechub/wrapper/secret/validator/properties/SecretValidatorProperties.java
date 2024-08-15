// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.properties;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "secret.validator")
public class SecretValidatorProperties {

    private final File configFile;
    private final boolean trustAllCertificates;

    @ConstructorBinding
    public SecretValidatorProperties(File configFile, boolean trustAllCertificates) {
        if (configFile == null) {
            throw new IllegalArgumentException("The secret validator configuration file is null!");
        }

        this.configFile = configFile;

        if (!this.configFile.exists()) {
            throw new IllegalArgumentException("The secret validator configuration file " + configFile + " does not exist!");
        }
        if (!this.configFile.canRead()) {
            throw new IllegalArgumentException("The secret validator configuration file " + configFile + "  is not readable!");
        }

        this.trustAllCertificates = trustAllCertificates;
    }

    public File getConfigFile() {
        return configFile;
    }

    public boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }

}
