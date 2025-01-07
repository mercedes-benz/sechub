// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;

public class ZapWrapperContextCreationException extends Exception {
    private static final long serialVersionUID = 1L;

    private final ZapWrapperExitCode zapWrapperExitCode;

    public ZapWrapperContextCreationException(String message, ZapWrapperExitCode zapWrapperExitCode) {
        super(message);
        this.zapWrapperExitCode = zapWrapperExitCode;
    }

    public ZapWrapperExitCode getZapWrapperExitCode() {
        return zapWrapperExitCode;
    }

}
