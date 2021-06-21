// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "physicalLocation" })
public class Location {
    private PhysicalLocation physicalLocation;

    public Location() {
    }

    public Location(PhysicalLocation physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    public PhysicalLocation getPhysicalLocation() {
        return physicalLocation;
    }

    public void setPhysicalLocation(PhysicalLocation physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    @Override
    public String toString() {
        return "Location [physicalLocation=" + physicalLocation + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(physicalLocation);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Location other = (Location) obj;
        return Objects.equals(physicalLocation, other.physicalLocation);
    }
}
