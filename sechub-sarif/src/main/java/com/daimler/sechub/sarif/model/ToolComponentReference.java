// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.Objects;

/**
 * Tool component reference object, see <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317875">SARIF
 * 2.1.0 specification entry</a>
 * 
 * 
 * @author Albert Tregnaghi
 *
 */
public class ToolComponentReference {

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
        ToolComponentReference other = (ToolComponentReference) obj;
        return Objects.equals(guid, other.guid) && Objects.equals(name, other.name);
    }
}
