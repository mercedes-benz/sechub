// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.web;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class SecHubReportWebResponse {

    private int statusCode;
    private String reasonPhrase;
    private String protocol;
    private String version;

    private Map<String, String> headers = new TreeMap<>();

    private SecHubReportWebBody body = new SecHubReportWebBody();

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

    public SecHubReportWebBody getBody() {
        return body;
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, headers, protocol, reasonPhrase, statusCode, version);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SecHubReportWebResponse other = (SecHubReportWebResponse) obj;
        return Objects.equals(body, other.body) && Objects.equals(headers, other.headers) && Objects.equals(protocol, other.protocol)
                && Objects.equals(reasonPhrase, other.reasonPhrase) && statusCode == other.statusCode && Objects.equals(version, other.version);
    }

    @Override
    public String toString() {
        return "SecHubReportWebResponse [\nstatusCode=" + statusCode + ", " + (reasonPhrase != null ? "\nreasonPhrase=" + reasonPhrase + ", " : "")
                + (protocol != null ? "\nprotocol=" + protocol + ", " : "") + (version != null ? "\nversion=" + version + ", " : "") + ", "
                + (headers != null ? "\nheaders=" + headers + ", " : "") + (body != null ? "\nbody=" + body : "") + "]";
    }

}