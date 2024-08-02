// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterDefinition;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSServerConfiguration;
import com.mercedesbenz.sechub.test.TestFileReader;

/**
 * In our asciidoc files we have multiple references to example files. Inside
 * this test we check some of them to contain expected values.
 *
 * @author Albert Tregnaghi
 *
 */
class ExampleFilesValidTest {

    @Test
    void check_pds_config_example1_can_be_loaded_and_is_valid() throws Exception {

        /* execute */
        String json = TestFileReader.loadTextFile("src/docs/asciidoc/documents/pds/product_delegation_server_config_example1.json");
        PDSServerConfiguration configuration = PDSServerConfiguration.fromJSON(json);

        /* test */
        assertNotNull(configuration);
        List<PDSProductSetup> products = configuration.getProducts();
        assertEquals(2, products.size());
        PDSProductSetup productSetup1 = null;
        PDSProductSetup productSetup2 = null;

        for (PDSProductSetup setup : products) {
            if ("PRODUCT_1".equals(setup.getId())) {
                productSetup1 = setup;
            } else if ("PRODUCT_2".equals(setup.getId())) {
                productSetup2 = setup;
            } else {
                fail("Unexpeted product found: " + setup.getId());
            }
        }

        // check both setup are found
        assertNotNull(productSetup1);
        assertNotNull(productSetup2);

        // make same spear checks
        assertDefaultValue(productSetup1, false, PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SUPPORTED_DATATYPES, "source");
        assertDefaultValue(productSetup2, true, PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SUPPORTED_DATATYPES, "none");
    }

    @ParameterizedTest
    @EnumSource(ExampleFile.class)
    void every_sechub_config_file_is_valid(ExampleFile file) {
        /* prepare */
        String json = TestFileReader.loadTextFile(file.getPath());
        SecHubScanConfiguration config = null;

        /* execute */
        try {
            config = SecHubScanConfiguration.createFromJSON(json);
        } catch (JSONConverterException e) {
            fail("Could not create SecHubScanConfiguration from json for file: " + file.getPath());
        }

        /* test */
        assertNotNull(config);
    }

    @ParameterizedTest
    @EnumSource(value = ExampleFile.class, names = { "WEBSCAN_ANONYMOUS", "WEBSCAN_BASIC_AUTH", "WEBSCAN_FORM_BASED_SCRIPT_AUTH",
            "WEBSCAN_OPENAPI_WITH_DATA_REFERENCE" }, mode = EnumSource.Mode.INCLUDE)
    void every_sechub_config_webscan_file_is_valid_and_has_a_target_uri(ExampleFile file) {
        /* prepare */
        String json = TestFileReader.loadTextFile(file.getPath());

        /* execute */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        Optional<SecHubWebScanConfiguration> webScanOpt = config.getWebScan();
        assertTrue(webScanOpt.isPresent(), "Webscan configuration does exist for file: " + file.getPath());

        SecHubWebScanConfiguration webScan = webScanOpt.get();
        assertNotNull(webScan.getUrl(), "No URI set in file: " + file.getPath());
    }

    private void assertDefaultValue(PDSProductSetup setup, boolean isMandatory, String parameterKey, String expectedDefault) {
        PDSProductParameterSetup parameters = setup.getParameters();
        List<PDSProductParameterDefinition> list = null;
        if (isMandatory) {
            list = parameters.getMandatory();
        } else {
            list = parameters.getOptional();
        }

        for (PDSProductParameterDefinition definition : list) {
            if (parameterKey.equals(definition.getKey())) {
                assertEquals(expectedDefault, definition.getDefault(), " Product:" + setup.getId() + " has unexpectedd default value");
                return;
            }
        }
        fail("No parameter with key:" + parameterKey + " found in (" + (isMandatory ? "mandatory" : "optional") + " configuration of product:" + setup.getId()
                + " !");
    }

}
