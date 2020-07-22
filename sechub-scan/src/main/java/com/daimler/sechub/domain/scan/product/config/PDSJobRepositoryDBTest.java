// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Optional;

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
@ContextConfiguration(classes = { ProductExecutorConfig.class, PDSJobRepositoryDBTest.SimpleTestConfiguration.class })
public class PDSJobRepositoryDBTest {
	
    @Autowired
	private TestEntityManager entityManager;

	@Autowired
	private ProductExecutorConfigRepository repositoryToTest;
	

	@Before
	public void before() {
	}
	
	
	@Test
    public void can_store_pds_codescan_config_and_stored_part_has_uuuid() {
        /* prepare */
	    ProductExecutorConfig config = new ProductExecutorConfig();
	    config.setEnabled(true);
	    config.setProductIdentifier(ProductIdentifier.PDS_CODESCAN);
	    config.setExecutorVersion(1);
	    config.setSetup("setupdata");
	    
	    assertNull(config.getUUID());
        
        /* execute */
        ProductExecutorConfig stored = repositoryToTest.save(config);
        
        /* test */
        assertNotNull(stored.getUUID());

    }


	@TestConfiguration
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration{

	}


}
