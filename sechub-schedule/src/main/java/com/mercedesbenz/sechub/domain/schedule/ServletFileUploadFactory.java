package com.mercedesbenz.sechub.domain.schedule;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.stereotype.Component;

@Component
public class ServletFileUploadFactory {
    public ServletFileUpload create() {
        return new ServletFileUpload();
    }
}
