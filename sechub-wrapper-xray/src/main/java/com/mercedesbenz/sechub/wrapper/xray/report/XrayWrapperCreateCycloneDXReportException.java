// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.report;

import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

public class XrayWrapperCreateCycloneDXReportException extends XrayWrapperReportException {

    public XrayWrapperCreateCycloneDXReportException(String message) {
        this("Error occurred during report handling: " + message, null);
    }

    public XrayWrapperCreateCycloneDXReportException(String message, Throwable cause) {
        super(message, XrayWrapperExitCode.CREATE_CYCLONEDX_REPORT_ERROR, cause);
    }
}