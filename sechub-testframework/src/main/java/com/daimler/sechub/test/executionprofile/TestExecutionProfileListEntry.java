// SPDX-License-Identifier: MIT
package com.daimler.sechub.test.executionprofile;

import java.util.Objects;

public class TestExecutionProfileListEntry {
    
    public String id;
    
    public String description;
    
    public boolean enabled;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestExecutionProfileListEntry other = (TestExecutionProfileListEntry) obj;
        return Objects.equals(id, other.id);
    }
    

}
