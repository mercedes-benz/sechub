// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import java.util.List;
import java.util.Objects;

public class WebscanFalsePositiveProjectData implements ProjectData {

    public static final String PROPERTY_CWEID = "cweId";
    public static final String PROPERTY_PORTS = "ports";
    public static final String PROPERTY_PROTOCOLS = "protocols";
    public static final String PROPERTY_URLPATHPATTERNS = "urlPathPatterns";
    public static final String PROPERTY_HOSTPATTERNS = "hostPatterns";
    public static final String PROPERTY_METHODS = "methods";

    private Integer cweId;
    private List<String> ports;
    private List<String> protocols;
    private List<String> urlPathPatterns;
    private List<String> hostPatterns;
    private List<String> methods;

    public Integer getCweId() {
        return cweId;
    }

    public void setCweId(Integer cweId) {
        this.cweId = cweId;
    }

    public List<String> getPorts() {
        return ports;
    }

    public void setPorts(List<String> ports) {
        this.ports = ports;
    }

    public List<String> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<String> protocols) {
        this.protocols = protocols;
    }

    public List<String> getUrlPathPatterns() {
        return urlPathPatterns;
    }

    public void setUrlPathPatterns(List<String> urlPathPatterns) {
        this.urlPathPatterns = urlPathPatterns;
    }

    public List<String> getHostPatterns() {
        return hostPatterns;
    }

    public void setHostPatterns(List<String> hostPatterns) {
        this.hostPatterns = hostPatterns;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "WebscanFalsePositiveProjectData [cweId=" + cweId + ", ports=" + ports + ", protocols=" + protocols + ", urlPatterns=" + urlPathPatterns
                + ", servers=" + hostPatterns + ", methods=" + methods + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(cweId, methods, ports, protocols, hostPatterns, urlPathPatterns);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WebscanFalsePositiveProjectData other = (WebscanFalsePositiveProjectData) obj;
        return Objects.equals(cweId, other.cweId) && Objects.equals(methods, other.methods) && Objects.equals(ports, other.ports)
                && Objects.equals(protocols, other.protocols) && Objects.equals(hostPatterns, other.hostPatterns)
                && Objects.equals(urlPathPatterns, other.urlPathPatterns);
    }

}
