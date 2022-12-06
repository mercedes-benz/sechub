// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import com.mercedesbenz.sechub.integrationtest.scenario1.Scenario1;
import com.mercedesbenz.sechub.integrationtest.scenario10.Scenario10;
import com.mercedesbenz.sechub.integrationtest.scenario11.Scenario11;
import com.mercedesbenz.sechub.integrationtest.scenario12.Scenario12;
import com.mercedesbenz.sechub.integrationtest.scenario13.Scenario13;
import com.mercedesbenz.sechub.integrationtest.scenario14.Scenario14;
import com.mercedesbenz.sechub.integrationtest.scenario15.Scenario15;
import com.mercedesbenz.sechub.integrationtest.scenario16.Scenario16;
import com.mercedesbenz.sechub.integrationtest.scenario17.Scenario17;
import com.mercedesbenz.sechub.integrationtest.scenario18.Scenario18;
import com.mercedesbenz.sechub.integrationtest.scenario19.Scenario19;
import com.mercedesbenz.sechub.integrationtest.scenario2.Scenario2;
import com.mercedesbenz.sechub.integrationtest.scenario3.Scenario3;
import com.mercedesbenz.sechub.integrationtest.scenario4.Scenario4;
import com.mercedesbenz.sechub.integrationtest.scenario5.Scenario5;
import com.mercedesbenz.sechub.integrationtest.scenario6.Scenario6;
import com.mercedesbenz.sechub.integrationtest.scenario7.Scenario7;
import com.mercedesbenz.sechub.integrationtest.scenario8.Scenario8;
import com.mercedesbenz.sechub.integrationtest.scenario9.Scenario9;

/* @formatter:off */
/**
 *
 * For the SecHub integration tests we have test scenarios which do automatically provide some
 * setup.
 * <h2>Overview</h2>
 * <h3>Scenarios</h3>
 * <ul>
 * <li>{@link Scenario1 Scenario 1} - no initialized users or projects</li>
 * <li>{@link Scenario2 Scenario 2} - 2 projects, 2 users but not assigned to projects</li>
 * <li>{@link Scenario3 Scenario 3} - 3 projects, 2 users, 1 user already assigned to project PROJECT_1 which is assigned to {@link IntegrationTestDefaultProfiles#PROFILE_1 Profile 1}</li>
 * <li>{@link Scenario4 Scenario 4} - 1 project, 1 user as {@link StaticTestScenario static test data}. USER_1 is already assigned to project PROJECT_1 which is assigned to {@link IntegrationTestDefaultProfiles#PROFILE_1 Profile 1}</li>
 * <li>{@link Scenario5 Scenario 5} - 1 user assigned to 2 projects. One project has {@link IntegrationTestDefaultProfiles#PROFILE_2_PDS_CODESCAN Profile 2} (generic PDS code scan) and the other has {@link IntegrationTestDefaultProfiles#PROFILE_6_NO_STORAGE_REUSED__PDS_CODESCAN_PROCESS_EXEC_FAILS_EXITCODE_1 Profile 6} (always failing)</li>
 * <li>{@link Scenario6 Scenario 6} - This scenario is only for direct PDS communication without any SecHub data</li>
 * <li>{@link Scenario7 Scenario 7} - Special scenario for product executor configuration changes.</li>
 * <li>{@link Scenario8 Scenario 8} - Special scenario for product execution profile changes</li>
 * <li>{@link Scenario9 Scenario 9} - PDS scenario for SARIF code and web scan testing. 1 user, 1 project. Project is assigned to {@link IntegrationTestDefaultProfiles#PROFILE_3_PDS_CODESCAN_SARIF Profile 3} (PDS code scan SARIF) and {@link IntegrationTestDefaultProfiles#PROFILE_8_PDS_WEBSCAN_SARIF Profile 8} (PDS web scan SARIF)</li>
 * <li>{@link Scenario10 Scenario 10} - PDS scenario for SARIF code scan without storage reuse. 1 user, 1 project. Project is assigned to {@link IntegrationTestDefaultProfiles#PROFILE_4_NO_STORAGE_REUSED__PDS_CODESCAN_SARIF Profile 4} (PDS code scan SARIF - but no storage reused) </li>
 * <li>{@link Scenario11 Scenario 11} - Special PDS scenario to test message stream handling, 1 user, 1 project. Project is assigned to {@link IntegrationTestDefaultProfiles#PROFILE_5_PDS_CODESCAN_LAZY_STREAMS Profile 4} (PDS code scan with lazy streams) </li>
 * <li>{@link Scenario12 Scenario 12} - PDS scenario for generic web scan testing. 1 user, 1 project. Project is assigned to {@link IntegrationTestDefaultProfiles#PROFILE_7_PDS_WEBSCAN Profile 7} (PDS code scan SARIF) and {@link IntegrationTestDefaultProfiles#PROFILE_8_PDS_WEBSCAN_SARIF Profile 8} (PDS web scan generic)</li>
 * <li>{@link Scenario13 Scenario 13} - PDS scenario for SPDX license scan testing. 1 user, 1 project. Project is assigned to {@link IntegrationTestDefaultProfiles#PROFILE_7_PDS_WEBSCAN Profile 7} (PDS code scan SARIF) and {@link IntegrationTestDefaultProfiles#PROFILE_9_PDS_LICENSESCAN_SPDX Profile 9} (PDS license scan SPDX)</li>
 * <li>{@link Scenario14 Scenario 14} - PDS scenario for testing binary and source code data structure handling. 1 user, 1 project. Project is assigned to {@link IntegrationTestDefaultProfiles#PROFILE_2_PDS_CODESCAN Profile 2}</li>
 * <li>{@link Scenario15 Scenario 15} - PDS scenario for testing include and exclude file filtering. 1 user, 1 project. Project is assigned to {@link IntegrationTestDefaultProfiles#PROFILE_10_PDS_CODESCAN_INCLUDES_EXCLUDES Profile 10}</li>
 * <li>{@link Scenario16 Scenario 16} - PDS scenario for testing mapping. 1 user, 1 project. Project is assigned to {@link IntegrationTestDefaultProfiles#PROFILE_11_PDS_CODESCAN_MAPPING Profile 11}</li>
 * <li>{@link Scenario17 Scenario 17} - PDS scenario for testing PDS checkmarx wrapper. 1 user, 1 project. Project is assigned to {@link IntegrationTestDefaultProfiles#PROFILE_12_PDS_CHECKMARX_INTEGRATIONTEST Profile 12}</li>
 * <li>{@link Scenario18 Scenario 18} - PDS scenario for testing PDS cancellation. 1 user, 1 project. Project is assigned to {@link IntegrationTestDefaultProfiles#PROFILE_13_PDS_CANCELLATION Profile 13}</li>
 * <li>{@link Scenario19 Scenario 19} - Simple scenario for testing job information list fetching for user. 1 user, 1 project.</li>
 * </ul>
 * <b3>Profiles</h3>
 * <ul>
 * <li>{@link IntegrationTestDefaultProfiles#PROFILE_1 Profile 1} - Checkmarx, Nessus, Netsparker (mocked)</li>
 * <li>{@link IntegrationTestDefaultProfiles#PROFILE_2_PDS_CODESCAN Profile 2} - PDS code scan (generic)</li>
 * <li>{@link IntegrationTestDefaultProfiles#PROFILE_3_PDS_CODESCAN_SARIF Profile 3} - PDS code scan SARIF</li>
 * <li>{@link IntegrationTestDefaultProfiles#PROFILE_4_NO_STORAGE_REUSED__PDS_CODESCAN_SARIF Profile 4} - PDS code scan SARIF (storage NOT reused)</li>
 * <li>{@link IntegrationTestDefaultProfiles#PROFILE_5_PDS_CODESCAN_LAZY_STREAMS Profile 5} - PDS code scan (lazy message streams)</li>
 * <li>{@link IntegrationTestDefaultProfiles#PROFILE_6_NO_STORAGE_REUSED__PDS_CODESCAN_PROCESS_EXEC_FAILS_EXITCODE_1 Profile 6} - PDS code scan (failing)</li>
 * <li>{@link IntegrationTestDefaultProfiles#PROFILE_7_PDS_WEBSCAN Profile 7} - PDS web scan</li>
 * <li>{@link IntegrationTestDefaultProfiles#PROFILE_8_PDS_WEBSCAN_SARIF Profile 8} - PDS web scan SARIF</li>
 * <li>{@link IntegrationTestDefaultProfiles#PROFILE_9_PDS_LICENSESCAN_SPDX Profile 9} - PDS license scan SPDX</li>
 * <li>{@link IntegrationTestDefaultProfiles#PROFILE_10_PDS_CODESCAN_INCLUDES_EXCLUDES Profile 10} - PDS code scan with include and exclude settings</li>
 * <li>{@link IntegrationTestDefaultProfiles#PROFILE_11_PDS_CODESCAN_MAPPING Profile 11} - PDS code scan for testing sechub mapping reuse</li>
 * <li>{@link IntegrationTestDefaultProfiles#PROFILE_12_PDS_CHECKMARX_INTEGRATIONTEST Profile 12} - PDS code scan for checkmarx integration testing</li>
 * <li>{@link IntegrationTestDefaultProfiles#PROFILE_13_PDS_CANCELLATION Profile 13} - PDS code scan for cancellation roundtrip testing (sechub-pds-launcherscript-pds-sechub)</li>
 * </ul>
 * @author Albert Tregnaghi
 *
 */
/* @formatter:on*/
public class IntegrationTestDataOverview {

    /*
     * This class is only central point for javadoc documentaiton and linked between
     * other javadoc pages. The purpose is to have the possibility to jump between
     * the different javadoc locations inside IDE.
     */
}
