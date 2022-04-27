// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import java.util.List;

public class IntegrationTestDefaultProfiles {
    /**
     * Standard profile for testing: Netsparker, Nessus and Checkmarx are configured
     * but no PDS configuration. Storage is reused.
     */
    public static final DefaultTestExecutionProfile PROFILE_1 = defineProfile1();

    /**
     * PDS scan profile, returns no real data but some dynamic text messages -
     * storage is reused
     */
    public static final DefaultTestExecutionProfile PROFILE_2_PDS_CODESCAN = defineProfile2();

    /**
     * PDS scan profile, returns code scan results in SARIF format - storage is
     * reused
     */
    public static final DefaultTestExecutionProfile PROFILE_3_PDS_CODESCAN_SARIF = defineProfile3();

    /**
     * PDS scan profile, returns code scan results in SARIF format - storage is NOT
     * reused
     */
    public static final DefaultTestExecutionProfile PROFILE_4_NO_STORAGE_REUSED__PDS_CODESCAN_SARIF = defineProfile4();

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
    public static final DefaultTestExecutionProfile PROFILE_5_PDS_CODESCAN_LAZY_STREAMS = defineProfile5();

    /**
     * PDS scan profile, will always return 1 from PDS execution script
     * 'integrationtest-codescan.sh' reused
     */
    public static final DefaultTestExecutionProfile PROFILE_6_NO_STORAGE_REUSED__PDS_CODESCAN_PROCESS_EXEC_FAILS_EXITCODE_1 = defineProfile6();

    /**
     * PDS WEN scan profile, returns no real data but some dynamic text messages -
     * storage is reused
     */
    public static final DefaultTestExecutionProfile PROFILE_7_PDS_WEBSCAN = defineProfile7();

    /**
     * PDS scan profile, returns web scan results in OWASP ZAP SARIF format -
     * storage is reused. It uses executor configuration
     * {@link IntegrationTestDefaultExecutorConfigurations#PDS_V1_WEB_SCAN_B_OWASP_SARIF_RESULTS}
     */
    public static final DefaultTestExecutionProfile PROFILE_8_PDS_WEBSCAN_SARIF = defineProfile8();

    public static final DefaultTestExecutionProfile PROFILE_9_PDS_LICENSESCAN_SPDX = defineProfile9();

    /**
     * @return all default profiles
     */
    public static List<DefaultTestExecutionProfile> getAllDefaultProfiles() {
        return DefaultTestExecutionProfile.allDefaultTestExecutionProfiles;
    }

    private static DefaultTestExecutionProfile defineProfile1() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.CHECKMARX_V1);
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.NETSPARKER_V1);
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.NESSUS_V1);
        profile.id = "inttest-p1";
        profile.description = "Profile 1: Checkmarx, Netsparker, Nessus. Reused storage";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile2() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_A);
        profile.id = "inttest-p2-pds";
        profile.description = "Profile 2: PDS, reused storage, dynamic text results";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile3() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_D);
        profile.id = "inttest-p3-sarif"; // not more than 30 chars per profile id, so we use this
        profile.description = "Profile 3: PDS, reused storage, will return SARIF results";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile4() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_E_DO_NOT_REUSE_SECHUBDATA);
        profile.id = "inttest-p4-sarif"; // not more than 30 chars per profile id, so we use this
        profile.description = "Same as profile 3, but executor config does not reuse sechub storage!";
        profile.description = "Profile 4: PDS, does NOT reuse storage, will return SARIF results";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile5() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_F);
        profile.id = "inttest-p5-pds-lazy-output"; // not more than 30 chars per profile id, so we use this
        profile.description = "Profile 5: PDS, reused storage, will return dynamic text results, output streams will be lazy, run is 1500 ms";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile6() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_G_FAIL_EXIT_CODE_1);
        profile.id = "inttest-p6-fail-call"; // not more than 30 chars per profile id, so we use this
        profile.description = "Profile 6: PDS, reused storage, will return nothing because of exit 1 in script";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile7() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_WEB_SCAN_A);
        profile.id = "inttest-p7-pds-webscan";
        profile.description = "Profile 7: PDS webscan, reused storage, dynamic text results";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile8() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_WEB_SCAN_B_OWASP_SARIF_RESULTS);
        profile.id = "inttest-p8-pds-webscan";
        profile.description = "Profile 8: PDS webscan, reused storage, OWASP ZAP sarif file returned";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile9() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_LICENSE_SCAN_A);
        profile.id = "inttest-p9-pds-licensescan";
        profile.description = "Profile 9: PDS license scan, reused storage, SPDX JSON file returned";
        profile.enabled = true;
        return profile;
    }
}
