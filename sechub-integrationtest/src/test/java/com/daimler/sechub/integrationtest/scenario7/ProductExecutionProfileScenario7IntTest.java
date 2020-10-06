// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario7;

import static com.daimler.sechub.integrationtest.api.AssertExecutionProfile.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.integrationtest.api.TestExecutorProductIdentifier;
import com.daimler.sechub.test.executionprofile.TestExecutionProfile;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

public class ProductExecutionProfileScenario7IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario7.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(30);

    private TestExecutionProfile profileData;

    @Test
    public void an_admin_can_create_a_new_empty_profile_() {
        /* prepare */
        profileData = new TestExecutionProfile();
        profileData.description="test1";
        
        String profileId = "s7p1-"+System.currentTimeMillis();
        
        
        /* execute */
        as(SUPER_ADMIN).createProductExecutionProfile(profileId,profileData);
        
        /* test */
        assertProfile(profileId).hasDescritpion("test1");

    }
    
    @Test
    public void an_admin_can_create_a_new_empty_profile_for_project1() {
        /* prepare */
        profileData = new TestExecutionProfile();
        profileData.description="test1";
        profileData.projectIds.add("project1");
        
        String profileId = "s7p1-"+System.currentTimeMillis();
        
        
        /* execute */
        as(SUPER_ADMIN).createProductExecutionProfile(profileId,profileData);
        
        /* test */
        assertProfile(profileId).hasDescritpion("test1").hasProjectIds("project1");

    }
    
    @Test
    public void an_admin_can_create_a_new_profile_containing_already_a_configuration() {
        /* prepare */
        profileData = new TestExecutionProfile();
        profileData.description="test1";
        profileData.projectIds.add("project1");
        
        String profileId = "s7p1-"+System.currentTimeMillis();
        
        UUID uuid = createTestExecutorConfig();
        
        TestExecutorConfig config = new TestExecutorConfig(uuid); // use created uuid for parameter
        
        profileData.configurations.add(config);
        
        /* execute */
        as(SUPER_ADMIN).createProductExecutionProfile(profileId,profileData);
        
        /* test */
        assertProfile(profileId).hasDescritpion("test1").hasConfigurations(uuid);

    }
    
    @Test
    public void an_admin_can_fetch_an_existing_profile_() {
        /* prepare */
        profileData = new TestExecutionProfile();
        profileData.description="test1234";
        profileData.projectIds.add("project1");
        
        String profileId = "s7p1-"+System.currentTimeMillis();
        
        
        UUID uuid = createTestExecutorConfig();
        
        TestExecutorConfig config = new TestExecutorConfig(uuid); // use created uuid for parameter
        profileData.configurations.add(config);
        
        as(SUPER_ADMIN).createProductExecutionProfile(profileId,profileData);
        
        /* execute */
        TestExecutionProfile result = as(SUPER_ADMIN).fetchProductExecutionProfile(profileId);
        
        /* test */
        assertNotNull(result);
        assertEquals("test1234",result.description);

    }

    @Test
    public void an_admin_can_delete_a_profile_containing__a_configuration() {
        /* prepare */
        TestExecutionProfile profileData = new TestExecutionProfile();
        profileData.description="test1";
        profileData.projectIds.add("project1");
        
        String profileId = "s7p1-"+System.currentTimeMillis();
        
        UUID uuid = createTestExecutorConfig();
        TestExecutorConfig config = new TestExecutorConfig(uuid); // use created uuid for parameter
        profileData.configurations.add(config);
        
        as(SUPER_ADMIN).createProductExecutionProfile(profileId,profileData);
        
        /* execute */
        as(SUPER_ADMIN).deleteProductExecutionProfile(profileId);
        
        /* test */
        assertProfileDoesNotExist(profileId);

    }
    
    @Test
    public void an_admin_can_update_a_profile_containing__a_configuration_and_define_another_projects() {
        /* prepare */
        TestExecutionProfile profileData = new TestExecutionProfile();
        profileData.description="test1";
        profileData.projectIds.add("project1");
        
        String profileId = "s7p1-"+System.currentTimeMillis();
        
        UUID uuid = createTestExecutorConfig();
        TestExecutorConfig config = new TestExecutorConfig(uuid);
        profileData.configurations.add(config);
        
        as(SUPER_ADMIN).createProductExecutionProfile(profileId,profileData);
        
        /* @formatter:off */
        assertProfile(profileId).
            hasDescritpion("test1").
            isNotEnabled().
            hasConfigurations(uuid).
            hasProjectIds("project1");
        
        /* now change local data - so changed when update done */
        profileData.projectIds.clear(); // removes project1
        profileData.projectIds.add("project2");
        profileData.projectIds.add("project3");
        profileData.description="changed description";
        profileData.enabled=true;
        
        /* execute */
        as(SUPER_ADMIN).updateProductExecutionProfile(profileId,profileData);
        
        /* test */
        assertProfile(profileId).
            isEnabled().
            hasDescritpion("changed description").
            hasConfigurations(uuid).
            hasProjectIds("project2","project3");
        /* @formatter:on */

    }
    
    private UUID createTestExecutorConfig() {
        TestExecutorConfig config = new TestExecutorConfig();
        config.productIdentifier=TestExecutorProductIdentifier.PDS_CODESCAN.name();
        config.name="pds gosec-1";
        config.executorVersion=1;
        config.enabled=false;
        config.setup.baseURL="https://baseurl.product.example.com/start";
        
        UUID uuid = as(SUPER_ADMIN).
                createProductExecutorConfig(config);
        return uuid;
    }
    

}
