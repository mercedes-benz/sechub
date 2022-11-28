package com.mercedesbenz.sechub;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.pds.config.PDSProductParameterDefinition;
import com.mercedesbenz.sechub.pds.config.PDSProductParameterSetup;
import com.mercedesbenz.sechub.pds.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.config.PDSServerConfiguration;
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
