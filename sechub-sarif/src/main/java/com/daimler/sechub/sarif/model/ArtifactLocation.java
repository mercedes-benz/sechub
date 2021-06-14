package com.daimler.sechub.sarif.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "uri", "uriBaseId" })
public class ArtifactLocation {
    private String uri;
    private String uriBaseId;

    public ArtifactLocation() {
    }

    public ArtifactLocation(String uriBaseId, String uri) {
        this.uriBaseId = uriBaseId;
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUriBaseId() {
        return uriBaseId;
    }

    public void setUriBaseId(String uriBaseId) {
        this.uriBaseId = uriBaseId;
    }

    @Override
    public String toString() {
        return "ArtifactLocation [uri=" + uri + ", uriBaseId=" + uriBaseId + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ArtifactLocation)) {
            return false;
        }
        ArtifactLocation other = (ArtifactLocation) obj;
        return Objects.equals(uri, other.uri) && Objects.equals(uriBaseId, other.uriBaseId);
    }
}
