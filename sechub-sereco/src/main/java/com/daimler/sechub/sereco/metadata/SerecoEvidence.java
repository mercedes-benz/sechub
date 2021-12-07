package com.daimler.sechub.sereco.metadata;

import java.util.Objects;

public class SerecoEvidence {
    String snippet;
    long bodyLocation;

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public long getBodyLocation() {
        return bodyLocation;
    }

    public void setBodyLocation(long bodyLocation) {
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
        SerecoEvidence other = (SerecoEvidence) obj;
        return bodyLocation == other.bodyLocation && Objects.equals(snippet, other.snippet);
    }
    
}