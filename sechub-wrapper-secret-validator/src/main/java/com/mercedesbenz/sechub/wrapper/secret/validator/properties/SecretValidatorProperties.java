// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.properties;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "secret.validator")
public class SecretValidatorProperties {

    private final File configFile;
    private final boolean trustAllCertificates;

    @ConstructorBinding
    public SecretValidatorProperties(@NotNull String configFile, boolean trustAllCertificates) {
        this.configFile = new File(configFile);

        if (!this.configFile.exists()) {
            throw new IllegalStateException("The configuration file " + configFile + " does not exist!");
        }
        if (!this.configFile.canRead()) {
            throw new IllegalStateException("The configuration file " + configFile + "  is not readable!");
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
