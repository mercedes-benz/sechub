// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

import java.io.File;

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;

public class ZapEventHandler {
    File cancelEventFile;

    public ZapEventHandler(String pdsJobEventsFolder) {
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
