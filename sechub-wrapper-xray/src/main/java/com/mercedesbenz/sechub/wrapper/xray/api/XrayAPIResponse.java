package com.mercedesbenz.sechub.wrapper.xray.api;

import java.util.List;
import java.util.Map;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

public class XrayAPIResponse {

    private int statusCode;
    private Map<String, List<String>> headers;
    private String responseMessage;
    private String body;

    public static class Builder {

        private int statusCode;
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
            return new XrayAPIResponse(this.statusCode, this.headers, this.body);
        }

        Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
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

    private XrayAPIResponse(int statusCode, Map<String, List<String>> headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
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
