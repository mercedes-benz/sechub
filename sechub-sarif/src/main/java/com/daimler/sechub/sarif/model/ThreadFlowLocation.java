// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.Objects;

public class ThreadFlowLocation {

    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ThreadFlowLocation other = (ThreadFlowLocation) obj;
        return Objects.equals(location, other.location);
    }
}
