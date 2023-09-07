package com.mercedesbenz.sechub.xraywrapper.helper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.mercedesbenz.sechub.xraywrapper.util.XrayAuthenticationHeader;
import com.mercedesbenz.sechub.xraywrapper.util.XrayFullResponseBuilder;

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

    public XrayAPIResponse sendRequest() throws IOException {
        URL url = new URL(getBaseUrl());
        HttpURLConnection con = null;
        if (requestMethodEnum == RequestMethodEnum.POST) {
            con = setUpPostConnection(url, data);
        } else if (requestMethodEnum == RequestMethodEnum.GET) {
            con = setUpGetConnection(url);
        } else {
            // default GET method
            requestMethodEnum = RequestMethodEnum.GET;
            con = setUpGetConnection(url);
        }
        return XrayFullResponseBuilder.getFullResponse(con, filename);
    }

    public String authenticate() {
        return XrayAuthenticationHeader.setAuthHeader();
    }

    protected HttpURLConnection setUpGetConnection(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(getRequestMethodEnum().toString());
        con.setRequestProperty("Content-Type", "application/json");
        if (needAuthentication()) {
            String auth = authenticate();
            con.setRequestProperty("Authorization", auth);
        }
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.connect();
        return con;
    }

    protected HttpURLConnection setUpPostConnection(URL url, String jsonInputString) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(getRequestMethodEnum().toString());
        con.setRequestProperty("Content-Type", "application/json");
        // todo: might need workaround for application/gzip for reports
        con.setRequestProperty("Accept", "application/json");
        if (needAuthentication()) {
            String auth = authenticate();
            con.setRequestProperty("Authorization", auth);
        }
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return con;
    }
}
