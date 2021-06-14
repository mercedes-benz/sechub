package com.daimler.sechub.sarif.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PhysicalLocation)) {
            return false;
        }
        PhysicalLocation other = (PhysicalLocation) obj;
        return Objects.equals(artifactLocation, other.artifactLocation) && Objects.equals(region, other.region);
    }
}
