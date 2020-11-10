// SPDX-License-Identifier: MIT
package com.daimler.sechub.test.executionprofile;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.daimler.sechub.test.executorconfig.TestExecutorConfig;


public class TestExecutionProfile {

    public String id;
    
    public String description;
    
    public Set<TestExecutorConfig> configurations = new HashSet<>();
    
    public Set<String> projectIds = new HashSet<>();
    
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
        TestExecutionProfile other = (TestExecutionProfile) obj;
        return Objects.equals(id, other.id);
    }

}
