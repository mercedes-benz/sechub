// SPDX-License-Identifier: MIT
package com.daimler.sechub.test.executorconfig;

import java.util.Objects;
import java.util.UUID;

/**
 * This is our test implementation which is parsed in and and to JSON. So we need to be "in sync"
 * with the origin class `ProductExecutorConfig` !
 * @author Albert Tregnaghi
 *
 */
public class TestExecutorConfig {

    public TestExecutorConfig(UUID uuid) {
        this.uuid=uuid;
    }
    public TestExecutorConfig() {
        /* just do nothing */
    }

    public UUID uuid;
    public String name;
    public String productIdentifier;
    public int executorVersion;
    public boolean enabled;
    
    public TestExecutorConfigSetup setup = new TestExecutorConfigSetup();

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
        TestExecutorConfig other = (TestExecutorConfig) obj;
        return Objects.equals(uuid, other.uuid);
    }
}
