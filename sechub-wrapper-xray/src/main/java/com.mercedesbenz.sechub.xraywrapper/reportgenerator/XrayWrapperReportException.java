package com.mercedesbenz.sechub.xraywrapper.reportgenerator;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;

public class XrayWrapperReportException extends RuntimeException {
    private XrayWrapperExitCode exitCode;

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
