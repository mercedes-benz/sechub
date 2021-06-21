// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.Objects;

public class ToolComponent {

    private String name;
    private String guid;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getGuid() {
        return guid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(guid, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ToolComponent other = (ToolComponent) obj;
        return Objects.equals(guid, other.guid) && Objects.equals(name, other.name);
    }
}
