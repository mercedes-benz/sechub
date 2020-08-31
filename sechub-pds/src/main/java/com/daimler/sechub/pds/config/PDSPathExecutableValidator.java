// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

@Component
public class PDSPathExecutableValidator {

    /**
     * Creates error message when validation failed
     * 
     * @param productId
     * @return failure message or <code>null</code> when valid
     */
    public String createValidationErrorMessage(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "no path set!";
        }
        Path p = Paths.get(path);
        if (Files.notExists(p)) {
            return "file does not exist:"+path;
        }
        if (! Files.isExecutable(p)) {
            return "file exists, but not executable:"+path;
        }
        
        return null;
    }
}
