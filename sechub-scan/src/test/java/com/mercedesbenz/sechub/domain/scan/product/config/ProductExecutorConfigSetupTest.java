// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import static org.junit.Assert.*;

import org.junit.Test;

import com.mercedesbenz.sechub.domain.scan.TestScanDomainFileSupport;

public class ProductExecutorConfigSetupTest {

    @Test
    public void executor_config_example_1_can_be_read_transformed_to_json_and_contains_expected_data() {
        /* prepare */
        String json = TestScanDomainFileSupport.getTestfileSupport().loadTestFile("executor/executor-configuration-setup-example1.json");

        /* execute */
        ProductExecutorConfigSetup result = ProductExecutorConfigSetup.fromJSONString(json);

        /* test */
        assertNotNull(result);

    }

}
