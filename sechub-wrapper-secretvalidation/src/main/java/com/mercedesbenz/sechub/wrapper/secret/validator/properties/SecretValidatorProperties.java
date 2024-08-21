// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.properties;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "secret.validator")
public class SecretValidatorProperties {

    private final File configFile;
    private long connectionRetries;

    @ConstructorBinding
    public SecretValidatorProperties(File configFile, long connectionRetries) {
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

        this.connectionRetries = connectionRetries;
    }

    public File getConfigFile() {
        return configFile;
    }

    public long getConnectionRetries() {
        return connectionRetries;
    }
}
