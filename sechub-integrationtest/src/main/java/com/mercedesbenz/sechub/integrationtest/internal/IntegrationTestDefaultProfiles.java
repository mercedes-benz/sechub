// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import java.util.List;

import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.integrationtest.scenario10.Scenario10;
import com.mercedesbenz.sechub.integrationtest.scenario11.Scenario11;
import com.mercedesbenz.sechub.integrationtest.scenario12.Scenario12;
import com.mercedesbenz.sechub.integrationtest.scenario13.Scenario13;
import com.mercedesbenz.sechub.integrationtest.scenario15.Scenario15;
import com.mercedesbenz.sechub.integrationtest.scenario16.Scenario16;
import com.mercedesbenz.sechub.integrationtest.scenario17.Scenario17;
import com.mercedesbenz.sechub.integrationtest.scenario18.Scenario18;
import com.mercedesbenz.sechub.integrationtest.scenario2.Scenario2;
import com.mercedesbenz.sechub.integrationtest.scenario21.Scenario21;
import com.mercedesbenz.sechub.integrationtest.scenario3.Scenario3;
import com.mercedesbenz.sechub.integrationtest.scenario4.Scenario4;
import com.mercedesbenz.sechub.integrationtest.scenario5.Scenario5;
import com.mercedesbenz.sechub.integrationtest.scenario9.Scenario9;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorConfig;

public class IntegrationTestDefaultProfiles {
    /**
     * <h3>Profile 1</h3>
     * <h4>Short description</h4> Standard profile for Nessus, Netsparker and
     * Checkmarx
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4> Standard profile for testing: Netsparker, Nessus and
     * Checkmarx are configured but no PDS configuration. Storage is reused. <br>
     * <br>
     * <h5>Contains configurations</h5>
     * <ul>
     * <li>{@link IntegrationTestDefaultExecutorConfigurations#CHECKMARX_V1
     * Checkmarx V1}</li>
     * <li>{@link IntegrationTestDefaultExecutorConfigurations#NETSPARKER_V1
     * Nesparker V1}</li>
     * <li>{@link IntegrationTestDefaultExecutorConfigurations#NESSUS_V1 Nessus
     * V1}</li>
     * </ul>
     *
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario2}</li>
     * <li>{@link Scenario3}</li>
     * <li>{@link Scenario4}</li>
     * </ul>
     *
     */
    public static final DefaultTestExecutionProfile PROFILE_1 = defineProfile1();

    /**
     * <h3>Profile 2</h3>
     * <h4>Short description</h4>PDS scan profile
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4> PDS scan profile, returns no real data but some dynamic text
     * messages - storage is reused <br>
     * <br>
     * <h5>Contains configurations</h5>
     * <ul>
     * <li>{@link IntegrationTestDefaultExecutorConfigurations#PDS_V1_CODE_SCAN_A
     * PDS code scan A}</li>
     * </ul>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario5}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_2_PDS_CODESCAN = defineProfile2();

    /**
     * <h3>Profile 3</h3>
     * <h4>Short description</h4>PDS scan profile for SARIF code scan, storage
     * reused
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4>
     *
     * PDS scan profile, returns code scan results in SARIF format - storage is
     * reused <br>
     * <br>
     * <h5>Contains configurations</h5>
     * <ul>
     * <li>{@link IntegrationTestDefaultExecutorConfigurations#PDS_V1_CODE_SCAN_D
     * PDS code scan D}</li>
     * </ul>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario9}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_3_PDS_CODESCAN_SARIF = defineProfile3();

    /**
     * <h3>Profile 4</h3>
     * <h4>Short description</h4>PDS scan profile for SARIF code scan
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4> PDS scan profile, returns code scan results in SARIF format
     * - storage is NOT reused <br>
     * <br>
     * <h5>Contains configurations</h5>
     * <ul>
     * <li>{@link IntegrationTestDefaultExecutorConfigurations#PDS_V1_CODE_SCAN_E_DO_NOT_REUSE_SECHUBDATA
     * PDS code scan E}</li>
     * </ul>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario10}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_4_NO_STORAGE_REUSED__PDS_CODESCAN_SARIF = defineProfile4();

    /**
     * <h3>Profile 5</h3>
     * <h4>Short description</h4>PDS scan profile code scan with lazy message
     * streams
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4> PDS scan profile, returns no real data but some dynamic text
     * messages - the output streams will be a little bit delayed. so full text from
     * streams will be only available after 1.5 seconds: <br>
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
     * storage will be reused <br>
     * <br>
     * <h5>Contains configurations</h5>
     * <ul>
     * <li>{@link IntegrationTestDefaultExecutorConfigurations#PDS_V1_CODE_SCAN_G_FAIL_EXIT_CODE_1
     * PDS code scan G}</li>
     * </ul>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario11}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_5_PDS_CODESCAN_LAZY_STREAMS = defineProfile5();

    /**
     * <h3>Profile 6</h3>
     * <h4>Short description</h4>PDS scan profile which does always fail
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4> PDS scan profile, will always return 1 from PDS execution
     * script 'integrationtest-codescan.sh' reused <br>
     * <br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario5}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_6_NO_STORAGE_REUSED__PDS_CODESCAN_PROCESS_EXEC_FAILS_EXITCODE_1 = defineProfile6();

    /**
     * <h3>Profile 7</h3>
     * <h4>Short description</h4>PDS scan profile for web scan
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4> PDS Web scan profile, returns no real data but some dynamic
     * text messages - storage is reused <br>
     * <br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario12}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_7_PDS_WEBSCAN = defineProfile7();

    /**
     * <h3>Profile 8</h3>
     * <h4>Short description</h4>PDS scan profile for OWASP Zap web scans
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4> PDS scan profile, returns web scan results in OWASP ZAP
     * SARIF format - storage is reused. It uses executor configuration
     * {@link IntegrationTestDefaultExecutorConfigurations#PDS_V1_WEB_SCAN_B_OWASP_SARIF_RESULTS}
     * <br>
     * <br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario9}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_8_PDS_WEBSCAN_SARIF = defineProfile8();

    /**
     * <h3>Profile 9</h3>
     * <h4>Short description</h4>PDS scan profile for license scans
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4><br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario13}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_9_PDS_LICENSESCAN_SPDX = defineProfile9();

    /**
     * <h3>Profile 10</h3>
     * <h4>Short description</h4>PDS scan profile code scan with include exclude
     * settings
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4> PDS scan profile, returns no real data but some dynamic text
     * messages - the executor configuration does contain some include and exclude
     * file filter options:<br>
     * <br>
     * <ul>
     * </ul>
     * <h5>Contains configurations</h5>
     * <ul>
     * <li>{@link IntegrationTestDefaultExecutorConfigurations#PDS_V1_CODE_SCAN_I_INCLUDES_EXCLUDES
     * PDS code scan I}</li>
     * </ul>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario15}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_10_PDS_CODESCAN_INCLUDES_EXCLUDES = defineProfile10();

    /**
     * The profile does use executor configuration
     * {@link IntegrationTestDefaultExecutorConfigurations#PDS_V1_CODE_SCAN_J_MAPPING
     * PDS_V1_CODE_SCAN_J_MAPPING}
     *
     * PDS job parameter
     * {@value PDSDefaultParameterKeyConstants#PARAM_KEY_PDS_CONFIG_USE_SECHUB_MAPPINGS}
     * does include:
     * <ul>
     * <li>{@link IntegrationTestExampleConstants#MAPPING_ID_1_REPLACE_ANY_PROJECT1}
     * ({@value IntegrationTestExampleConstants#MAPPING_ID_1_REPLACE_ANY_PROJECT1})</li>
     * <li>{@link IntegrationTestExampleConstants#MAPPING_ID_2_NOT_EXISTING_IN_SECHUB}
     * ({@value IntegrationTestExampleConstants#MAPPING_ID_2_NOT_EXISTING_IN_SECHUB})</li>
     * </ul>
     *
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario16}</li>
     * </ul>
     *
     */
    public static final DefaultTestExecutionProfile PROFILE_11_PDS_CODESCAN_MAPPING = defineProfile11();

    /**
     * The profile does use executor configuration
     * {@link IntegrationTestDefaultExecutorConfigurations#PDS_V1_CHECKMARX_INTEGRATIONTEST
     * PDS_V1_CHECKMARX_INTEGRATIONTEST}
     *
     * PDS job parameter
     * {@value PDSDefaultParameterKeyConstants#PARAM_KEY_PDS_CONFIG_USE_SECHUB_MAPPINGS}
     * does include:
     * <ul>
     * <li>{@link IntegrationTestExampleConstants#MAPPING_ID_1_REPLACE_ANY_PROJECT1}
     * ({@value IntegrationTestExampleConstants#MAPPING_ID_1_REPLACE_ANY_PROJECT1})</li>
     * <li>{@link IntegrationTestExampleConstants#MAPPING_ID_2_NOT_EXISTING_IN_SECHUB}
     * ({@value IntegrationTestExampleConstants#MAPPING_ID_2_NOT_EXISTING_IN_SECHUB})</li>
     * </ul>
     *
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario17}</li>
     * </ul>
     *
     */
    public static final DefaultTestExecutionProfile PROFILE_12_PDS_CHECKMARX_INTEGRATIONTEST = defineProfile12();

    /**
     * <h3>Profile 13</h3>
     * <h4>Short description</h4>PDS scan profile code scan for PDS cancellation by
     * SecHub
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4> PDS scan profile, which provides special script behavior for
     * PDS cancellation and cancel events: The used script variant does listen to
     * the cancel event insdie PDS job workspace. If such an event happens a user
     * message will be set and inform that the cancellation post processing has been
     * done by the script. <br>
     * <br>
     * This can be tested by an integration test and we can ensure the complete
     * process (sechub->pds->pdsscript->pds>sechub) has been done correctly. <br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario18}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_13_PDS_CANCELLATION = defineProfile13();

    /**
     * The profile is exactly the same as
     * {@link #PROFILE_12_PDS_CHECKMARX_INTEGRATIONTEST} with one exception:<br>
     * The used executor configuration will set job parameter
     * {@link PDSDefaultParameterKeyConstants#PARAM_KEY_PDS_CONFIG_SUPPORTED_DATATYPES}
     * to "source,binary" which does override the PDS sever configuration which is
     * "source" per default, because checkmarx cannot scan binaries. But for test
     * purposes we have this "wrong configured" entries. So we can check the
     * behavior.
     *
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario17}</li>
     * </ul>
     *
     */
    public static final DefaultTestExecutionProfile PROFILE_14_PDS_CHECKMARX_INTEGRATIONTEST_WRONG_WITH_SOURCE_AND_BINARY = defineProfile14();

    /**
     * The profile is exactly the same as
     * {@link #PROFILE_12_PDS_CHECKMARX_INTEGRATIONTEST} with one exception:<br>
     * The used executor configuration will define job parameter
     * {@link PDSDefaultParameterKeyConstants#PARAM_KEY_PDS_CONFIG_FILEFILTER_EXCLUDES}
     * to "*.txt" so every text file will be ignored.
     *
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario17}</li>
     * </ul>
     *
     */
    public static final DefaultTestExecutionProfile PROFILE_15_PDS_CHECKMARX_INTEGRATIONTEST_FILTERING_TEXTFILES = defineProfile15();

    /**
     * The profile enables a PDS analyzer, which will return CLOC data (JSON).
     *
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario17}</li>
     * </ul>
     *
     */
    public static final DefaultTestExecutionProfile PROFILE_16_PDS_ANALYZE_CLOC_OUTPUT = defineProfile16();

    // TODO prepare
    /**
     * The profile enables a PDS prepare.
     *
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * </ul>
     *
     */
    public static final DefaultTestExecutionProfile PROFILE_100_PDS_PREPARE = defineProfile100();

    /**
     * <h3>Profile 17</h3>
     * <h4>Short description</h4>PDS scan profile for secret scans
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4><br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario20}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_17_PDS_SECRETSCAN = defineProfile17();

    /**
     * <h3>Profile 18</h3>
     * <h4>Short description</h4>PDS scan profile for PDS solution mock code scan
     * (gosec)
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4><br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario21}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_18_PDS_SOLUTION_GOSEC_MOCKED = defineProfileForPdsSolutionMockMode(18,
            IntegrationTestDefaultExecutorConfigurations.PDS_V1_PDS_SOLUTION_GOSEC_MOCKED);

    /**
     * <h3>Profile 19</h3>
     * <h4>Short description</h4>PDS scan profile for PDS solution mock code scan
     * (checkmarx)
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4><br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario21}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_19_PDS_SOLUTION_CHECKMARX_MOCK_MODE = defineProfileForPdsSolutionMockMode(19,
            IntegrationTestDefaultExecutorConfigurations.PDS_V1_PDS_SOLUTION_CHECKMARX_MOCKED);

    /**
     * <h3>Profile 20</h3>
     * <h4>Short description</h4>PDS scan profile for PDS solution mock code scan
     * (bandit)
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4><br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario21}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_20_PDS_SOLUTION_MULTI_BANDIT_MOCKED = defineProfileForPdsSolutionMockMode(20,
            IntegrationTestDefaultExecutorConfigurations.PDS_V1_PDS_SOLUTION_MULTI_BANDIT_MOCKED);

    /**
     * <h3>Profile 21</h3>
     * <h4>Short description</h4>PDS scan profile for PDS solution mock web scan
     * (zap)
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4><br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario21}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_21_PDS_SOLUTION_ZAP_MOCKED = defineProfileForPdsSolutionMockMode(21,
            IntegrationTestDefaultExecutorConfigurations.PDS_V1_PDS_SOLUTION_ZAP_MOCKED);

    /**
     * <h3>Profile 22</h3>
     * <h4>Short description</h4>PDS scan profile for PDS solution mock license scan
     * (scancode spdx json)
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4><br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario21}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_22_PDS_SOLUTION_SCANCODE_SPDX_JSON_MOCKED = defineProfileForPdsSolutionMockMode(22,
            IntegrationTestDefaultExecutorConfigurations.PDS_V1_PDS_SOLUTION_SCANCODE_SPDX_JSON_MOCKED);

    /**
     * <h3>Profile 23</h3>
     * <h4>Short description</h4>PDS scan profile for PDS solution mock secret scan
     * (gitleaks)
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4><br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario21}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_23_PDS_SOLUTION_GITLEAKS_MOCKED = defineProfileForPdsSolutionMockMode(23,
            IntegrationTestDefaultExecutorConfigurations.PDS_V1_PDS_SOLUTION_GITLEAKS_MOCKED);

    /**
     * <h3>Profile 24</h3>
     * <h4>Short description</h4>PDS scan profile for PDS solution mock license scan
     * (tern)
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4><br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario21}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_24_PDS_SOLUTION_TERN_MOCKED = defineProfileForPdsSolutionMockMode(24,
            IntegrationTestDefaultExecutorConfigurations.PDS_V1_PDS_SOLUTION_TERN_MOCKED);

    /**
     * <h3>Profile 25</h3>
     * <h4>Short description</h4>PDS scan profile for PDS solution mock license scan
     * (xray spdx)
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4><br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario21}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_25_PDS_SOLUTION_XRAY_SPDX_MOCKED = defineProfileForPdsSolutionMockMode(25,
            IntegrationTestDefaultExecutorConfigurations.PDS_V1_PDS_SOLUTION_XRAY_SPDX_MOCKED);

    /**
     * <h3>Profile 26</h3>
     * <h4>Short description</h4>PDS scan profile for PDS solution mock license scan
     * (xray cyclonedx)
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4><br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario21}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_26_PDS_SOLUTION_XRAY_CYCLONEDX_MOCKED = defineProfileForPdsSolutionMockMode(26,
            IntegrationTestDefaultExecutorConfigurations.PDS_V1_PDS_SOLUTION_XRAY_CYCLONEDX_MOCKED);
    /**
     * <h3>Profile 27</h3>
     * <h4>Short description</h4>PDS scan profile for PDS solution mock code scan
     * (findsecuritybugs)
     *
     * <h4>Overview</h4> For an overview over all scenarios, look at
     * {@link com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestDataOverview
     * Overview}
     *
     * <h4>Details</h4><br>
     * <h5>Used inside scenarios:</h5>
     * <ul>
     * <li>{@link Scenario21}</li>
     * </ul>
     */
    public static final DefaultTestExecutionProfile PROFILE_27_PDS_SOLUTION_FINDSECURITYBUGS_MOCKED = defineProfileForPdsSolutionMockMode(27,
            IntegrationTestDefaultExecutorConfigurations.PDS_V1_PDS_SOLUTION_FINDSECURITYBUGS_MOCKED);


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

    private static DefaultTestExecutionProfile defineProfile10() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_I_INCLUDES_EXCLUDES);
        profile.id = "inttest-p10-pds-incl-exclude"; // not more than 30 chars per profile id, so we use this
        profile.description = "Profile 10: PDS, reused storage, dynamic text results, has include exclude filters:\nIncludes:"
                + IntegrationTestDefaultExecutorConfigurations.INCLUDES_1 + "\nExcludes:" + IntegrationTestDefaultExecutorConfigurations.EXCLUDES_1;
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile11() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_J_MAPPING);
        profile.id = "inttest-p11-pds-mapping"; // not more than 30 chars per profile id, so we use this
        profile.description = "Profile 11: PDS, reused storage, dynamic text results, uses predefined mapping: '"
                + IntegrationTestExampleConstants.MAPPING_ID_1_REPLACE_ANY_PROJECT1 + "'";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile12() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CHECKMARX_INTEGRATIONTEST);
        profile.id = "inttest-p12-pds-checkmarx";
        profile.description = "Profile 12: PDS checkmarx, reused storage, dynamic text results";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile13() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_K_CANCELLATION);
        profile.id = "inttest-p13-pds-cancellation";
        profile.description = "Profile 13: PDS, reused storage, dynamic text results, pds script waits for cancel event.";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile14() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID
                .add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CHECKMARX_INTEGRATIONTEST_WRONG_WITH_SOURCE_AND_BINARY);
        profile.id = "inttest-p14-pds-checkmarx";
        profile.description = "Profile 14: PDS checkmarx, reused storage, dynamic text results, but with source,binary as supported data type";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile15() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID
                .add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CHECKMARX_INTEGRATIONTEST_WITH_FILEFILTER_EXCLUDE_TEXTFILES);
        profile.id = "inttest-p15-pds-checkmarx";
        profile.description = "Profile 15: PDS checkmarx, reused storage, dynamic text results, with file filter exclusion of *.txt";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile16() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_ANALYZE_INTEGRATIONTEST_CLOC_JSON_1);
        profile.id = "inttest-p16-pds-analyze-cloc";
        profile.description = "Profile 16: PDS anaylze, reused storage, dynamic text results";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile17() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_SECRET_SCAN_A);
        profile.id = "inttest-p17-pds-secretscan";
        profile.description = "Profile 17: PDS secret scan, reused storage, SARIF JSON file returned";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfile100() {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_PREPARE_INTEGRATIONTEST);
        profile.id = "inttest-p100-pds-prepare";
        profile.description = "Profile 100: PDS prepare, reused storage, dynamic text results";
        profile.enabled = true;
        return profile;
    }

    private static DefaultTestExecutionProfile defineProfileForPdsSolutionMockMode(int number, TestExecutorConfig config) {

        DefaultTestExecutionProfile profile = new DefaultTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(config);
        profile.id = "inttest-p" + number + "-pds-solution";
        profile.description = "Profile " + number + ": PDS solution mock mode scan, reused storage, real product result returned";
        profile.enabled = true;
        return profile;
    }
}
