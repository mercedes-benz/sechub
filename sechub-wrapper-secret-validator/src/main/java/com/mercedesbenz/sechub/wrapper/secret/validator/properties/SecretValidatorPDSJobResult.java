// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.properties;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "pds.job.result")
@Component
public class SecretValidatorPDSJobResult {

    @NotNull
    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}
