package com.mercedesbenz.sechub.xraywrapper.helper;

import com.mercedesbenz.sechub.xraywrapper.util.XrayAuthenticationHeader;

public class XrayAPIRequest {
    public enum RequestMethodEnum {
        GET, POST;
    }

    private String baseUrl;

    private RequestMethodEnum requestMethodEnum;

    private boolean authentication = false;

    private String data;

    private String filename;

    public XrayAPIRequest() {
    }

    public XrayAPIRequest(String baseUrl, RequestMethodEnum requestMethodEnum, boolean authentication, String data, String filename) {
        this.baseUrl = baseUrl;
        this.requestMethodEnum = requestMethodEnum;
        this.authentication = authentication;
        this.data = data;
        this.filename = filename;
    }

    public XrayAPIRequest(String baseUrl, RequestMethodEnum requestMethodEnum, boolean authentication, String data) {
        this.baseUrl = baseUrl;
        this.requestMethodEnum = requestMethodEnum;
        this.authentication = authentication;
        this.data = data;
        filename = "";
    }

    public void setRequestMethodEnum(RequestMethodEnum requestMethodEnum) {
        this.requestMethodEnum = requestMethodEnum;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setAuthentication(boolean authentication) {
        this.authentication = authentication;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public boolean needAuthentication() {
        return authentication;
    }

    public RequestMethodEnum getRequestMethodEnum() {
        return requestMethodEnum;
    }

    public String getData() {
        return data;
    }

    public String authenticate() {
        return XrayAuthenticationHeader.setAuthHeader();
    }

}
