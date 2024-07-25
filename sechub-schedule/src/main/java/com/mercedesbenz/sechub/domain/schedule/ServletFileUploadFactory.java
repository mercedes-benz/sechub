// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.springframework.stereotype.Component;

@Component
public class ServletFileUploadFactory {
    public JakartaServletFileUpload create() {
        return new JakartaServletFileUpload();
    }
}