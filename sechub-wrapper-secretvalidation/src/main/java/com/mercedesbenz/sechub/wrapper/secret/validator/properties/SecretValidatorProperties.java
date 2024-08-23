// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.properties;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "secret.validator")
public class SecretValidatorProperties {

    private final File configFile;
    private int maximumRetries;
    private long timeoutSeconds;

    @ConstructorBinding
    public SecretValidatorProperties(File configFile, int maximumRetries, long timeoutSeconds) {
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

        this.maximumRetries = maximumRetries;
        this.timeoutSeconds = timeoutSeconds;
    }

    public File getConfigFile() {
        return configFile;
    }

    public int getMaximumRetries() {
        return maximumRetries;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
