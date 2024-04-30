// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.metadata;

import java.util.Objects;

public class SerecoRevisionData {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String revisionId) {
        this.id = revisionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SerecoRevisionData other = (SerecoRevisionData) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return "SerecoRevisionData [" + (id != null ? "id=" + id : "") + "]";
    }
}
