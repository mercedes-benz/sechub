// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.metadata;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class SerecoWebRequest {

    String protocol;
    String version;
    String target;
    String method;

    Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    SerecoWebBody body = new SerecoWebBody();

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

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public SerecoWebBody getBody() {
        return body;
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, headers, method, protocol, target, version);
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
        SerecoWebRequest other = (SerecoWebRequest) obj;
        return Objects.equals(body, other.body) && Objects.equals(headers, other.headers) && Objects.equals(method, other.method)
                && Objects.equals(protocol, other.protocol) && Objects.equals(target, other.target) && Objects.equals(version, other.version);
    }

    @Override
    public String toString() {
        return "SerecoWebRequest [" + (protocol != null ? "\nprotocol=" + protocol + ", " : "") + (version != null ? "\nversion=" + version + ", " : "")
                + (target != null ? "\ntarget=" + target + ", " : "") + (method != null ? "\nmethod=" + method + ", " : "")
                + (headers != null ? "\nheaders=" + headers + ", " : "") + (body != null ? "\nbody=" + body : "") + "]";
    }

}