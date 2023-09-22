package com.mercedesbenz.sechub.xraywrapper.http;

import java.net.MalformedURLException;
import java.net.URL;

public class XrayAPIRequest {
    public enum RequestMethodEnum {
        GET, POST;
    }

    private String baseUrl;

    private URL url;

    private RequestMethodEnum requestMethodEnum;

    private boolean authentication = false;

    private String data;

    public XrayAPIRequest() {
    }

    public XrayAPIRequest(String baseUrl, RequestMethodEnum requestMethodEnum, boolean authentication, String data) {
        this.baseUrl = baseUrl;
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

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setAuthentication(boolean authentication) {
        this.authentication = authentication;
    }

    public boolean needAuthentication() {
        return authentication;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public URL getUrl() throws MalformedURLException {
        if (url == null) {
            url = stringToUrl();
        }
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    private URL stringToUrl() throws MalformedURLException {
        return new URL(this.baseUrl);
    }
}
