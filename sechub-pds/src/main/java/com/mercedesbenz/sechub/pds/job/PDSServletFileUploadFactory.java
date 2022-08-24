// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.stereotype.Component;

@Component
public class PDSServletFileUploadFactory {
    public ServletFileUpload create() {
        return new ServletFileUpload();
    }
}
