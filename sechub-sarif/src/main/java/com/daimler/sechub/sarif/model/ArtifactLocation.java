// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Location of an artifact, look at see <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317613">SARIF
 * 2.1.0 specification entry</a>
 * 
 * @author Albert Tregnaghi
 *
 */
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
    public int hashCode() {
        return Objects.hash(uri, uriBaseId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ArtifactLocation other = (ArtifactLocation) obj;
        return Objects.equals(uri, other.uri) && Objects.equals(uriBaseId, other.uriBaseId);
    }
}
