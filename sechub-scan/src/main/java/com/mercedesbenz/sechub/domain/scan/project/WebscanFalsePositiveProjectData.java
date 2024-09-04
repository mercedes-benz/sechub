// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import java.util.List;
import java.util.Objects;

public class WebscanFalsePositiveProjectData implements ProjectData {

    public static final String PROPERTY_CWEID = "cweId";
    public static final String PROPERTY_URLPATTERN = "urlPattern";
    public static final String PROPERTY_METHODS = "methods";

    private int cweId;
    private String urlPattern;
    private List<String> methods;

    public int getCweId() {
        return cweId;
    }

    public void setCweId(int cweId) {
        this.cweId = cweId;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPatterns) {
        this.urlPattern = urlPatterns;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "WebscanFalsePositiveProjectData [cweId=" + cweId + ", urlPatterns=" + urlPattern + ", methods=" + methods + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(cweId, methods, urlPattern);
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
        return Objects.equals(cweId, other.cweId) && Objects.equals(methods, other.methods) && Objects.equals(urlPattern, other.urlPattern);
    }

}
