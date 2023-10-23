package com.mercedesbenz.sechub.xraywrapper.api;

import java.net.URL;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;

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

        public static Builder create(URL url, XrayAPIRequest.RequestMethodEnum requestMethodEnum) {
            if ((url == null) || (requestMethodEnum == null)) {
                throw new XrayWrapperRuntimeException("Cannot create XrayAPIRequest with null parameters", XrayWrapperExitCode.INVALID_HTTP_REQUEST);
            }
            return new Builder(url, requestMethodEnum);
        }

        public XrayAPIRequest build() {
            return new XrayAPIRequest(this.url, this.requestMethodEnum, this.authentication, this.data);
        }

        Builder setData(String data) {
            this.data = data;
            return this;
        }

        Builder setAuthentication(boolean b) {
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

    public void setRequestMethodEnum(RequestMethodEnum requestMethodEnum) {
        this.requestMethodEnum = requestMethodEnum;
    }

    public RequestMethodEnum getRequestMethodEnum() {
        return requestMethodEnum;
    }

    public boolean needAuthentication() {
        return authentication;
    }

    public String getData() {
        return data;
    }

    public URL getUrl() {
        return url;
    }
}
