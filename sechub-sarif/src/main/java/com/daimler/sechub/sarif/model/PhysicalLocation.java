// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Physical location property. See <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317673">SARIF
 * 2.1.0 specification entry</a>
 * 
 * @author Albert Tregnaghi
 *
 */
@JsonPropertyOrder({ "artifactLocation" })
public class PhysicalLocation {
    private ArtifactLocation artifactLocation;
    private Region region;

    public PhysicalLocation() {
    }

    public PhysicalLocation(ArtifactLocation artifactLocation, Region region) {
        this.artifactLocation = artifactLocation;
        this.region = region;
    }

    public ArtifactLocation getArtifactLocation() {
        return artifactLocation;
    }

    public Region getRegion() {
        return region;
    }

    public void setArtifactLocation(ArtifactLocation artifactLocation) {
        this.artifactLocation = artifactLocation;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "PhysicalLocation [artifactLocation=" + artifactLocation + ", region=" + region + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactLocation, region);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PhysicalLocation other = (PhysicalLocation) obj;
        return Objects.equals(artifactLocation, other.artifactLocation) && Objects.equals(region, other.region);
    }
}
