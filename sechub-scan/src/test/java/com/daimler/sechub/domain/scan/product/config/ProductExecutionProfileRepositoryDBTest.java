// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import static org.junit.Assert.*;

import java.util.Arrays;

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

import com.daimler.sechub.sharedkernel.Profiles;

@ActiveProfiles(Profiles.TEST)
@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { ProductExecutionProfile.class, ProductExecutionProfileRepositoryDBTest.SimpleTestConfiguration.class })
public class ProductExecutionProfileRepositoryDBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductExecutionProfileRepository repositoryToTest;

    @Before
    public void before() {
    }

    @Test
    public void can_remove_existing_project_relation() {
        /* prepare */
        String profileId = "1profile1";
        String projectId = "1projectA";

        createProfileWithProjectRelation(profileId, projectId);

        /* check preconditions */
        assertRelationShipExists(projectId, profileId);

        /* execute */
        repositoryToTest.deleteProfileRelationToProject(profileId, projectId);

        /* test */
        assertRelationShipDoesNotExist(projectId, profileId);

    }

    @Test
    public void can_remove_all_existing_project_relation_for_profile_but_not_influence_other_profiles() {
        /* prepare */
        String profileId1 = "2profile1";
        String profileId2 = "2profile2";

        String projectIdA = "2projectA";
        String projectIdB = "2projectB";
        

        createProfileWithProjectRelation(profileId1, projectIdA, projectIdB);
        createProfileWithProjectRelation(profileId2, projectIdA, projectIdB);

        /* check preconditions */
        assertRelationShipExists(projectIdA, profileId1);
        assertRelationShipExists(projectIdB, profileId1);
        assertRelationShipExists(projectIdA, profileId2);
        assertRelationShipExists(projectIdB, profileId2);

        /* execute */
        repositoryToTest.deleteAllProfileRelationsToProject(projectIdA);

        /* test */
        assertRelationShipDoesNotExist(projectIdA, profileId1);
        assertRelationShipExists(projectIdB, profileId1);
        assertRelationShipDoesNotExist(projectIdA, profileId2);
        assertRelationShipExists(projectIdB, profileId2);
    }

    @Test
    public void can_add_project_relation() {
        /* prepare */
        String projectId = "3projectB";
        String profileId = "3profile2";

        createProfile(profileId);

        /* execute */
        repositoryToTest.createProfileRelationToProject(profileId, projectId);

        /* test */
        assertRelationShipExists(projectId, profileId);

    }

    @Test
    public void count_0_when_not_exists() {
        /* prepare */
        String projectId = "4projectB";
        String profileId = "4profile2";

        createProfile(profileId);

        /* execute */
        int count = repositoryToTest.countRelationShipEntries(profileId, projectId);

        /* test */
        assertEquals(0, count);

    }

    @Test
    public void count_1_when__exists() {
        /* prepare */
        String profileId2 = "5profile2";
        String profileId3 = "5profile3";

        String projectIdB = "5projectB";
        String projectIdC = "5projectC";
        

        createProfileWithProjectRelation(profileId2, projectIdB,projectIdC);
        createProfileWithProjectRelation(profileId3, projectIdB,projectIdC);

        /* execute */
        int count = repositoryToTest.countRelationShipEntries(profileId2, projectIdB);

        /* test */
        assertEquals(1, count);

    }

    private ProductExecutionProfile createProfile(String profileId) {
        return createProfileWithProjectRelation(profileId);
    }

    private ProductExecutionProfile createProfileWithProjectRelation(String profileId, String... projectIds) {
        ProductExecutionProfile profile = new ProductExecutionProfile();
        profile.id = profileId;
        if (projectIds != null && projectIds.length > 0) {
            profile.projectIds.addAll(Arrays.asList(projectIds));
        }
        ProductExecutionProfile persisted = entityManager.persist(profile);

        entityManager.flush();
        entityManager.clear();
        return persisted;
    }

    protected void assertRelationShipExists(String projectId, String profileId) {
        assertRelationShipExists(projectId, profileId, true);
    }

    protected void assertRelationShipDoesNotExist(String projectId, String profileId) {
        assertRelationShipExists(projectId, profileId, false);
    }

    private void assertRelationShipExists(String projectId, String profileId, boolean expectedToExist) {
        entityManager.flush();
        entityManager.clear();

        ProductExecutionProfile loaded = entityManager.find(ProductExecutionProfile.class, profileId);
        assertEquals("Expected was that loaded project ids contains" + projectId + ":" + expectedToExist, expectedToExist,
                loaded.projectIds.contains(projectId));
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }

}
