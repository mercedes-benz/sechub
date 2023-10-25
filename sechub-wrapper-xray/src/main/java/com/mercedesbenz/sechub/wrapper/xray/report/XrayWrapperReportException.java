package com.mercedesbenz.sechub.wrapper.xray.report;

import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

public class XrayWrapperReportException extends RuntimeException {
    private final XrayWrapperExitCode exitCode;

    public XrayWrapperReportException(String message, XrayWrapperExitCode exitCode) {
        this(message, null, exitCode);
    }

    public XrayWrapperReportException(String message, Throwable cause, XrayWrapperExitCode exitCode) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public XrayWrapperExitCode getExitCode() {
        return exitCode;
    }
}
