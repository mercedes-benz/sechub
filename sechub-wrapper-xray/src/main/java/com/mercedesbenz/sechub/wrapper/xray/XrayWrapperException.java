// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray;

import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

public class XrayWrapperException extends Exception {

    private static final long serialVersionUID = 1L;
    private final XrayWrapperExitCode exitCode;

    public XrayWrapperException(String message, XrayWrapperExitCode exitCode) {
        this(message, exitCode, null);
    }

    public XrayWrapperException(String message, XrayWrapperExitCode exitCode, Throwable cause) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public XrayWrapperExitCode getExitCode() {
        return exitCode;
    }

}
