// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario7;

import static com.mercedesbenz.sechub.integrationtest.api.AssertExecutorConfig.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.springframework.http.HttpStatus;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProductExecutorIdentifier;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorConfig;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorConfigList;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorConfigListEntry;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorSetupJobParam;

public class ProductExecutorConfigurationScenario7IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario7.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(30);

    @Test
    public void an_admin_can_create_a_new_product_executor_config_and_it_returns_uuid() {
        /* prepare */
        TestExecutorConfig config = new TestExecutorConfig();
        config.productIdentifier = TestProductExecutorIdentifier.PDS_CODESCAN.name();
        config.name = "pds gosec-1";
        config.executorVersion = 1;
        config.setup.baseURL = "https://baseurl.product.example.com/start";

        /* execute */

        UUID uuid = as(SUPER_ADMIN).createProductExecutorConfig(config);

        /* test */
        assertNotNull(uuid);

    }

    @Test
    public void an_admin_can_fetch_former_created_product_executor_config() {

        /* prepare */
        TestExecutorConfig config = new TestExecutorConfig();
        config.productIdentifier = TestProductExecutorIdentifier.PDS_CODESCAN.name();
        config.name = "pds gosec-1";
        config.executorVersion = 1;
        config.setup.baseURL = "https://baseurl.product.example.com/start";
        config.setup.jobParameters.add(new TestExecutorSetupJobParam("key1", "value1"));
        config.setup.jobParameters.add(new TestExecutorSetupJobParam("key2", "value2"));

        UUID uuid = as(SUPER_ADMIN).createProductExecutorConfig(config);

        /* execute */
        TestExecutorConfig fetchedConfig = as(SUPER_ADMIN).fetchProductExecutorConfig(uuid);

        /* test */
        /* @formatter:off */
        assertNotNull(fetchedConfig);

        assertConfig(uuid).
            hasBaseURL("https://baseurl.product.example.com/start").
            hasJobParameter("key1", "value1").
            hasJobParameter("key2", "value2");
        /* @formatter:on */
    }

    @Test
    public void an_admin_can_update_former_created_product_executor_config() {
        /* @formatter:off */

        /* prepare */
        TestExecutorConfig config = new TestExecutorConfig();
        config.productIdentifier=TestProductExecutorIdentifier.PDS_CODESCAN.name();
        config.name="pds gosec-1";
        config.executorVersion=1;
        config.setup.baseURL="https://baseurl.product.example.com/start";
        config.setup.jobParameters.add(new TestExecutorSetupJobParam("key1","value1"));
        config.setup.jobParameters.add(new TestExecutorSetupJobParam("key2","value2"));

        UUID uuid = as(SUPER_ADMIN).
                createProductExecutorConfig(config);
        assertConfig(uuid).
            hasBaseURL("https://baseurl.product.example.com/start").
            isNotEnabled().
            hasJobParameters(2);

        TestExecutorConfig config2 = new TestExecutorConfig();
        config2.productIdentifier=TestProductExecutorIdentifier.PDS_INFRASCAN.name();
        config2.name="pds gosec-1-renamed";
        config2.executorVersion=2;
        config2.enabled=true;
        config2.setup.baseURL="https://baseurl.product-changed.example.com/start";
        config2.setup.jobParameters.add(new TestExecutorSetupJobParam("key3","value3"));

        /* execute */
        as(SUPER_ADMIN).updateProdcutExecutorConfig(uuid,config2);

        /* test */
        assertConfig(uuid).
            hasBaseURL("https://baseurl.product-changed.example.com/start").
            hasName("pds gosec-1-renamed").
            hasJobParameters(1).
            isEnabled().
            hasJobParameter("key3", "value3");
        /* @formatter:on */
    }

    @Test
    public void an_admin_can_delete_former_created_product_executor_config() {

        /* prepare */
        TestExecutorConfig config = new TestExecutorConfig();
        config.productIdentifier = TestProductExecutorIdentifier.PDS_CODESCAN.name();
        config.name = "pds gosec-3";
        config.executorVersion = 1;
        config.setup.baseURL = "https://baseurl.product.example.com/start";

        UUID uuid = as(SUPER_ADMIN).createProductExecutorConfig(config);
        assertNotNull(uuid);

        /* execute */
        as(SUPER_ADMIN).deleteProductExecutorConfig(uuid);

        /* test */
        assertConfigDoesNotExist(uuid);
    }

    @Test
    public void an_admin_can_fetch_product_configuration_list() {

        /* prepare */
        TestExecutorConfig config = new TestExecutorConfig();
        config.productIdentifier = TestProductExecutorIdentifier.PDS_CODESCAN.name();
        config.name = "pds gosec-forlist-check";
        config.executorVersion = 1;
        config.enabled = true;
        config.setup.baseURL = "https://baseurl.product.example.com/start";

        UUID uuid = as(SUPER_ADMIN).createProductExecutorConfig(config);
        assertNotNull(uuid);

        /* execute */
        TestExecutorConfigList result = as(SUPER_ADMIN).fetchProductExecutorConfigList();

        /* execute + test */
        /* 2 invalid UUID results in 400 */
        expectHttpFailure(() -> as(SUPER_ADMIN).fetchProductExecutorConfigAsJSON("i-am-not-a-valid-uuid"), HttpStatus.BAD_REQUEST);

        /* test */
        TestExecutorConfigListEntry found = resolveEntry(uuid, result);
        if (found == null) {
            fail("Did not found an config list entry with uuid:" + uuid + "\n" + JSONConverter.get().toJSON(result, true));

        }
        assertEquals("pds gosec-forlist-check", found.name);
        assertTrue(found.enabled);

    }

    private TestExecutorConfigListEntry resolveEntry(UUID uuid, TestExecutorConfigList result) {
        TestExecutorConfigListEntry found = null;
        for (TestExecutorConfigListEntry entry : result.executorConfigurations) {
            if (entry.uuid.equals(uuid)) {
                found = entry;
                break;
            }
        }
        return found;
    }

}
