package com.daimler.sechub.test.executionprofile;

import java.util.HashSet;
import java.util.Set;

import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

public class TestExecutionProfile {
    
    public String id;
    
    public String description;
    
    public Set<TestExecutorConfig> configurations = new HashSet<>();
    
    public Set<String> projectIds = new HashSet<>();
    
    public boolean enabled;

}
