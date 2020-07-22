package com.daimler.sechub.domain.scan.product.config;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.daimler.sechub.domain.scan.ScanDomainTestFileSupport;

public class ProductExecutorConfigListTest {

    @Test
    public void from_json_executor_config_example_1_can_be_read_transformed_to_json_and_contains_expected_data() {
        /* prepare */
        String json = ScanDomainTestFileSupport.getTestfileSupport().loadTestFile("executor/executor-configuration-list-example1.json");

        /* execute */
        ProductExecutorConfigList result = ProductExecutorConfigList.fromJSONString(json);

        /* test */
        assertEquals("executorConfigurationList", result.getType());
        List<ProductExecutorConfig> configurations = result.getExecutorConfigurations();
        assertEquals(3, configurations.size());
        Iterator<ProductExecutorConfig> it = configurations.iterator();
        ProductExecutorConfig p1 = it.next();
        assertEquals(Boolean.TRUE, p1.getEnabled());
        assertEquals(Integer.valueOf(1), p1.getExecutorVersion());
    }

    @Test
    public void to_json_returns_not_null_and_can_converted_back() {
        /* prepare */

        ProductExecutorConfigList list = new ProductExecutorConfigList();
        ProductExecutorConfig config1 = new ProductExecutorConfig();
        config1.enabled = true;
        config1.uUID = UUID.randomUUID();
        config1.version = 1234;
        config1.executorVersion = 4711;
        ProductExecutorConfigSetup setup = new ProductExecutorConfigSetup();
        setup.setBaseURL("https://localhost:8475/api/xyz");
        setup.getCredentials().setUser("dan");
        setup.getCredentials().setPassword("pwd1");

        config1.setSetup(setup.toJSON());

        list.getExecutorConfigurations().add(config1);
        /* execute */
        String json = list.toJSON();

        /* test */
        assertNotNull(json);
        ProductExecutorConfigList back = ProductExecutorConfigList.fromJSONString(json);
        assertEquals("executorConfigurationList", back.getType());
        System.out.println(json);

    }

}
