// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.properties;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "pds.job.result")
public class SecretValidatorPDSJobResult {

    private final File file;

    @ConstructorBinding
    public SecretValidatorPDSJobResult(File file) {
        if (file == null) {
            throw new IllegalArgumentException("The PDS result file is null!");
        }

        this.file = file;

        if (!this.file.exists()) {
            throw new IllegalArgumentException("The PDS result file " + file + " does not exist!");
        }
        if (!this.file.canRead()) {
            throw new IllegalArgumentException("The PDS result file " + file + " is not readable!");
        }
    }

    public File getFile() {
        return file;
    }

}
