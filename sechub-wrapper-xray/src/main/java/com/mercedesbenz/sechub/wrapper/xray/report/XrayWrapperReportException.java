// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.report;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

public class XrayWrapperReportException extends XrayWrapperException {

    public XrayWrapperReportException(String message) {
        this("Error occurred during report handling: " + message, XrayWrapperExitCode.UNKNOWN_ERROR, null);
    }

    public XrayWrapperReportException(String message, XrayWrapperExitCode exitCode, Throwable cause) {
        super(message, exitCode, cause);
    }
}