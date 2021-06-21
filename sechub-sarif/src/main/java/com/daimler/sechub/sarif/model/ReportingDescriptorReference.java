// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.Objects;

/**
 * Reporting descriptor reference, see <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317862">SARIF
 * 2.1.0 specification entry</a>
 * 
 * @author Albert Tregnaghi
 *
 */
public class ReportingDescriptorReference {

    private String id;
    private String guid;
    private ToolComponentReference toolComponent;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317869
     * 
     * @return toolcomponent reference
     */
    public ToolComponentReference getToolComponent() {
        return toolComponent;
    }

    public void setToolComponent(ToolComponentReference toolComponent) {
        this.toolComponent = toolComponent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(guid, id, toolComponent);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReportingDescriptorReference other = (ReportingDescriptorReference) obj;
        return Objects.equals(guid, other.guid) && Objects.equals(id, other.id) && Objects.equals(toolComponent, other.toolComponent);
    }

}
