package com.mercedesbenz.sechub.xraywrapper.cli;

public class XrayWrapperCommandLineParserException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public XrayWrapperCommandLineParserException(String message, Exception e) {
        super(message, e);
    }

    public XrayWrapperCommandLineParserException(String message) {
        super(message);
    }
}
