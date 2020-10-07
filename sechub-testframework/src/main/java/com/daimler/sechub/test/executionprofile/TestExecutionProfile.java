package com.daimler.sechub.test.executionprofile;

import java.util.HashSet;
import java.util.Set;

import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

import wiremock.com.fasterxml.jackson.annotation.JsonInclude;
import wiremock.com.fasterxml.jackson.annotation.JsonInclude.Include;

public class TestExecutionProfile {

    public String id;
    
    @JsonInclude(Include.NON_NULL)
    public String description;
    
    public Set<TestExecutorConfig> configurations = new HashSet<>();
    
    public Set<String> projectIds = new HashSet<>();
    
    public boolean enabled;

}
