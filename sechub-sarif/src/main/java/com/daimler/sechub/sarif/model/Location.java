// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents a location object. See see <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317670">SARIF
 * 2.1.0 specification entry</a>
 * 
 * @author Albert Tregnaghi
 *
 */
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
