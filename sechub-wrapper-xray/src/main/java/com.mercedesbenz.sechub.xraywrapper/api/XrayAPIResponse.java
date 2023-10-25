package com.mercedesbenz.sechub.xraywrapper.api;

import java.util.List;
import java.util.Map;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;

public class XrayAPIResponse {

    private int statusCode;
    private Map<String, List<String>> headers;
    private String body;

    public static class Builder {

        private int statusCode;
        private Map<String, List<String>> headers;

        private String body = "";

        private Builder(int statusCode, Map<String, List<String>> headers) {
            this.statusCode = statusCode;
            this.headers = headers;
        }

        public static Builder create(int statusCode, Map<String, List<String>> headers) {
            if (headers == null) {
                throw new XrayWrapperRuntimeException("HTTP response headers cannot be null!", XrayWrapperExitCode.INVALID_HTTP_RESPONSE);
            }
            return new Builder(statusCode, headers);
        }

        public XrayAPIResponse build() {
            return new XrayAPIResponse(this.statusCode, this.headers, this.body);
        }

        Builder setBody(String body) {
            this.body = body;
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

}
