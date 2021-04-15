// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;

public class PDSProductExecutorMinimumConfigValidationTest {

    private PDSProductExecutorMinimumConfigValidation validationToTest;

    @Before
    public void before() throws Exception {
        validationToTest = new PDSProductExecutorMinimumConfigValidation();
    }

    @Test
    public void empty_parameters_has_message_with_product_identifier_not_set() {
        /* prepare */
        ProductExecutorConfig config = createConfigWithNoParametersSet();

        /* execute */
        ValidationResult result = validationToTest.validate(config);

        /* test */
        assertValidationResultContainsMissingProductIdentifier(result);

    }

    @Test
    public void mandatory_parameters_set_but_empty_has_message_with_product_identifier_not_set() {
        /* prepare */
        ProductExecutorConfig config = createConfigWithAllMandatoryParametersSetButEmpty();

        /* execute */
        ValidationResult result = validationToTest.validate(config);

        /* test */
        assertValidationResultContainsMissingProductIdentifier(result);

    }

    @Test
    public void when_all_mandatory_parameters_are_set_we_got_no_error_messages() {
        /* prepare */
        ProductExecutorConfig config = createConfigWithAllMandatoryParametersSet();

        /* execute */
        ValidationResult result = validationToTest.validate(config);

        /* test */
        if (!result.isValid()) {
            fail("Result is not valid, failure message was:" + result.getErrorDescription());
        }
        assertEquals(0, result.getErrors().size());
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helpers.......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private void assertValidationResultContainsMissingProductIdentifier(ValidationResult result) {
        assertFalse("Result may not be valid, but is!", result.isValid());
        boolean found = false;
        for (String error : result.getErrors()) {
            if (error.indexOf(PDSConfigDataKeyProvider.PDS_PRODUCT_IDENTIFIER.getKey().getId()) == -1) {
                continue;
            }
            if (error.toLowerCase().indexOf("not set") == -1) {
                continue;
            }
            found = true;
            break;
        }
        assertTrue("Did not found an entry about missing product identifier key", found);
    }

    private ProductExecutorConfig createConfigWithAllMandatoryParametersSet() {
        return createConfigWithAllMandatoryParametersSetWith("not-empty");
    }

    private ProductExecutorConfig createConfigWithAllMandatoryParametersSetWith(String value) {
        ProductExecutorConfig config = createConfigWithNoParametersSet();
        List<ProductExecutorConfigSetupJobParameter> params = config.getSetup().getJobParameters();
        for (PDSProductExecutorKeyProvider k : PDSProductExecutorKeyProvider.values()) {
            PDSProductExecutorKey key = k.getKey();
            if (key.isMandatory()) {
                params.add(new ProductExecutorConfigSetupJobParameter(key.getId(),value));
            }
        }
        for (PDSConfigDataKeyProvider k : PDSConfigDataKeyProvider.values()) {
            PDSConfigDataKey key = k.getKey();
            if (key.isMandatory()) {
                params.add(new ProductExecutorConfigSetupJobParameter(key.getId(),value));
            }
        }
        return config;
    }

    private ProductExecutorConfig createConfigWithAllMandatoryParametersSetButEmpty() {
        return createConfigWithAllMandatoryParametersSetWith("");
    }

    private ProductExecutorConfig createConfigWithNoParametersSet() {
        ProductExecutorConfigSetup setup = new ProductExecutorConfigSetup();
        ProductExecutorConfig config = new ProductExecutorConfig(ProductIdentifier.PDS_CODESCAN, 1, setup);
        return config;
    }

}
