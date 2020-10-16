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
import com.daimler.sechub.integrationtest.api.TestProject;
import com.daimler.sechub.test.executionprofile.TestExecutionProfile;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

public class ProductExecutionProfileScenario7IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario7.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(30);

    private TestExecutionProfile profileData;

    /* @formatter:off */
    @Test
    public void an_admin_can_add_and_also_remove_project_relations() {
        /* prepare */
        String profileId = "test-profile-to-delete10";
        dropExecutionProfileIfExisting(profileId);
        
        TestProject tempProject = setup.getScenario().newTestProject();
        String tempProjectProjectId = tempProject.getProjectId();
        
        TestProject tempProject2 = setup.getScenario().newTestProject();
        String tempProjectProject2Id = tempProject2.getProjectId();
        
        as(SUPER_ADMIN).
            createProductExecutionProfile(profileId,  new TestExecutionProfile()).
            createProject(tempProject,Scenario7.OWNER_1.getUserId()).
            addProjectsToProfile(profileId, tempProject,tempProject2);
        
        /* check precondition */
        assertProfile(profileId).hasProjectIds(tempProjectProjectId,tempProjectProject2Id);
        
        /* execute */
        as(SUPER_ADMIN).removeProjectIdsFromProfile(profileId, tempProjectProjectId);

        /* test */
        assertProfile(profileId).hasProjectIds(tempProjectProject2Id);
    }
    /* @formatter:on */
    

    /* @formatter:off */
    @Test
    public void an_admin_deletes_a_project__automatically_removes_project_from_profile() {
        /* prepare */
        String profileId = "test-profile-to-delete1";
        dropExecutionProfileIfExisting(profileId);
        
        TestProject tempProject = setup.getScenario().newTestProject();
        String tempProjectProjectId = tempProject.getProjectId();
        
        as(SUPER_ADMIN).
            createProductExecutionProfile(profileId,  new TestExecutionProfile()).
            createProject(tempProject,Scenario7.OWNER_1.getUserId()).
            addProjectsToProfile(profileId, tempProject);
        
        /* check precondition */
        assertProfile(profileId).hasProjectIds(tempProjectProjectId);
        
        /* execute */
        as(SUPER_ADMIN).deleteProject(tempProject);

        /* test */
        assertProfile(profileId).hasNoProjectIds();
    }
    /* @formatter:on */

    /* @formatter:off */
    @Test
    public void an_admin_can_delete_a_profile_when_a_project_is_related_and_also_add_again() {
        /* prepare */
        String profileId = "test-profile-to-delete2";
        dropExecutionProfileIfExisting(profileId);
        
        TestProject tempProject = setup.getScenario().newTestProject();
        String tempProjectProjectId = tempProject.getProjectId();
        
        TestExecutorConfig tempConfig = new TestExecutorConfig();
        tempConfig.name="config1";
        tempConfig.setup.baseURL="something";
        tempConfig.productIdentifier="PDS_CODESCAN";
        tempConfig.executorVersion=42;
        
        UUID uuid1 = as(SUPER_ADMIN).
            createProductExecutorConfig(tempConfig);
        
        tempConfig.name="config2";
        UUID uuid2 = as(SUPER_ADMIN).
            createProductExecutorConfig(tempConfig);
        
        as(SUPER_ADMIN).
            createProductExecutionProfile(profileId,  new TestExecutionProfile()).
            createProject(tempProject,Scenario7.OWNER_1.getUserId()).
            addConfigurationToProfile(profileId,uuid1,uuid2).
            addProjectsToProfile(profileId, tempProject);
        
        /* check precondition */
        assertProfile(profileId).
            hasProjectIds(tempProjectProjectId).
            hasConfigurations(uuid1,uuid2);
        
        /* execute */
        as(SUPER_ADMIN).deleteProductExecutionProfile(profileId);

        /* test */
        assertProfileDoesNotExist(profileId);
        
        /* prepare 2 - recrate same profile*/
        as(SUPER_ADMIN).
            createProductExecutionProfile(profileId,  new TestExecutionProfile());

        /* test 2 - new project (with same id as before) has no relation to project, so really deleted */
        assertProfile(profileId).
            hasNoProjectIds().
            hasNoConfigurations();
        
        /* prepare 3 - add relation again */
        as(SUPER_ADMIN).
            addProjectsToProfile(profileId, tempProject);
        
        /* check relation is working again */
        assertProfile(profileId).
            hasNoConfigurations().
            hasProjectIds(tempProjectProjectId);
    }
    /* @formatter:on */
    
    @Test
    public void an_admin_can_create_a_new_empty_profile_() {
        /* prepare */
        String profileId = "test-profile-to-delete3";
        dropExecutionProfileIfExisting(profileId);

        profileData = new TestExecutionProfile();
        profileData.description = "test1";


        /* execute */
        as(SUPER_ADMIN).createProductExecutionProfile(profileId, profileData);

        /* test */
        assertProfile(profileId).hasDescritpion("test1");

    }
    
    /* @formatter:off */
    @Test
    public void sanity_check_remove_projects_implemented_correct_for_daui() {
        /* prepare */
        String profileId = "test-profile-to-delete9";
        dropExecutionProfileIfExisting(profileId);

        profileData = new TestExecutionProfile();
        profileData.description = "test9";
        
        TestExecutorConfig config = new TestExecutorConfig();
        config.name="config1";
        config.executorVersion=33;
        config.productIdentifier="PDS_WEBSCAN";
        config.setup.baseURL="";
        
        UUID uuid1 = as(SUPER_ADMIN).createProductExecutorConfig(config);
        
        config.name="config2";
        UUID uuid2 = as(SUPER_ADMIN).createProductExecutorConfig(config);
        
        config.name="config3";
        UUID uuid3 = as(SUPER_ADMIN).createProductExecutorConfig(config);

        as(SUPER_ADMIN).
            createProductExecutionProfile(profileId, profileData).
            addConfigurationToProfile(profileId, uuid1, uuid2,uuid3).
            addProjectIdsToProfile(profileId, "p1","p2","p3");

        /* check preconditions */
        assertProfile(profileId).hasConfigurations(uuid1,uuid2,uuid3).hasProjectIds("p1","p2","p3");
        /* execute */
        as(SUPER_ADMIN).removeConfigurationFromProfile(profileId, uuid1,uuid3);
        as(SUPER_ADMIN).removeProjectIdsFromProfile(profileId, "p1","p3");
        
        /* test */
        assertProfile(profileId).hasConfigurations(uuid2).hasProjectIds("p2");
    }
    /* @formatter:on */

    @Test
    public void an_admin_can_create_a_new_empty_profile_for_project1() {
        /* prepare */
        String profileId = "test-profile-to-delete4";
        dropExecutionProfileIfExisting(profileId);
        
        profileData = new TestExecutionProfile();
        profileData.description = "test1";
        profileData.projectIds.add("project1");

        /* execute */
        as(SUPER_ADMIN).createProductExecutionProfile(profileId, profileData);

        /* test */
        assertProfile(profileId).hasDescritpion("test1").hasProjectIds("project1");

    }

    @Test
    public void an_admin_can_create_a_new_profile_containing_already_a_configuration() {
        /* prepare */
        String profileId = "test-profile-to-delete5";
        dropExecutionProfileIfExisting(profileId);
        
        profileData = new TestExecutionProfile();
        profileData.description = "test1";
        profileData.projectIds.add("project1");

        UUID uuid = createTestExecutorConfig();

        TestExecutorConfig config = new TestExecutorConfig(uuid); // use created uuid for parameter

        profileData.configurations.add(config);

        /* execute */
        as(SUPER_ADMIN).createProductExecutionProfile(profileId, profileData);

        /* test */
        assertProfile(profileId).hasDescritpion("test1").hasConfigurations(uuid);

    }

    @Test
    public void an_admin_can_fetch_an_existing_profile_() {
        /* prepare */
        String profileId = "test-profile-to-delete6";
        dropExecutionProfileIfExisting(profileId);

        profileData = new TestExecutionProfile();
        profileData.description = "test1234";
        profileData.projectIds.add("project1");


        UUID uuid = createTestExecutorConfig();

        TestExecutorConfig config = new TestExecutorConfig(uuid); // use created uuid for parameter
        profileData.configurations.add(config);

        as(SUPER_ADMIN).createProductExecutionProfile(profileId, profileData);

        /* execute */
        TestExecutionProfile result = as(SUPER_ADMIN).fetchProductExecutionProfile(profileId);

        /* test */
        assertNotNull(result);
        assertEquals("test1234", result.description);

    }

    @Test
    public void an_admin_can_delete_a_profile_containing__a_configuration() {
        /* prepare */
        String profileId = "test-profile-to-delete7";
        dropExecutionProfileIfExisting(profileId);
        
        TestExecutionProfile profileData = new TestExecutionProfile();
        profileData.description = "test1";
        profileData.projectIds.add("project1");


        UUID uuid = createTestExecutorConfig();
        TestExecutorConfig config = new TestExecutorConfig(uuid); // use created uuid for parameter
        profileData.configurations.add(config);

        as(SUPER_ADMIN).createProductExecutionProfile(profileId, profileData);

        /* execute */
        as(SUPER_ADMIN).deleteProductExecutionProfile(profileId);

        /* test */
        assertProfileDoesNotExist(profileId);

    }

    @Test
    public void an_admin_can_NOT_update_a_profile_containing__a_configuration_and_define_another_projects() {
        /* prepare */
        String profileId = "test-profile-to-delete8";
        dropExecutionProfileIfExisting(profileId);
        
        TestExecutionProfile profileData = new TestExecutionProfile();
        profileData.description = "test1";
        profileData.projectIds.add("project1");

        UUID uuid = createTestExecutorConfig();
        TestExecutorConfig config = new TestExecutorConfig(uuid);
        profileData.configurations.add(config);

        as(SUPER_ADMIN).createProductExecutionProfile(profileId, profileData);

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
            hasProjectIds("project1");
        /* @formatter:on */

    }
   

    private UUID createTestExecutorConfig() {
        TestExecutorConfig config = new TestExecutorConfig();
        config.productIdentifier = TestExecutorProductIdentifier.PDS_CODESCAN.name();
        config.name = "pds gosec-1";
        config.executorVersion = 1;
        config.enabled = false;
        config.setup.baseURL = "https://baseurl.product.example.com/start";

        UUID uuid = as(SUPER_ADMIN).createProductExecutorConfig(config);
        return uuid;
    }

}
