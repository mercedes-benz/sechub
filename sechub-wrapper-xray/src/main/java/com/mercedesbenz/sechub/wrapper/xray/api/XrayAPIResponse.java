// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.api;

import java.util.List;
import java.util.Map;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

public class XrayAPIResponse {

    private int httpStatusCode;
    private Map<String, List<String>> headers;
    private String responseMessage;
    private String body;

    public static class Builder {

        private int httpStatusCode;
        private Map<String, List<String>> headers;
        private String responseMessage = "";

        private String body = "";

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public XrayAPIResponse build() throws XrayWrapperException {
            if (this.headers == null) {
                throw new XrayWrapperException("HTTP response headers cannot be null!", XrayWrapperExitCode.INVALID_HTTP_RESPONSE);
            }
            return new XrayAPIResponse(this.httpStatusCode, this.headers, this.body);
        }

        Builder httpStatusCode(int httpStatusCode) throws XrayWrapperException {
            if (httpStatusCode > 0 && httpStatusCode < 600) {
                this.httpStatusCode = httpStatusCode;
                return this;
            }
            throw new XrayWrapperException("HTTP status code is out of range. Must be between 0 and 600.", XrayWrapperExitCode.UNKNOWN_ERROR);
        }

        Builder headers(Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        Builder addResponseBody(String body) {
            this.body = body;
            return this;
        }

        Builder addResponseMessage(String responseMessage) {
            this.responseMessage = responseMessage;
            return this;
        }
    }

    private XrayAPIResponse(int httpStatusCode, Map<String, List<String>> headers, String body) {
        this.httpStatusCode = httpStatusCode;
        this.headers = headers;
        this.body = body;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getResponseMessage() {
        return responseMessage;
    }
}
