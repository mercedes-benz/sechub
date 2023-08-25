// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

import java.io.File;

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.zapwrapper.util.EnvironmentVariableReader;

public class ZapEventHandler {

    File cancelEventFile;
    EnvironmentVariableReader environmentVariableReader = new EnvironmentVariableReader();

    public ZapEventHandler() {
        String pdsJobEventsFolder = environmentVariableReader.readAsString(EnvironmentVariableConstants.PDS_JOB_EVENTS_FOLDER);
        if (pdsJobEventsFolder == null) {
            throw new ZapWrapperRuntimeException("PDS configuration invalid. Cannot send user messages, because environment variable "
                    + EnvironmentVariableConstants.PDS_JOB_EVENTS_FOLDER + " is not set.", ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        this.cancelEventFile = new File(pdsJobEventsFolder, "cancel_requested.json");
    }

    public boolean isScanCancelled() {
        return cancelEventFile.exists();
    }

    public void cancelScan(String scanContextName) {
        if (isScanCancelled()) {
            throw new ZapWrapperRuntimeException("Scan job: " + scanContextName + " was cancelled!", ZapWrapperExitCode.SCAN_JOB_CANCELLED);
        }
    }
}
