package com.mercedesbenz.sechub.xraywrapper.api;

import java.util.List;
import java.util.Map;

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
