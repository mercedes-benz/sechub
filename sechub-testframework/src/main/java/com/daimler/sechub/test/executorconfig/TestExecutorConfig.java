package com.daimler.sechub.test.executorconfig;

import java.util.UUID;

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
}
