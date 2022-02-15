package com.daimler.sechub.commons.model.web;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class SecHubReportWebRequest {

    private String protocol;
    private String version;
    private String target;
    private String method;

    private Map<String, String> headers = new TreeMap<>();

    private SecHubReportWebBody body = new SecHubReportWebBody();

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

    public SecHubReportWebBody getBody() {
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
        SecHubReportWebRequest other = (SecHubReportWebRequest) obj;
        return Objects.equals(body, other.body) && Objects.equals(headers, other.headers) && Objects.equals(method, other.method)
                && Objects.equals(protocol, other.protocol) && Objects.equals(target, other.target) && Objects.equals(version, other.version);
    }

    @Override
    public String toString() {
        return "SecHubReportWebRequest [" + (protocol != null ? "\nprotocol=" + protocol + ", " : "") + (version != null ? "\nversion=" + version + ", " : "")
                + (target != null ? "\ntarget=" + target + ", " : "") + (method != null ? "\nmethod=" + method + ", " : "")
                + (headers != null ? "\nheaders=" + headers + ", " : "") + (body != null ? "\nbody=" + body : "") + "]";
    }

}