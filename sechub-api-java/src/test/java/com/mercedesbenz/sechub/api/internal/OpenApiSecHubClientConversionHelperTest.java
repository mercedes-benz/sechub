// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiScanJob;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubLicenseScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSecretScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;

class OpenApiSecHubClientConversionHelperTest {

    private OpenApiSecHubClientConversionHelper helperToTest;

    @BeforeAll
    static void beforeAll() {
    }

    @BeforeEach
    void beforeEach() {
        helperToTest = new OpenApiSecHubClientConversionHelper();
    }

    @ParameterizedTest
    @EnumSource(value = ScanType.class)
    void scantype_is_either_internal_or_public_but_available_in_origin_config_also_converted_json(ScanType type) throws Exception {

        /* prepare */
        SecHubConfigurationModel configuration = new SecHubConfigurationModel();
        configuration.setApiVersion("1.0");

        switch (type) {
        case CODE_SCAN:
            SecHubCodeScanConfiguration codeScan = new SecHubCodeScanConfiguration();
            configuration.setCodeScan(codeScan);
            break;
        case INFRA_SCAN:
            SecHubInfrastructureScanConfiguration infraScan = new SecHubInfrastructureScanConfiguration();
            configuration.setInfraScan(infraScan);
            break;
        case LICENSE_SCAN:
            SecHubLicenseScanConfiguration licenseScan = new SecHubLicenseScanConfiguration();
            configuration.setLicenseScan(licenseScan);
            break;
        case SECRET_SCAN:
            SecHubSecretScanConfiguration secretScan = new SecHubSecretScanConfiguration();
            configuration.setSecretScan(secretScan);
            break;
        case WEB_SCAN:
            SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
            configuration.setWebScan(webScan);
            break;
        default:
            if (type.isInternalScanType()) {
                /* we just ignore internal scan types - they are not configurable inside json */
                return;
            }
            throw new IllegalStateException("Not tested public scan type: " + type + ". Must be implemented inside this test");
        }

        /* execute */
        OpenApiScanJob result = helperToTest.convertToOpenApiScanJob(configuration);

        /* test */
        String configurationAsPrettyJson = JSONConverter.get().toJSON(configuration, true);
        String resultAsPrettyJson = JSONConverter.get().toJSON(result, true);

        if (!configurationAsPrettyJson.contains(type.getId())) {
            throw new IllegalStateException("Something wrong with this test! At least " + type.getId() + " must be contained in configuration json!");
        }
        if (!resultAsPrettyJson.contains(type.getId())) {
            // we use assertEquals to show up difference directly in IDEs as error text -
            // easier for debugging.
            assertEquals(configurationAsPrettyJson, resultAsPrettyJson, "Scan type: " + type.getId() + " is not contained in result!");
        }

    }

}
