// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.cli;

public class XrayWrapperCommandLineParserException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public XrayWrapperCommandLineParserException(String message, Exception e) {
        super(message, e);
    }

    public XrayWrapperCommandLineParserException(String message) {
        super(message);
    }
}
