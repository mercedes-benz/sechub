// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

public class IntegrationTestDefaultProfiles {
    /**
     * Standard profile for testing: Netsparker, Nessus and Checkmarx are configured
     * but no PDS configuration. Storage is reused.
     */
    public static final DoNotChangeTestExecutionProfile PROFILE_1 = defineProfile1();

    /**
     * PDS scan profile, returns no real data but some dynamic text messages -
     * storage is reused
     */
    public static final DoNotChangeTestExecutionProfile PROFILE_2_PDS_CODESCAN = defineProfile2();

    /**
     * PDS scan profile, returns code scan results in SARIF format - storage is
     * reused
     */
    public static final DoNotChangeTestExecutionProfile PROFILE_3_PDS_CODESCAN_SARIF = defineProfile3();

    /**
     * PDS scan profile, returns code scan results in SARIF format - storage is NOT
     * reused
     */
    public static final DoNotChangeTestExecutionProfile PROFILE_4_NO_STORAGE_REUSED__PDS_CODESCAN_SARIF = defineProfile4();

    /**
     * PDS scan profile, returns no real data but some dynamic text messages - the
     * output streams will be a little bit delayed. so full text from streams will
     * be only available after 1.5 seconds: <br>
     * <br>
     * <ul>
     * <li>On startup: ending with STARTED (output stream), NO-PROBLEMS (error
     * stream)</li>
     * <li>After 500 ms: ending with WORKING1 (output stream), ERRORS1(error
     * stream)</li>
     * <li>After 1000 ms ms: ending with WORKING2 (output stream), ERRORS2(error
     * stream)</li>
     * <li>After 1500 ms ms: ending with WORKING3 (output stream), ERRORS3(error
     * stream)</li>
     * </ul>
     * The text will contain the given data at the end of the output streams. Before
     * we will have some additional text content to simulate larger outputs. <br>
     * storage will be reused
     */
    public static final DoNotChangeTestExecutionProfile PROFILE_5_PDS_CODESCAN_LAZY_STREAMS = defineProfile5();

    /**
     * PDS scan profile, will always return 1 from PDS execution script 'integrationtest-codescan.sh'
     * reused
     */
    public static final DoNotChangeTestExecutionProfile PROFILE_6_NO_STORAGE_REUSED__PDS_CODESCAN_PROCESS_EXEC_FAILS_EXITCODE_1 = defineProfile6();

    
    private static final DoNotChangeTestExecutionProfile[] ALL_PROFILES = new DoNotChangeTestExecutionProfile[] {

            PROFILE_1,

            PROFILE_2_PDS_CODESCAN,

            PROFILE_3_PDS_CODESCAN_SARIF,

            PROFILE_4_NO_STORAGE_REUSED__PDS_CODESCAN_SARIF,

            PROFILE_5_PDS_CODESCAN_LAZY_STREAMS,
            
            PROFILE_6_NO_STORAGE_REUSED__PDS_CODESCAN_PROCESS_EXEC_FAILS_EXITCODE_1

    };

    public static DoNotChangeTestExecutionProfile[] getAllDefaultProfiles() {
        return ALL_PROFILES;
    }

    private static DoNotChangeTestExecutionProfile defineProfile1() {

        DoNotChangeTestExecutionProfile profile = new DoNotChangeTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.CHECKMARX_V1);
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.NETSPARKER_V1);
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.NESSUS_V1);
        profile.id = "inttest-p1";
        profile.description = "Profile 1: Checkmarx, Netsparker, Nessus. Reused storage";
        profile.enabled = true;
        return profile;
    }

    private static DoNotChangeTestExecutionProfile defineProfile2() {

        DoNotChangeTestExecutionProfile profile = new DoNotChangeTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_A);
        profile.id = "inttest-p2-pds";
        profile.description = "Profile 2: PDS, reused storage, dynamic text results";
        profile.enabled = true;
        return profile;
    }

    private static DoNotChangeTestExecutionProfile defineProfile3() {

        DoNotChangeTestExecutionProfile profile = new DoNotChangeTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_D);
        profile.id = "inttest-p3-sarif"; // not more than 30 chars per profile id, so we use this
        profile.description = "Profile 3: PDS, reused storage, will return SARIF results";
        profile.enabled = true;
        return profile;
    }

    private static DoNotChangeTestExecutionProfile defineProfile4() {

        DoNotChangeTestExecutionProfile profile = new DoNotChangeTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_E_DO_NOT_REUSE_SECHUBDATA);
        profile.id = "inttest-p4-sarif"; // not more than 30 chars per profile id, so we use this
        profile.description = "Same as profile 3, but executor config does not reuse sechub storage!";
        profile.description = "Profile 4: PDS, does NOT reuse storage, will return SARIF results";
        profile.enabled = true;
        return profile;
    }
    
    private static DoNotChangeTestExecutionProfile defineProfile5() {

        DoNotChangeTestExecutionProfile profile = new DoNotChangeTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_F);
        profile.id = "inttest-p5-pds-lazy-output"; // not more than 30 chars per profile id, so we use this
        profile.description = "Profile 5: PDS, reused storage, will return dynamic text results, output streams will be lazy, run is 1500 ms";
        profile.enabled = true;
        return profile;
    }
    
    private static DoNotChangeTestExecutionProfile defineProfile6() {
        
        DoNotChangeTestExecutionProfile profile = new DoNotChangeTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_G_FAIL_EXIT_CODE_1);
        profile.id = "inttest-p6-fail-call"; // not more than 30 chars per profile id, so we use this
        profile.description = "Profile 6: PDS, reused storage, will return nothing because of exit 1 in script";
        profile.enabled = true;
        return profile;
    }

}
