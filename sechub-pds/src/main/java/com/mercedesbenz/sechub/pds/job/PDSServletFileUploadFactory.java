// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.springframework.stereotype.Component;

@Component
public class PDSServletFileUploadFactory {
    public JakartaServletFileUpload create() {
        return new JakartaServletFileUpload();
    }
}
