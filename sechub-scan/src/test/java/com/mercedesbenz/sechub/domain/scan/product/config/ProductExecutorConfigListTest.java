// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.config;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.mercedesbenz.sechub.domain.scan.TestScanDomainFileSupport;

public class ProductExecutorConfigListTest {

    @Test
    public void from_json_executor_config_example_1_can_be_read_transformed_to_json_and_contains_expected_data() {
        /* prepare */
        String json = TestScanDomainFileSupport.getTestfileSupport().loadTestFile("executor/executor-configuration-list-example1.json");

        /* execute */
        ProductExecutorConfigList result = ProductExecutorConfigList.fromJSONString(json);

        /* test */
        assertEquals("executorConfigurationList", result.getType());
        List<ProductExecutorConfigListEntry> configurations = result.getExecutorConfigurations();
        assertEquals(3, configurations.size());
        Iterator<ProductExecutorConfigListEntry> it = configurations.iterator();
        ProductExecutorConfigListEntry p1 = it.next();
        assertEquals(Boolean.TRUE, p1.getEnabled());
    }

}
