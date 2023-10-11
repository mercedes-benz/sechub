package com.mercedesbenz.sechub.xraywrapper.api;

import java.net.MalformedURLException;
import java.net.URL;

public class XrayAPIRequest {
    public enum RequestMethodEnum {
        GET, POST, DELETE;
    }

    private String stringUrl;

    private URL url;

    private RequestMethodEnum requestMethodEnum;

    private boolean authentication = false;

    private String data;

    public XrayAPIRequest() {
    }

    public XrayAPIRequest(String stringUrl, RequestMethodEnum requestMethodEnum, boolean authentication, String data) {
        this.stringUrl = stringUrl;
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

    public void setStringUrl(String stringUrl) {
        this.stringUrl = stringUrl;
    }

    public String getStringUrl() {
        return stringUrl;
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
        return new URL(this.stringUrl);
    }
}
