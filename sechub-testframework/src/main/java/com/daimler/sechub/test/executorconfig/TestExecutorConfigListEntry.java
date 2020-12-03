// SPDX-License-Identifier: MIT
package com.daimler.sechub.test.executorconfig;

import java.util.Objects;
import java.util.UUID;

public class TestExecutorConfigListEntry {
    
    public UUID uuid;
    public String name;
    public Boolean enabled;
    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestExecutorConfigListEntry other = (TestExecutorConfigListEntry) obj;
        return Objects.equals(uuid, other.uuid);
    }

}
