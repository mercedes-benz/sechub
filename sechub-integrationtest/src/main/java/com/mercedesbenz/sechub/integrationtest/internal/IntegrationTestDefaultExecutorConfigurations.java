// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants.*;
import static com.mercedesbenz.sechub.domain.scan.product.pds.PDSProductExecutorKeyConstants.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestProductExecutorIdentifier.*;
import static com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperKeyConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mercedesbenz.sechub.adapter.AdapterConfigBuilder;
import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.commons.mapping.MappingEntry;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.integrationtest.api.PDSIntTestProductIdentifier;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.TestProductExecutorIdentifier;
import com.mercedesbenz.sechub.test.PDSTestURLBuilder;
import com.mercedesbenz.sechub.test.TestPortProvider;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorConfig;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorSetupJobParam;

public class IntegrationTestDefaultExecutorConfigurations {

    public static final String INCLUDES_1 = "*included-folder/*";

    public static final String EXCLUDES_1 = "*excluded-folder/*, *excluded*.txt";

    public static final MappingEntry CHECKMARX_PRESETID_MAPPING_DEFAULT_MAPPING = new MappingEntry(".*", "4711", "A default presetId for integration tests");

    public static final MappingEntry CHECKMARX_TEAMID_MAPPING_DEFAULT_MAPPING = new MappingEntry(".*", "checkmarx-newproject-teamid",
            "our default team id for new checkmarx projects in integration tests");

    public static final String VALUE_PRODUCT_LEVEL = "42";

    private static final List<TestExecutorConfig> registeredConfigurations = new ArrayList<>();

    private enum StorageType {
        REUSE_SECHUB_DATA(),

        DO_NOT_REUSE_SECHUB_DATA,

        ;

    }

    private static final String INTTEST_NAME_PREFIX = "IT_";

    public static final TestExecutorConfig NETSPARKER_V1 = defineNetsparkerConfig();
    public static final TestExecutorConfig CHECKMARX_V1 = defineCheckmarxConfig();
    public static final TestExecutorConfig NESSUS_V1 = defineNessusConfig();

    public static final String PDS_CODESCAN_VARIANT_A = "a";
    public static final String PDS_CODESCAN_VARIANT_B = "b";
    public static final String PDS_CODESCAN_VARIANT_C = "b";
    public static final String PDS_CODESCAN_VARIANT_D = "d";
    public static final String PDS_CODESCAN_VARIANT_E = "e";
    public static final String PDS_CODESCAN_VARIANT_F = "f";
    public static final String PDS_CODESCAN_VARIANT_G = "g";
    public static final String PDS_CODESCAN_VARIANT_I = "i";
    public static final String PDS_CODESCAN_VARIANT_J = "j";
    public static final String PDS_CODESCAN_VARIANT_K = "k";

    public static final String PDS_WEBSCAN_VARIANT_A = "a";
    public static final String PDS_WEBSCAN_VARIANT_B = "b";

    public static final String PDS_PREPARE_VARIANT_A = "a";
    public static final String PDS_PREPARE_VARIANT_B = "b";
    public static final String PDS_PREPARE_VARIANT_C = "c";

    public static final String PDS_LICENSESCAN_VARIANT_A = "a";

    public static final String PDS_SECRETSCAN_VARIANT_A = "a";

    /* @formatter:off */
    public static final TestExecutorConfig PDS_V1_CODE_SCAN_A = definePDSScan(
                                                PDS_CODESCAN_VARIANT_A,false,
                                                PDSIntTestProductIdentifier.PDS_INTTEST_CODESCAN,
                                                StorageType.REUSE_SECHUB_DATA,
                                                PDS_CODESCAN, defineScriptTrustAllCertificatesJobParameter(true));

    public static final TestExecutorConfig PDS_V1_CODE_SCAN_B = definePDSScan(
                                                PDS_CODESCAN_VARIANT_B,true,
                                                PDSIntTestProductIdentifier.PDS_INTTEST_CODESCAN,
                                                StorageType.REUSE_SECHUB_DATA,
                                                PDS_CODESCAN);

    public static final TestExecutorConfig PDS_V1_CODE_SCAN_C = definePDSScan(
                                                PDS_CODESCAN_VARIANT_C,true,
                                                null, // no product identifier - so will fail
                                                StorageType.REUSE_SECHUB_DATA,
                                                PDS_CODESCAN);

    public static final TestExecutorConfig PDS_V1_CODE_SCAN_D = definePDSScan(
                                                PDS_CODESCAN_VARIANT_D,false,PDSIntTestProductIdentifier.
                                                PDS_INTTEST_PRODUCT_CS_SARIF,
                                                StorageType.REUSE_SECHUB_DATA,
                                                PDS_CODESCAN,defineScriptTrustAllCertificatesJobParameter(false));

    public static final TestExecutorConfig PDS_V1_CODE_SCAN_E_DO_NOT_REUSE_SECHUBDATA = definePDSScan(
                                                PDS_CODESCAN_VARIANT_E,false,
                                                PDSIntTestProductIdentifier.PDS_INTTEST_PRODUCT_CS_SARIF,
                                                StorageType.DO_NOT_REUSE_SECHUB_DATA,
                                                PDS_CODESCAN);

    public static final TestExecutorConfig PDS_V1_CODE_SCAN_F = definePDSScan(
                                                PDS_CODESCAN_VARIANT_F,false,
                                                PDSIntTestProductIdentifier.PDS_INTTEST_CODESCAN,
                                                StorageType.REUSE_SECHUB_DATA,
                                                PDS_CODESCAN);

    public static final TestExecutorConfig PDS_V1_CODE_SCAN_G_FAIL_EXIT_CODE_1 = definePDSScan(
                                                PDS_CODESCAN_VARIANT_G,false,
                                                PDSIntTestProductIdentifier.PDS_INTTEST_CODESCAN,
                                                StorageType.REUSE_SECHUB_DATA,
                                                PDS_CODESCAN);

    /**
     * A PDS executor configuration for code scan (generic integration test code scan results). It reuses SecHub storage/data.
     * The {@value IntegrationTestDefaultExecutorConfigurations#JOBPARAM_PDS_KEY_FOR_VARIANTNAME} is set to {@value IntegrationTestDefaultExecutorConfigurations#PDS_CODESCAN_VARIANT_I}.
     *
     * The file filter job parameters are set to:
     * <ul>
     * <li> includes: {@value IntegrationTestDefaultExecutorConfigurations#INCLUDES_1}</li>
     * <li> excludes: {@value IntegrationTestDefaultExecutorConfigurations#EXCLUDES_1}</li>
     * </ul>
     *
     * It is used inside {@link IntegrationTestDefaultProfiles#PROFILE_10_PDS_CODESCAN_INCLUDES_EXCLUDES profile 10}
     *
     */
    public static final TestExecutorConfig PDS_V1_CODE_SCAN_I_INCLUDES_EXCLUDES= definePDSScan(
                                                PDS_CODESCAN_VARIANT_I,false,
                                                PDSIntTestProductIdentifier.PDS_INTTEST_CODESCAN,
                                                StorageType.REUSE_SECHUB_DATA,
                                                PDS_CODESCAN, defineExcludeIncludes1JobParameters());

    /**
     * A PDS executor configuration for code scan (generic integration test code scan results). It reuses SecHub storage/data.
     * The {@value IntegrationTestDefaultExecutorConfigurations#JOBPARAM_PDS_KEY_FOR_VARIANTNAME} is set to {@value IntegrationTestDefaultExecutorConfigurations#PDS_CODESCAN_VARIANT_J}.
     * <br><br>
     * PDS job parameter {@value PDSDefaultParameterKeyConstants#PARAM_KEY_PDS_CONFIG_USE_SECHUB_MAPPINGS} does include:
     * <ul>
     * <li>{@link IntegrationTestExampleConstants#MAPPING_ID_1_REPLACE_ANY_PROJECT1} ({@value IntegrationTestExampleConstants#MAPPING_ID_1_REPLACE_ANY_PROJECT1})</li>
     * <li>{@link IntegrationTestExampleConstants#MAPPING_ID_2_NOT_EXISTING_IN_SECHUB} ({@value IntegrationTestExampleConstants#MAPPING_ID_2_NOT_EXISTING_IN_SECHUB})</li>
     * </ul>
     * <br>
     * Attention: Even when this is a growing scenario, the mapping {@value IntegrationTestExampleConstants#MAPPING_ID_1_REPLACE_ANY_PROJECT1}  is only created one time - same as the profile setup. So mapping may NOT be chnaged inside tests (to avoid side effects).
     * <br>
     * It is used inside {@link IntegrationTestDefaultProfiles#PROFILE_11_PDS_CODESCAN_MAPPING profile 11}
     *
     */
    public static final TestExecutorConfig PDS_V1_CODE_SCAN_J_MAPPING= definePDSScan(
            PDS_CODESCAN_VARIANT_J,false,
            PDSIntTestProductIdentifier.PDS_INTTEST_CODESCAN,
            StorageType.REUSE_SECHUB_DATA,
            PDS_CODESCAN, defineMappingJobParameters());

    /**
     * A PDS executor configuration for code scan (generic integration test code scan results). It reuses SecHub storage/data.
     * The {@value IntegrationTestDefaultExecutorConfigurations#JOBPARAM_PDS_KEY_FOR_VARIANTNAME} is set to {@value IntegrationTestDefaultExecutorConfigurations#PDS_CODESCAN_VARIANT_K}.
     * <br><br>
     * It is used inside {@link IntegrationTestDefaultProfiles#PROFILE_13_PDS_CANCELLATION profile 13}
     */
    public static final TestExecutorConfig PDS_V1_CODE_SCAN_K_CANCELLATION = definePDSScan(
            PDS_CODESCAN_VARIANT_K,false,
            PDSIntTestProductIdentifier.PDS_INTTEST_CODESCAN,
            StorageType.REUSE_SECHUB_DATA,
            PDS_CODESCAN, defineWaitForCancellation());


    public static final TestExecutorConfig PDS_V1_WEB_SCAN_A = definePDSScan(
                                                PDS_WEBSCAN_VARIANT_A,false,
                                                PDSIntTestProductIdentifier.PDS_INTTEST_WEBSCAN,
                                                StorageType.REUSE_SECHUB_DATA,
                                                PDS_WEBSCAN);

    public static final TestExecutorConfig PDS_V1_LICENSE_SCAN_A = definePDSScan(
    											PDS_LICENSESCAN_VARIANT_A, false,
    											PDSIntTestProductIdentifier.PDS_TEST_PRODUCT_LICENSESCAN,
    											StorageType.REUSE_SECHUB_DATA,
    											PDS_LICENSESCAN);

    public static final TestExecutorConfig PDS_V1_SECRET_SCAN_A = definePDSScan(
			PDS_SECRETSCAN_VARIANT_A, false,
			PDSIntTestProductIdentifier.PDS_TEST_PRODUCT_SECRETSCAN,
			StorageType.REUSE_SECHUB_DATA,
			PDS_SECRETSCAN);

    /**
     * The executor configuration does result in usage of {@link PDSIntTestProductIdentifier#PDS_INTTEST_PRODUCT_WS_SARIF}.
     */
    public static final TestExecutorConfig PDS_V1_WEB_SCAN_B_OWASP_SARIF_RESULTS = definePDSScan(
            PDS_WEBSCAN_VARIANT_B,false,
            PDSIntTestProductIdentifier.PDS_INTTEST_PRODUCT_WS_SARIF,
            StorageType.REUSE_SECHUB_DATA,
            PDS_WEBSCAN);

    public static final TestExecutorConfig PDS_V1_CHECKMARX_INTEGRATIONTEST = definePDSScan(
            "default", false,
            PDSIntTestProductIdentifier.PDS_CHECKMARX_INTEGRATIONTEST,
            StorageType.REUSE_SECHUB_DATA,
            PDS_CODESCAN, definePDSCheckmarxParameters(null,null));

    public static final TestExecutorConfig PDS_V1_CHECKMARX_INTEGRATIONTEST_WRONG_WITH_SOURCE_AND_BINARY = definePDSScan(
            "default", false,
            PDSIntTestProductIdentifier.PDS_CHECKMARX_INTEGRATIONTEST,
            StorageType.REUSE_SECHUB_DATA,
            PDS_CODESCAN, definePDSCheckmarxParameters("source,binary",null));

    public static final TestExecutorConfig PDS_V1_CHECKMARX_INTEGRATIONTEST_WITH_FILEFILTER_EXCLUDE_TEXTFILES = definePDSScan(
            "default", false,
            PDSIntTestProductIdentifier.PDS_CHECKMARX_INTEGRATIONTEST,
            StorageType.REUSE_SECHUB_DATA,
            PDS_CODESCAN, definePDSCheckmarxParameters(null,"*.txt"));

    public static final TestExecutorConfig PDS_V1_ANALYZE_INTEGRATIONTEST_CLOC_JSON_1 = definePDSScan(
            "default", false,
            PDSIntTestProductIdentifier.PDS_INTTEST_PRODUCT_ANALYZE,
            StorageType.REUSE_SECHUB_DATA,
            PDS_ANALYTICS);

    public static final TestExecutorConfig PDS_V1_PREPARE_INTEGRATIONTEST_VARIANT_A = definePDSScan(
            PDS_PREPARE_VARIANT_A, false,
            PDSIntTestProductIdentifier.PDS_INTTEST_PRODUCT_PREPARE,
            StorageType.REUSE_SECHUB_DATA,
            PDS_PREPARE);

    public static final TestExecutorConfig PDS_V1_PREPARE_INTEGRATIONTEST_VARIANT_B = definePDSScan(
            PDS_PREPARE_VARIANT_B, false,
            PDSIntTestProductIdentifier.PDS_INTTEST_PRODUCT_PREPARE,
            StorageType.REUSE_SECHUB_DATA,
            PDS_PREPARE);

    public static final TestExecutorConfig PDS_V1_PREPARE_INTEGRATIONTEST_VARIANT_C = definePDSScan(
            PDS_PREPARE_VARIANT_C, false,
            PDSIntTestProductIdentifier.PDS_INTTEST_PRODUCT_PREPARE,
            StorageType.REUSE_SECHUB_DATA,
            PDS_PREPARE);


    /* ----------------------------------------------------------------*/
    /* ---------------PDS solutions mocked-----------------------------*/
    /* ----------------------------------------------------------------*/
    public static final TestExecutorConfig PDS_V1_PDS_SOLUTION_GOSEC_MOCKED = definePDSSolutionMockScan(
            PDSIntTestProductIdentifier.PDS_SOLUTION_GOSEC_MOCKED, PDS_CODESCAN);

    public static final TestExecutorConfig PDS_V1_PDS_SOLUTION_GITLEAKS_MOCKED = definePDSSolutionMockScan(
            PDSIntTestProductIdentifier.PDS_SOLUTION_GITLEAKS_MOCKED, PDS_SECRETSCAN);

    public static final TestExecutorConfig PDS_V1_PDS_SOLUTION_ZAP_MOCKED = definePDSSolutionMockScan(
            PDSIntTestProductIdentifier.PDS_SOLUTION_ZAP_MOCKED, PDS_WEBSCAN);

    public static final TestExecutorConfig PDS_V1_PDS_SOLUTION_CHECKMARX_MOCKED = definePDSSolutionMockScan(
            PDSIntTestProductIdentifier.PDS_SOLUTION_CHECKMARX_MOCKED, PDS_CODESCAN);

    public static final TestExecutorConfig PDS_V1_PDS_SOLUTION_SCANCODE_SPDX_JSON_MOCKED = definePDSSolutionMockScan(
            PDSIntTestProductIdentifier.PDS_SOLUTION_SCANCODE_SPDX_JSON_MOCKED, PDS_LICENSESCAN);

    public static final TestExecutorConfig PDS_V1_PDS_SOLUTION_FINDSECURITYBUGS_MOCKED = definePDSSolutionMockScan(
            PDSIntTestProductIdentifier.PDS_SOLUTION_FINDSECURITYBUGS_MOCKED, PDS_CODESCAN);

    public static final TestExecutorConfig PDS_V1_PDS_SOLUTION_TERN_MOCKED = definePDSSolutionMockScan(
            PDSIntTestProductIdentifier.PDS_SOLUTION_TERN_MOCKED, PDS_LICENSESCAN);

    public static final TestExecutorConfig PDS_V1_PDS_SOLUTION_XRAY_SPDX_MOCKED = definePDSSolutionMockScan(
            PDSIntTestProductIdentifier.PDS_SOLUTION_XRAY_SPDX_MOCKED, PDS_LICENSESCAN);

    public static final TestExecutorConfig PDS_V1_PDS_SOLUTION_XRAY_CYCLONEDX_MOCKED = definePDSSolutionMockScan(
            PDSIntTestProductIdentifier.PDS_SOLUTION_XRAY_CYCLONEDX_MOCKED, PDS_LICENSESCAN);

    public static final TestExecutorConfig PDS_V1_PDS_SOLUTION_MULTI_BANDIT_MOCKED = definePDSSolutionMockScan(
            PDSIntTestProductIdentifier.PDS_SOLUTION_MULTI_BANDIT_MOCKED, PDS_CODESCAN);

    /* @formatter:on */

    public static final String PDS_ENV_VARIABLENAME_TECHUSER_ID = "TEST_PDS_TECHUSER_ID";
    public static final String PDS_ENV_VARIABLENAME_TECHUSER_APITOKEN = "TEST_PDS_TECHUSER_APITOKEN";

    public static final String JOBPARAM_PDS_KEY_FOR_VARIANTNAME = "pds.test.key.variantname";

    public static List<TestExecutorConfig> getAllConfigurations() {
        return Collections.unmodifiableList(registeredConfigurations);
    }

    private static List<TestExecutorSetupJobParam> definePDSCheckmarxParameters(String overrideSupportedDataTypes, String fileFilterExcludes) {
        List<TestExecutorSetupJobParam> parameters = new ArrayList<>();

        parameters.add(new TestExecutorSetupJobParam(KEY_PDS_CHECKMARX_ALWAYS_FULLSCAN_ENABLED, "true"));
        parameters.add(new TestExecutorSetupJobParam(KEY_PDS_CHECKMARX_RESULT_CHECK_PERIOD_MILLISECONDS,
                String.valueOf(AdapterConfigBuilder.MIN_SCAN_RESULT_CHECK_IN_MILLISECONDS)));

        parameters.add(new TestExecutorSetupJobParam(KEY_PDS_CHECKMARX_BASE_URL, "https://localhost:6931/not-real-checkmarx"));
        parameters.add(new TestExecutorSetupJobParam(KEY_PDS_CHECKMARX_USER, "checkmarx-fakeuser"));
        parameters.add(new TestExecutorSetupJobParam(KEY_PDS_CHECKMARX_PASSWORD, "checkmarx-fakepassword"));

        parameters.add(new TestExecutorSetupJobParam(KEY_PDS_CHECKMARX_ENGINE_CONFIGURATION_NAME, "engine1"));

        if (overrideSupportedDataTypes != null) {
            parameters.add(new TestExecutorSetupJobParam(PARAM_KEY_PDS_CONFIG_SUPPORTED_DATATYPES, overrideSupportedDataTypes));
        }
        if (fileFilterExcludes != null) {
            parameters.add(new TestExecutorSetupJobParam(PARAM_KEY_PDS_CONFIG_FILEFILTER_EXCLUDES, fileFilterExcludes));
        }

        addCheckmarxDefaultTeamIdAndPresetMappingData(parameters);

        enablePDSDebugging(parameters);

        return parameters;
    }

    private static void enablePDSDebugging(List<TestExecutorSetupJobParam> parameters) {
        parameters.add(new TestExecutorSetupJobParam(PARAM_KEY_PDS_DEBUG_ENABLED, "true"));
    }

    private static List<TestExecutorSetupJobParam> defineExcludeIncludes1JobParameters() {
        List<TestExecutorSetupJobParam> list = new ArrayList<>();

        list.add(new TestExecutorSetupJobParam(PARAM_KEY_PDS_CONFIG_FILEFILTER_EXCLUDES, EXCLUDES_1));
        list.add(new TestExecutorSetupJobParam(PARAM_KEY_PDS_CONFIG_FILEFILTER_INCLUDES, INCLUDES_1));

        return list;
    }

    private static List<TestExecutorSetupJobParam> defineWaitForCancellation() {
        List<TestExecutorSetupJobParam> list = new ArrayList<>();
        list.add(new TestExecutorSetupJobParam(PARAM_KEY_PDS_CONFIG_CANCEL_EVENT_CHECKINTERVAL_MILLISECONDS, "100"));
        list.add(new TestExecutorSetupJobParam(PARAM_KEY_PDS_CONFIG_CANCEL_MAXIMUM_WAITTIME_SECONDS, "20"));
        return list;
    }

    private static List<TestExecutorSetupJobParam> defineMappingJobParameters() {
        List<TestExecutorSetupJobParam> list = new ArrayList<>();

        list.add(new TestExecutorSetupJobParam(PARAM_KEY_PDS_CONFIG_USE_SECHUB_MAPPINGS, IntegrationTestExampleConstants.MAPPING_ID_1_REPLACE_ANY_PROJECT1
                + ", " + IntegrationTestExampleConstants.MAPPING_ID_2_NOT_EXISTING_IN_SECHUB));
        return list;
    }

    private static List<TestExecutorSetupJobParam> defineScriptTrustAllCertificatesJobParameter(boolean trustAll) {
        List<TestExecutorSetupJobParam> list = new ArrayList<>();

        list.add(new TestExecutorSetupJobParam(PARAM_KEY_PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED, "" + trustAll));

        return list;
    }

    private static String createProductIdentifierString(PDSIntTestProductIdentifier pdsProductIdentifier) {
        return pdsProductIdentifier != null ? pdsProductIdentifier.getId() : null;
    }

    private static TestExecutorConfig definePDSSolutionMockScan(PDSIntTestProductIdentifier pdsProductIdentifier,
            TestProductExecutorIdentifier sechubProductIdentifier) {
        return definePDSScan("a", false, pdsProductIdentifier, StorageType.REUSE_SECHUB_DATA, sechubProductIdentifier, null);
    }

    private static TestExecutorConfig definePDSScan(String variant, boolean credentialsAsEnvEntries, PDSIntTestProductIdentifier pdsProductIdentifier,
            StorageType storageType, TestProductExecutorIdentifier sechubProductIdentifier) {
        return definePDSScan(variant, credentialsAsEnvEntries, pdsProductIdentifier, storageType, sechubProductIdentifier, null);
    }

    private static TestExecutorConfig definePDSScan(String variant, boolean credentialsAsEnvEntries, PDSIntTestProductIdentifier pdsProductIdentifier,
            StorageType storageType, TestProductExecutorIdentifier sechubProductIdentifier, List<TestExecutorSetupJobParam> additionalJobParameters) {
        String productIdentifierId = createProductIdentifierString(pdsProductIdentifier);

        TestExecutorConfig config = createTestExecutorConfig();

        String middleConfigName = sechubProductIdentifier.name().toLowerCase() + "_";

        config.enabled = true;
        config.executorVersion = 1;
        config.productIdentifier = sechubProductIdentifier.name();
        config.name = INTTEST_NAME_PREFIX + middleConfigName + variant;
        config.setup.baseURL = PDSTestURLBuilder.https(TestPortProvider.DEFAULT_INSTANCE.getIntegrationTestPDSPort()).buildBaseUrl();
        config.uuid = null;// not initialized - is done at creation time by scenario initializer!

        if (credentialsAsEnvEntries) {
            config.setup.credentials.user = "env:" + PDS_ENV_VARIABLENAME_TECHUSER_ID;
            config.setup.credentials.password = "env:" + PDS_ENV_VARIABLENAME_TECHUSER_APITOKEN;
        } else {
            config.setup.credentials.user = TestAPI.PDS_TECH_USER.getUserId();
            config.setup.credentials.password = TestAPI.PDS_TECH_USER.getApiToken();
        }
        boolean useSecHubStorage = storageType == StorageType.REUSE_SECHUB_DATA;

        List<TestExecutorSetupJobParam> jobParameters = config.setup.jobParameters;
        jobParameters.add(new TestExecutorSetupJobParam(PARAM_KEY_PDS_CONFIG_PRODUCTIDENTIFIER, productIdentifierId));
        jobParameters.add(new TestExecutorSetupJobParam(PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE, Boolean.valueOf(useSecHubStorage).toString()));

        jobParameters.add(new TestExecutorSetupJobParam(TRUST_ALL_CERTIFICATES, "true")); // accept self signed certificates for
                                                                                          // testing
        jobParameters.add(new TestExecutorSetupJobParam(TIME_TO_WAIT_NEXT_CHECK_MILLIS, "500")); // speed up tests...

        jobParameters.add(new TestExecutorSetupJobParam("product1.qualititycheck.enabled", "true")); // mandatory from PDS integration test server
        if (PDS_CODESCAN.equals(sechubProductIdentifier)) {
            jobParameters.add(new TestExecutorSetupJobParam("product1.level", VALUE_PRODUCT_LEVEL)); // mandatory from PDS integration test server
        } else if (PDS_WEBSCAN.equals(sechubProductIdentifier)) {
            jobParameters.add(new TestExecutorSetupJobParam("product2.level", "4711")); // mandatory from PDS integration test server
        }
        jobParameters.add(new TestExecutorSetupJobParam(JOBPARAM_PDS_KEY_FOR_VARIANTNAME, variant));

        if (additionalJobParameters != null) {
            jobParameters.addAll(additionalJobParameters);
        }
        return config;
    }

    private static TestExecutorConfig defineNetsparkerConfig() {
        TestExecutorConfig config = createTestExecutorConfig();
        config.enabled = true;
        config.executorVersion = 1;
        config.productIdentifier = NETSPARKER.name();
        config.name = INTTEST_NAME_PREFIX + "Netsparker V1";
        config.setup.baseURL = "https://netsparker.example.com";
        config.setup.credentials.user = "netsparker-user";
        config.setup.credentials.password = "netsparker-password";
        config.uuid = null;// not initialized - is done at creation time by scenario initializer!
        return config;
    }

    private static TestExecutorConfig defineCheckmarxConfig() {
        TestExecutorConfig config = createTestExecutorConfig();
        config.enabled = true;
        config.executorVersion = 1;
        config.productIdentifier = CHECKMARX.name();
        config.name = INTTEST_NAME_PREFIX + "Checkmarx V1";
        config.setup.baseURL = "https://checkmarx.mock.example.org:6011";
        config.setup.credentials.user = "checkmarx-user";
        config.setup.credentials.password = "checkmarx-password";
        config.uuid = null;// not initialized - is done at creation time by scenario initializer!

        List<TestExecutorSetupJobParam> jobParameters = config.setup.jobParameters;
        addCheckmarxDefaultTeamIdAndPresetMappingData(jobParameters);

        return config;
    }

    private static void addCheckmarxDefaultTeamIdAndPresetMappingData(List<TestExecutorSetupJobParam> jobParameters) {
        MappingData teamIdMappingData = new MappingData();
        teamIdMappingData.getEntries().add(CHECKMARX_TEAMID_MAPPING_DEFAULT_MAPPING);

        MappingData presetIdMappingData = new MappingData();
        presetIdMappingData.getEntries().add(CHECKMARX_PRESETID_MAPPING_DEFAULT_MAPPING);

        jobParameters.add(new TestExecutorSetupJobParam("checkmarx.newproject.teamid.mapping", teamIdMappingData.toJSON()));
        jobParameters.add(new TestExecutorSetupJobParam("checkmarx.newproject.presetid.mapping", presetIdMappingData.toJSON()));
    }

    private static TestExecutorConfig defineNessusConfig() {
        TestExecutorConfig config = createTestExecutorConfig();
        config.enabled = true;
        config.executorVersion = 1;
        config.productIdentifier = NESSUS.name();
        config.name = INTTEST_NAME_PREFIX + "Nessus V1";
        config.setup.baseURL = "\"https://nessus-intranet.mock.example.org:6000";
        config.setup.credentials.user = "nessus-user-id";
        config.setup.credentials.password = "nessus-password";
        config.uuid = null;// not initialized - is done at creation time by scenario initializer!

        List<TestExecutorSetupJobParam> jobParameters = config.setup.jobParameters;
        jobParameters.add(new TestExecutorSetupJobParam("nessus.default.policy.id", "nessus-default-policiy-id"));
        return config;
    }

    private static TestExecutorConfig createTestExecutorConfig() {
        TestExecutorConfig testExecutorConfig = new TestExecutorConfig();
        registeredConfigurations.add(testExecutorConfig);
        testExecutorConfig.setup.jobParameters.add(new TestExecutorSetupJobParam(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_MOCKING_DISABLED, "true"));
        return testExecutorConfig;
    }

}
