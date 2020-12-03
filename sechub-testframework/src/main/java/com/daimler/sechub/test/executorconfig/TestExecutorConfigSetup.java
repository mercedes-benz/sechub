// SPDX-License-Identifier: MIT
package com.daimler.sechub.test.executorconfig;

import java.util.ArrayList;
import java.util.List;


public class TestExecutorConfigSetup {

    public String baseURL;
    
    public TestCredentials credentials = new TestCredentials();
    
    public List<TestExecutorSetupJobParam> jobParameters  = new ArrayList<>();
}
