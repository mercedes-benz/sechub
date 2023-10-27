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

        private XrayAPIRequest.RequestMethodEnum requestMethodEnum;

        private boolean authentication = false;

        private String data = "";

        private Builder(URL url, XrayAPIRequest.RequestMethodEnum requestMethodEnum) {
            this.url = url;
            this.requestMethodEnum = requestMethodEnum;
        }

        public static Builder builder(URL url, XrayAPIRequest.RequestMethodEnum requestMethodEnum) throws XrayWrapperException {
            if ((url == null) || (requestMethodEnum == null)) {
                throw new XrayWrapperException("Cannot create XrayAPIRequest with null parameters", XrayWrapperExitCode.INVALID_HTTP_REQUEST);
            }
            return new Builder(url, requestMethodEnum);
        }

        public XrayAPIRequest build() {
            return new XrayAPIRequest(this.url, this.requestMethodEnum, this.authentication, this.data);
        }

        Builder buildJSONBody(String data) {
            this.data = data;
            return this;
        }

        Builder isAuthenticationNeeded(boolean b) {
            this.authentication = b;
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
