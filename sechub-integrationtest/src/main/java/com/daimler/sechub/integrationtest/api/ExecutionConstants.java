// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import com.daimler.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;

public class ExecutionConstants {

    /**
     * Id for default profile 1 - please NEVER CHANGE THIS PROFILE! Create your own execution
     * profiles. This profile contains executors of CHECKMARX, NESSUS and NETSPARKER in version V1.
     * Those executors do need their information by ENVIRONEMNT variables. This is a fix setup.
     * PlEASE DO NOT CHAGNE IT! We use this profile to execute old tests to simulate old behaviour
     * were no execution profiles nor executor configurations were available - all of those 
     * scenarios (1-6) will have automatically this profile assigned to projects!  
     * 
     */
    public static final String DEFAULT_PROFILE_1_ID = IntegrationTestDefaultProfiles.PROFILE_1.id;
    
    /**
     * Id for default profile 2 - please NEVER CHANGE THIS PROFILE! Create your own execution
     * profiles. This profile contains executors of PDS integration test in version V1.
     * 
     */
    public static final String DEFAULT_PROFILE_2_ID = IntegrationTestDefaultProfiles.PROFILE_2_PDS_CODESCAN.id;
    
    /**
     * Id for default profile 2 - please NEVER CHANGE THIS PROFILE! Create your own execution
     * profiles. This profile contains executors of PDS integration test in version V1, using SARIF
     * 
     */
    public static final String DEFAULT_PROFILE_3_ID = IntegrationTestDefaultProfiles.PROFILE_3_PDS_CODESCAN_SARIF.id;  
    
}
