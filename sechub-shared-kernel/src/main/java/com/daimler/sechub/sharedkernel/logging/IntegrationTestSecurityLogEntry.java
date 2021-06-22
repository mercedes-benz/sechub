// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IntegrationTestSecurityLogEntry {
    
    public SecurityLogType type;
    public String message;
    public List<Object> objects = new ArrayList<>();

    @Override
    public int hashCode() {
        return Objects.hash(message, objects, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IntegrationTestSecurityLogEntry other = (IntegrationTestSecurityLogEntry) obj;
        return Objects.equals(message, other.message) && Objects.equals(objects, other.objects) && type == other.type;
    }

    @Override
    public String toString() {
        return "IntegrationTestSecurityLogEntry [" + (type != null ? "type=" + type + ", " : "") + (message != null ? "message=" + message + ", " : "")
                + (objects != null ? "objects=" + objects : "") + "]";
    }

}