// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants.*;
import static com.mercedesbenz.sechub.domain.scan.product.pds.PDSProductExecutorKeyConstants.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestExecutorProductIdentifier.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.integrationtest.api.PDSIntTestProductIdentifier;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.TestExecutorProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingData;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingEntry;
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

    private static final String INTTEST_NAME_PREFIX = "INTTEST_";

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

    public static final String PDS_WEBSCAN_VARIANT_A = "a";
    public static final String PDS_WEBSCAN_VARIANT_B = "b";

    public static final String PDS_LICENSESCAN_VARIANT_A = "a";

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
     * The file filter job paramaters are set to:
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

    /**
     * The executor configuration does result in usage of {@link PDSIntTestProductIdentifier#PDS_INTTEST_PRODUCT_WS_SARIF}.
     */
    public static final TestExecutorConfig PDS_V1_WEB_SCAN_B_OWASP_SARIF_RESULTS = definePDSScan(
            PDS_WEBSCAN_VARIANT_B,false,
            PDSIntTestProductIdentifier.PDS_INTTEST_PRODUCT_WS_SARIF,
            StorageType.REUSE_SECHUB_DATA,
            PDS_WEBSCAN);

    /* @formatter:on */

    public static final String PDS_ENV_VARIABLENAME_TECHUSER_ID = "TEST_PDS_TECHUSER_ID";
    public static final String PDS_ENV_VARIABLENAME_TECHUSER_APITOKEN = "TEST_PDS_TECHUSER_APITOKEN";

    public static final String JOBPARAM_PDS_KEY_FOR_VARIANTNAME = "pds.test.key.variantname";

    public static List<TestExecutorConfig> getAllConfigurations() {
        return Collections.unmodifiableList(registeredConfigurations);
    }

    private static List<TestExecutorSetupJobParam> defineExcludeIncludes1JobParameters() {
        List<TestExecutorSetupJobParam> list = new ArrayList<>();

        list.add(new TestExecutorSetupJobParam(PARAM_KEY_PDS_CONFIG_FILEFILTER_EXCLUDES, EXCLUDES_1));
        list.add(new TestExecutorSetupJobParam(PARAM_KEY_PDS_CONFIG_FILEFILTER_INCLUDES, INCLUDES_1));

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

    private static TestExecutorConfig definePDSScan(String variant, boolean credentialsAsEnvEntries, PDSIntTestProductIdentifier pdsProductIdentifier,
            StorageType storageType, TestExecutorProductIdentifier sechubProductIdentifier) {
        return definePDSScan(variant, credentialsAsEnvEntries, pdsProductIdentifier, storageType, sechubProductIdentifier, null);
    }

    private static TestExecutorConfig definePDSScan(String variant, boolean credentialsAsEnvEntries, PDSIntTestProductIdentifier pdsProductIdentifier,
            StorageType storageType, TestExecutorProductIdentifier sechubProductIdentifier, List<TestExecutorSetupJobParam> additionalJobParameters) {
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

        MappingData teamIdMappingData = new MappingData();
        teamIdMappingData.getEntries().add(CHECKMARX_TEAMID_MAPPING_DEFAULT_MAPPING);

        MappingData presetIdMappingData = new MappingData();
        presetIdMappingData.getEntries().add(CHECKMARX_PRESETID_MAPPING_DEFAULT_MAPPING);

        List<TestExecutorSetupJobParam> jobParameters = config.setup.jobParameters;
        jobParameters.add(new TestExecutorSetupJobParam("checkmarx.newproject.teamid.mapping", teamIdMappingData.toJSON()));
        jobParameters.add(new TestExecutorSetupJobParam("checkmarx.newproject.presetid.mapping", presetIdMappingData.toJSON()));

        return config;
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
