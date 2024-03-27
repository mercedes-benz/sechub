package com.mercedesbenz.sechub.wrapper.prepare.cli;

import org.springframework.stereotype.Component;

@Component
public class PrepareWrapperResultStatus {

    private final String STATUS_OK = "SECHUB_PREPARE_RESULT;status=ok";
    private final String STATUS_FAILED = "SECHUB_PREPARE_RESULT;status=failed";

    public String getSTATUS_OK() {
        return STATUS_OK;
    }

    public String getSTATUS_FAILED() {
        return STATUS_FAILED;
    }
}
