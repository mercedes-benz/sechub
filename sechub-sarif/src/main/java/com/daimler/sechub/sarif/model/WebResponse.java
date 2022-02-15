package com.daimler.sechub.sarif.model;

import java.util.Map;
import java.util.TreeMap;

public class WebResponse extends SarifObject {

    private int statusCode;
    private String reasonPhrase;

    private String protocol;
    private String version;

    private boolean noResponseReceived;

    private Map<String, String> headers = new TreeMap<>();

    private Body body = new Body();

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public void setNoResponseReceived(boolean noResponseReceived) {
        this.noResponseReceived = noResponseReceived;
    }

    public boolean isNoResponseReceived() {
        return noResponseReceived;
    }

}
