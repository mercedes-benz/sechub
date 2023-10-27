package com.mercedesbenz.sechub.wrapper.xray.report;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

public class XrayWrapperReportException extends XrayWrapperException {

    public XrayWrapperReportException(String message) {
        this("Error occurred during report handling: " + message, null);
    }

    public XrayWrapperReportException(String message, Throwable cause) {
        super(message, XrayWrapperExitCode.CREATE_CYCLONEDX_REPORT_ERROR, cause);
    }
}
