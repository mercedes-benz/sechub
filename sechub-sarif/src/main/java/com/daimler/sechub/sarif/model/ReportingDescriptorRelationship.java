// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.Objects;

/**
 * 
 * Reporting descriptor relationship, see <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317870">SARIF
 * 2.1.0 specification entry</a>
 * 
 * @author Albert Tregnaghi
 *
 */
public class ReportingDescriptorRelationship {

    private ReportingDescriptorReference target;

    public ReportingDescriptorReference getTarget() {
        return target;
    }

    public void setTarget(ReportingDescriptorReference target) {
        this.target = target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(target);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReportingDescriptorRelationship other = (ReportingDescriptorRelationship) obj;
        return Objects.equals(target, other.target);
    }

}
