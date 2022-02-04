// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import static com.daimler.sechub.commons.pds.PDSDefaultParameterKeyConstants.*;
import static com.daimler.sechub.domain.scan.product.pds.PDSProductExecutorKeyConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.daimler.sechub.adapter.pds.DelegatingMockablePDSAdapterV1;
import com.daimler.sechub.integrationtest.api.PDSIntTestProductIdentifier;
import com.daimler.sechub.integrationtest.api.TestAPI;
import com.daimler.sechub.integrationtest.api.TestExecutorProductIdentifier;
import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingEntry;
import com.daimler.sechub.test.TestPortProvider;
import com.daimler.sechub.test.TestURLBuilder;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;
import com.daimler.sechub.test.executorconfig.TestExecutorSetupJobParam;

public class IntegrationTestDefaultExecutorConfigurations {

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

    public static final String PDS_WEBSCAN_VARIANT_A = "a";
    public static final String PDS_WEBSCAN_VARIANT_B = "b";

    /* @formatter:off */
    public static final TestExecutorConfig PDS_V1_CODE_SCAN_A = definePDSCodeScan(
                                                PDS_CODESCAN_VARIANT_A,false,
                                                PDSIntTestProductIdentifier.PDS_INTTEST_CODESCAN, 
                                                StorageType.REUSE_SECHUB_DATA);
    
    public static final TestExecutorConfig PDS_V1_CODE_SCAN_B = definePDSCodeScan(
                                                PDS_CODESCAN_VARIANT_B,true,
                                                PDSIntTestProductIdentifier.PDS_INTTEST_CODESCAN, 
                                                StorageType.REUSE_SECHUB_DATA);
    
    public static final TestExecutorConfig PDS_V1_CODE_SCAN_C = definePDSScan(
                                                PDS_CODESCAN_VARIANT_C,true,
                                                (String)null,
                                                StorageType.REUSE_SECHUB_DATA,TestExecutorProductIdentifier.PDS_CODESCAN);// no PDS identifier set, will not work...
    
    public static final TestExecutorConfig PDS_V1_CODE_SCAN_D = definePDSCodeScan(
                                                PDS_CODESCAN_VARIANT_D,false,PDSIntTestProductIdentifier.
                                                PDS_INTTEST_PRODUCT_CS_SARIF,
                                                StorageType.REUSE_SECHUB_DATA);
    
    public static final TestExecutorConfig PDS_V1_CODE_SCAN_E_DO_NOT_REUSE_SECHUBDATA = definePDSCodeScan(
                                                PDS_CODESCAN_VARIANT_E,false,
                                                PDSIntTestProductIdentifier.PDS_INTTEST_PRODUCT_CS_SARIF,
                                                StorageType.DO_NOT_REUSE_SECHUB_DATA);
    
    public static final TestExecutorConfig PDS_V1_CODE_SCAN_F = definePDSCodeScan(
                                                PDS_CODESCAN_VARIANT_F,false,
                                                PDSIntTestProductIdentifier.PDS_INTTEST_CODESCAN,
                                                StorageType.REUSE_SECHUB_DATA);
    
    public static final TestExecutorConfig PDS_V1_CODE_SCAN_G_FAIL_EXIT_CODE_1 = definePDSCodeScan(
                                                PDS_CODESCAN_VARIANT_G,false,
                                                PDSIntTestProductIdentifier.PDS_INTTEST_CODESCAN,
                                                StorageType.REUSE_SECHUB_DATA);
    
    public static final TestExecutorConfig PDS_V1_WEB_SCAN_A = definePDSWebScan(
                                                PDS_WEBSCAN_VARIANT_A,false,
                                                PDSIntTestProductIdentifier.PDS_INTTEST_WEBSCAN,
                                                StorageType.REUSE_SECHUB_DATA);
    
    /**
     * The executor configuration does result in usage of {@link PDSIntTestProductIdentifier#PDS_INTTEST_PRODUCT_WS_SARIF}.
     */
    public static final TestExecutorConfig PDS_V1_WEB_SCAN_B_OWASP_SARIF_RESULTS = definePDSWebScan(
            PDS_WEBSCAN_VARIANT_B,false,
            PDSIntTestProductIdentifier.PDS_INTTEST_PRODUCT_WS_SARIF,
            StorageType.REUSE_SECHUB_DATA);
    
    /* @formatter:on */

    public static final String PDS_ENV_VARIABLENAME_TECHUSER_ID = "TEST_PDS_TECHUSER_ID";
    public static final String PDS_ENV_VARIABLENAME_TECHUSER_APITOKEN = "TEST_PDS_TECHUSER_APITOKEN";

    public static final String JOBPARAM_PDS_KEY_FOR_VARIANTNAME = "pds.test.key.variantname";

    public static List<TestExecutorConfig> getAllConfigurations() {
        return Collections.unmodifiableList(registeredConfigurations);
    }

    private static TestExecutorConfig definePDSCodeScan(String variant, boolean credentialsAsEnvEntries, PDSIntTestProductIdentifier pdsProductIdentifier,
            StorageType useSecHubStorage) {
        String productIdentfieriId = createProductIdentifierString(pdsProductIdentifier);
        return definePDSScan(variant, credentialsAsEnvEntries, productIdentfieriId, useSecHubStorage, TestExecutorProductIdentifier.PDS_CODESCAN);

    }

    private static TestExecutorConfig definePDSWebScan(String variant, boolean credentialsAsEnvEntries, PDSIntTestProductIdentifier pdsProductIdentifier,
            StorageType useSecHubStorage) {
        String productIdentfieriId = createProductIdentifierString(pdsProductIdentifier);
        return definePDSScan(variant, credentialsAsEnvEntries, productIdentfieriId, useSecHubStorage, TestExecutorProductIdentifier.PDS_WEBSCAN);

    }

    private static String createProductIdentifierString(PDSIntTestProductIdentifier pdsProductIdentifier) {
        return pdsProductIdentifier != null ? pdsProductIdentifier.getId() : "not-existing";
    }

    private static TestExecutorConfig definePDSScan(String variant, boolean credentialsAsEnvEntries, String productIdentifierId, StorageType storageType,
            TestExecutorProductIdentifier sechubProductIdentifier) {
        TestExecutorConfig config = createTestExecutorConfig();

        String middleConfigName = sechubProductIdentifier.name().toLowerCase() + "_";

        config.enabled = true;
        config.executorVersion = 1;
        config.productIdentifier = sechubProductIdentifier.name();
        config.name = INTTEST_NAME_PREFIX + middleConfigName + variant;

        config.setup.baseURL = TestURLBuilder.https(TestPortProvider.DEFAULT_INSTANCE.getIntegrationTestPDSPort()).pds().buildBaseUrl();
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
        if (TestExecutorProductIdentifier.PDS_CODESCAN.equals(sechubProductIdentifier)) {
            jobParameters.add(new TestExecutorSetupJobParam("product1.level", VALUE_PRODUCT_LEVEL)); // mandatory from PDS integration test server
        } else if (TestExecutorProductIdentifier.PDS_WEBSCAN.equals(sechubProductIdentifier)) {
            jobParameters.add(new TestExecutorSetupJobParam("product2.level", "4711")); // mandatory from PDS integration test server
        }
        jobParameters.add(new TestExecutorSetupJobParam(JOBPARAM_PDS_KEY_FOR_VARIANTNAME, variant));

        return config;
    }

    private static TestExecutorConfig defineNetsparkerConfig() {
        TestExecutorConfig config = createTestExecutorConfig();
        config.enabled = true;
        config.executorVersion = 1;
        config.productIdentifier = TestExecutorProductIdentifier.NETSPARKER.name();
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
        config.productIdentifier = TestExecutorProductIdentifier.CHECKMARX.name();
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
        config.productIdentifier = TestExecutorProductIdentifier.NESSUS.name();
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
        testExecutorConfig.setup.jobParameters.add(new TestExecutorSetupJobParam(DelegatingMockablePDSAdapterV1.JOB_PARAMETER_KEY__PDS_MOCKING_DISABLED, "true"));
        return testExecutorConfig;
    }

}
