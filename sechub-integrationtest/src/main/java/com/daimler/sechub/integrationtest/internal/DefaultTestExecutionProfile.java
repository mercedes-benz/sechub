// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.test.executionprofile.TestExecutionProfile;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

/**
 * Do not change those profiles in tests!
 * @author Albert Tregnaghi
 *
 */
public class DefaultTestExecutionProfile extends TestExecutionProfile{
    
    static List<DefaultTestExecutionProfile> allDefaultTestExecutionProfiles=new ArrayList<>();
    
    

    /**
     * Contains all executor configuration entities for this profile (but at the beginning they 
     * have not the correct UUID) 
     */
    List<TestExecutorConfig> initialConfigurationsWithoutUUID = new ArrayList<>();  
    
    DefaultTestExecutionProfile(){
        allDefaultTestExecutionProfiles.add(this);
    }
}
