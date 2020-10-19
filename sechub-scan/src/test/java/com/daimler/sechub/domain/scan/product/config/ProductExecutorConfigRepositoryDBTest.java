// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.sharedkernel.Profiles;

@ActiveProfiles(Profiles.TEST)
@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { ProductExecutorConfig.class, ProductExecutorConfigRepositoryDBTest.SimpleTestConfiguration.class })
public class ProductExecutorConfigRepositoryDBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductExecutorConfigRepository repositoryToTest;

    @Before
    public void before() {
    }

    @Test
    public void can_store_pds_codescan_config_with_bigger_config_setup_and_stored_part_has_uuuid() {
        /* prepare */
        ProductExecutorConfig config = new ProductExecutorConfig();
        config.enabled=true;
        config.productIdentifier=ProductIdentifier.PDS_CODESCAN;
        config.executorVersion=1;
        ProductExecutorConfigSetup setup = new ProductExecutorConfigSetup();
        setup.getCredentials().setPassword(createPseudostring(255, 'p'));
        setup.getCredentials().setUser(createPseudostring(20, 'u'));

        List<ProductExecutorConfigSetupJobParameter> params = setup.getJobParameters();
        for (int i = 0; i < 20; i++) {
            ProductExecutorConfigSetupJobParameter param = new ProductExecutorConfigSetupJobParameter();
            param.setKey(i + "_" + createPseudostring(30, 'k'));
            param.setValue((i + "_" + createPseudostring(200, 'v')));
            params.add(param);
        }
        setup.setBaseURL("https://www.example.com/somewhere/very/special/target");
        config.setup=setup;

        assertNull(config.getUUID());

        /* execute */
        ProductExecutorConfig stored = repositoryToTest.save(config);

        /* test */
        UUID uuid = stored.getUUID();
        assertNotNull(uuid);
        
        entityManager.flush();
        entityManager.clear();
        
        Optional<ProductExecutorConfig> found = repositoryToTest.findById(uuid);
        assertTrue(found.isPresent());
        ProductExecutorConfigSetup setup2 = found.get().getSetup();
        assertEquals(255, setup2.getCredentials().getPassword().length());
        assertEquals("https://www.example.com/somewhere/very/special/target", setup2.getBaseURL());

    }

    private String createPseudostring(int max, char c) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < max; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }

}
