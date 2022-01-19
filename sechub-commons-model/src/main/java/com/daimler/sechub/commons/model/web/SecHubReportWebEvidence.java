package com.daimler.sechub.commons.model.web;

import java.util.Objects;

public class SecHubReportWebEvidence {
    
    String snippet;
    SecHubReportWebBodyLocation bodyLocation;

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public SecHubReportWebBodyLocation getBodyLocation() {
        return bodyLocation;
    }

    public void setBodyLocation(SecHubReportWebBodyLocation bodyLocation) {
        this.bodyLocation = bodyLocation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bodyLocation, snippet);
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
        SecHubReportWebEvidence other = (SecHubReportWebEvidence) obj;
        return bodyLocation == other.bodyLocation && Objects.equals(snippet, other.snippet);
    }

    @Override
    public String toString() {
        return "SecHubReportWebEvidence [" + (snippet != null ? "snippet=" + snippet + ", " : "") + "bodyLocation=" + bodyLocation + "]";
    }
    
    
    
}