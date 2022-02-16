package com.daimler.sechub.sereco.metadata;

import java.util.Objects;

public class SerecoWebEvidence {

    String snippet;
    SerecoWebBodyLocation bodyLocation;

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public SerecoWebBodyLocation getBodyLocation() {
        return bodyLocation;
    }

    public void setBodyLocation(SerecoWebBodyLocation bodyLocation) {
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
        SerecoWebEvidence other = (SerecoWebEvidence) obj;
        return bodyLocation == other.bodyLocation && Objects.equals(snippet, other.snippet);
    }

    @Override
    public String toString() {
        return "SerecoWebEvidence [" + (snippet != null ? "snippet=" + snippet + ", " : "") + "bodyLocation=" + bodyLocation + "]";
    }

}