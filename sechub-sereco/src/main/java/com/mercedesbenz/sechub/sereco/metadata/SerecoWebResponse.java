// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.metadata;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class SerecoWebResponse {

    int statusCode;
    String reasonPhrase;
    String protocol;
    String version;
    boolean noResponseReceived;

    Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    SerecoWebBody body = new SerecoWebBody();

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

    public boolean isNoResponseReceived() {
        return noResponseReceived;
    }

    public void setNoResponseReceived(boolean noResponseReceived) {
        this.noResponseReceived = noResponseReceived;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public SerecoWebBody getBody() {
        return body;
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, headers, noResponseReceived, protocol, reasonPhrase, statusCode, version);
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
        SerecoWebResponse other = (SerecoWebResponse) obj;
        return Objects.equals(body, other.body) && Objects.equals(headers, other.headers) && noResponseReceived == other.noResponseReceived
                && Objects.equals(protocol, other.protocol) && Objects.equals(reasonPhrase, other.reasonPhrase) && statusCode == other.statusCode
                && Objects.equals(version, other.version);
    }

    @Override
    public String toString() {
        return "SerecoWebResponse [\nstatusCode=" + statusCode + ", " + (reasonPhrase != null ? "\nreasonPhrase=" + reasonPhrase + ", " : "")
                + (protocol != null ? "\nprotocol=" + protocol + ", " : "") + (version != null ? "\nversion=" + version + ", " : "") + "\nnoResponseReceived="
                + noResponseReceived + ", " + (headers != null ? "\nheaders=" + headers + ", " : "") + (body != null ? "\nbody=" + body : "") + "]";
    }

}