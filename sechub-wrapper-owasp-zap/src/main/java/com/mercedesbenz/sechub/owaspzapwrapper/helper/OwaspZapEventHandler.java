// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import java.io.File;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableConstants;
import com.mercedesbenz.sechub.owaspzapwrapper.util.EnvironmentVariableReader;

public class OwaspZapEventHandler {

    File cancelEventFile;

    public OwaspZapEventHandler() {
        this.cancelEventFile = new File(new EnvironmentVariableReader().readAsString(EnvironmentVariableConstants.PDS_JOB_EVENTS_FOLDER),
                "cancel_requested.json");
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
