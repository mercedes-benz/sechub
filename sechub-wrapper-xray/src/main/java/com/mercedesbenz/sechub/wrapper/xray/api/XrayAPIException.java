// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.api;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

public class XrayAPIException extends XrayWrapperException {

    private int statusCode;
    private String serverMessage;
    private String errorBody;

    protected XrayAPIException(String message, int statusCode, String serverMessage, String errorBody) {
        super(message + ", status code: " + statusCode + ", serverMessage: " + serverMessage + ", errorBody: " + errorBody,
                XrayWrapperExitCode.ARTIFACTORY_ERROR_RESPONSE);
        this.statusCode = statusCode;
        this.serverMessage = serverMessage;
        this.errorBody = errorBody;
    }

    protected XrayAPIException(String message, int statusCode, String serverMessage, String errorBody, Throwable cause) {
        super(message + ", status code: " + statusCode + ", serverMessage: " + serverMessage + ", errorBody: " + errorBody,
                XrayWrapperExitCode.ARTIFACTORY_ERROR_RESPONSE, cause);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getServerMessage() {
        return serverMessage;
    }

    public String getErrorBody() {
        return errorBody;
    }
}
