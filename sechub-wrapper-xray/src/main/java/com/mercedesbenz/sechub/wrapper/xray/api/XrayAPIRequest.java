// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.api;

import java.net.URL;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

public class XrayAPIRequest {
    public enum RequestMethodEnum {
        GET, POST, DELETE;
    }

    private URL url;

    private RequestMethodEnum requestMethodEnum;

    private boolean authentication;

    private String data;

    public static class Builder {
        private URL url;

        private XrayAPIRequest.RequestMethodEnum requestMethodEnum = RequestMethodEnum.GET;

        private boolean authenticationNeeded;

        private String json = "";

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public XrayAPIRequest build() throws XrayWrapperException {
            if (url == null) {
                throw new XrayWrapperException("Cannot create XrayAPIRequest with null parameters", XrayWrapperExitCode.INVALID_HTTP_REQUEST);
            }
            return new XrayAPIRequest(this.url, this.requestMethodEnum, this.authenticationNeeded, this.json);
        }

        Builder url(URL url) {
            this.url = url;
            return this;
        }

        Builder requestMethod(XrayAPIRequest.RequestMethodEnum requestMethodEnum) {
            this.requestMethodEnum = requestMethodEnum;
            return this;
        }

        Builder jsonBody(String json) {
            this.json = json;
            return this;
        }

        Builder authenticationNeeded(boolean authenticationNeeded) {
            this.authenticationNeeded = authenticationNeeded;
            return this;
        }
    }

    private XrayAPIRequest(URL url, RequestMethodEnum requestMethodEnum, boolean authentication, String data) {
        this.url = url;
        this.requestMethodEnum = requestMethodEnum;
        this.authentication = authentication;
        this.data = data;
    }

    public RequestMethodEnum getRequestMethodEnum() {
        return requestMethodEnum;
    }

    public boolean isAuthenticationNeeded() {
        return authentication;
    }

    public String getData() {
        return data;
    }

    public URL getUrl() {
        return url;
    }
}
