// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import com.daimler.sechub.integrationtest.internal.IntegrationTestDefaultProfiles;

/**
 * This class contains only some constants which are normally only available
 * from internal packages. So those constants can be accessed via API / tests.
 * 
 * @author Albert Tregnaghi
 *
 */
public class ExecutionConstants {

    /**
     * Id for default execution profile (integration test profile 1)) - please NEVER
     * CHANGE THIS PROFILE! Create your own execution profiles. This profile
     * contains executors of CHECKMARX, NESSUS and NETSPARKER in version V1. Those
     * executors do need their information by ENVIRONMENT variables. This is a fixed
     * setup. PlEASE DO NOT CHAGNE IT! We use this profile to execute old tests to
     * simulate old behaviour were no execution profiles nor executor configurations
     * were available - all of those scenarios (1-6) will have automatically this
     * profile assigned to projects!
     * 
     */
    public static final String DEFAULT_EXECUTION_PROFILE_ID = IntegrationTestDefaultProfiles.PROFILE_1.id;

}
