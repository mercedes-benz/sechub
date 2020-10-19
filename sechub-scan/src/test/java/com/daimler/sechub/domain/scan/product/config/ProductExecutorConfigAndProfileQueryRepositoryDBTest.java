// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
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
@ContextConfiguration(classes = { ProductExecutorConfig.class, ProductExecutionProfile.class,
        ProductExecutorConfigAndProfileQueryRepositoryDBTest.SimpleTestConfiguration.class })
public class ProductExecutorConfigAndProfileQueryRepositoryDBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductExecutorConfigRepository repositoryToTest;

    @Autowired
    private ProductExecutionProfileRepository profileRepository;

    private ProductExecutorConfig config0;

    private ProductExecutorConfig config1;

    private ProductExecutorConfig config2;

    private ProductExecutorConfig config3;

    private ProductExecutorConfig config4;

    private ProductExecutorConfig config5;

    private ProductExecutionProfile profile0;

    private ProductExecutionProfile profile1_2;

    private ProductExecutionProfile profile3_4_5;

    @Before
    public void prepareDatabaseForAllOfTheseTests() {

        /* prepare */
        config0 = createConfig(ProductIdentifier.PDS_CODESCAN, 1);
        config1 = createConfig(ProductIdentifier.PDS_CODESCAN, 1);

        config2 = createConfig(ProductIdentifier.PDS_INFRASCAN, 1);

        config3 = createConfig(ProductIdentifier.PDS_WEBSCAN, 1);
        config4 = createConfig(ProductIdentifier.PDS_WEBSCAN, 1);
        config5 = createConfig(ProductIdentifier.PDS_WEBSCAN, 2);

        profile0 = createProfile("profile0", config0);
        profile1_2 = createProfile("profile1_2", config1, config2);
        profile3_4_5 = createProfile("profile3_4_5", config3, config4, config5);

        addProject(profile0, "project0");
        addProject(profile1_2, "project1", "project_1_to_5");
        addProject(profile3_4_5, "project3", "project_1_to_5");

        config0 = changeEnabledStateForConfig(config0, true);
        config1 = changeEnabledStateForConfig(config1, true);
        config2 = changeEnabledStateForConfig(config2, true);
        config3 = changeEnabledStateForConfig(config3, true);
        config4 = changeEnabledStateForConfig(config4, true);
        config5 = changeEnabledStateForConfig(config5, true);

        profile0 = changeEnabledStateForProfile(profile0, true);
        profile1_2 = changeEnabledStateForProfile(profile1_2, true);
        profile3_4_5 = changeEnabledStateForProfile(profile3_4_5, true);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void query_finds_executable_config_for_project0_codescan_v1() {

        test__project_0_parts();

        test__project_1_parts();

        test__project_1_to_5_parts();
        
        
        test__delete_all_project_1_relations();

    }

    protected void test__project_1_to_5_parts() {
        AssertSearch searchCode = searchingFor().project("project_1_to_5").executor(ProductIdentifier.PDS_CODESCAN, 1);
        AssertSearch searchInfra = searchingFor().project("project_1_to_5").executor(ProductIdentifier.PDS_INFRASCAN, 1);
        AssertSearch searchWeb1 = searchingFor().project("project_1_to_5").executor(ProductIdentifier.PDS_WEBSCAN, 1);
        AssertSearch searchWeb2 = searchingFor().project("project_1_to_5").executor(ProductIdentifier.PDS_WEBSCAN, 2);

        searchCode.assertFound(config1);
        searchInfra.assertFound(config2);
        searchWeb1.assertFound(config3,config4);
        searchWeb2.assertFound(config5);

        /* enable / disable on config 0 */
        config1 = changeEnabledStateForConfig(config1, false);
        searchCode.assertNothingFound();
        searchInfra.assertFound(config2);
        searchWeb1.assertFound(config3,config4);
        searchWeb2.assertFound(config5);
        
        config1 = changeEnabledStateForConfig(config1, true);
        searchCode.assertFound(config1);
        searchInfra.assertFound(config2);
        searchWeb1.assertFound(config3,config4);
        searchWeb2.assertFound(config5);

        /* disable profile1_2 */
        profile1_2 = changeEnabledStateForProfile(profile1_2, false);
        searchCode.assertNothingFound();
        searchInfra.assertNothingFound();
        searchWeb1.assertFound(config3,config4);
        searchWeb2.assertFound(config5);

        profile1_2 = changeEnabledStateForProfile(profile1_2, false);
        profile3_4_5= changeEnabledStateForProfile(profile3_4_5, false);
        searchCode.assertNothingFound();
        searchInfra.assertNothingFound();
        searchWeb1.assertNothingFound();
        searchWeb2.assertNothingFound();
        
        profile1_2 = changeEnabledStateForProfile(profile1_2, true);
        profile3_4_5= changeEnabledStateForProfile(profile3_4_5, true);
        searchCode.assertFound(config1);
        searchInfra.assertFound(config2);
        searchWeb1.assertFound(config3,config4);
        searchWeb2.assertFound(config5);

    }

    protected void test__project_1_parts() {
        AssertSearch searchCode = searchingFor().project("project1").executor(ProductIdentifier.PDS_CODESCAN, 1);
        AssertSearch searchInfra = searchingFor().project("project1").executor(ProductIdentifier.PDS_INFRASCAN, 1);
        searchCode.assertFound(config1);
        searchInfra.assertFound(config2);

        /* enable / disable on config 0 */
        config1 = changeEnabledStateForConfig(config1, false);
        searchCode.assertNothingFound();
        searchInfra.assertFound(config2);

        config1 = changeEnabledStateForConfig(config1, true);
        searchCode.assertFound(config1);
        searchInfra.assertFound(config2);

        /* disable profile */
        profile1_2 = changeEnabledStateForProfile(profile1_2, false);
        searchCode.assertNothingFound();
        searchInfra.assertNothingFound();

        profile1_2 = changeEnabledStateForProfile(profile1_2, true);
        searchCode.assertFound(config1);
        searchInfra.assertFound(config2);
    }

    private void test__delete_all_project_1_relations() {
        
        AssertSearch searchCode = searchingFor().project("project1").executor(ProductIdentifier.PDS_CODESCAN, 1);
        AssertSearch searchInfra = searchingFor().project("project1").executor(ProductIdentifier.PDS_INFRASCAN, 1);
        AssertSearch searchCode2 = searchingFor().project("project_1_to_5").executor(ProductIdentifier.PDS_CODESCAN, 1);
        
        searchCode.assertFound(config1);
        searchCode2.assertFound(config1);
        searchInfra.assertFound(config2);
        
        profileRepository.deleteAllProfileRelationsToProject("project1");
        
        searchCode.assertNothingFound();
        searchInfra.assertNothingFound();
        searchCode2.assertFound(config1);
    }
    
    protected void test__project_0_parts() {
        AssertSearch search = searchingFor().project("project0").executor(ProductIdentifier.PDS_CODESCAN, 1);
        /* enable / disable on config 0 */
        search.assertFound(config0);

        config0 = changeEnabledStateForConfig(config0, false);
        search.assertNothingFound();

        config0 = changeEnabledStateForConfig(config0, true);
        search.assertFound(config0);

        /* enable /disable on profile 0 */
        profile0 = changeEnabledStateForProfile(profile0, false);
        search.assertNothingFound();

        profile0 = changeEnabledStateForProfile(profile0, true);
        search.assertFound(config0);

    }

    protected class AssertSearch {
        private String projectId;
        private ProductIdentifier identifier;
        private int executorVersion;

        public AssertSearch project(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public AssertSearch executor(ProductIdentifier identifier, int version) {
            this.identifier = identifier;
            this.executorVersion = version;
            return this;
        }

        public void assertFound(ProductExecutorConfig... expectedConfigurations) {
            /* execute */
            List<ProductExecutorConfig> found = find(projectId, identifier, executorVersion);

            /* test */
            if (expectedConfigurations.length != found.size()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Not found expected size\nexpected:");
                sb.append(Arrays.asList(expectedConfigurations));
                sb.append("\nfound");
                sb.append(found);
                fail(sb.toString());
            }
            for (ProductExecutorConfig config : expectedConfigurations) {
                if (!found.contains(config)) {
                    fail("Did not contain:"+config);
                }
            }
        }

        public void assertNothingFound() {
            assertFound();
        }
    }

    private AssertSearch searchingFor() {
        return new AssertSearch();
    }

    private ProductExecutorConfig changeEnabledStateForConfig(ProductExecutorConfig config, boolean enabled) {
        config = repositoryToTest.findById(config.getUUID()).get();
        config.enabled = enabled;
        return repositoryToTest.save(config);
    }

    private ProductExecutionProfile changeEnabledStateForProfile(ProductExecutionProfile profile, boolean enabled) {
        profile = profileRepository.findById(profile.getId()).get();
        profile.enabled = enabled;
        return profileRepository.save(profile);
    }

    private List<ProductExecutorConfig> find(String projectId, ProductIdentifier identifier, int executorVersion) {
        List<ProductExecutorConfig> found = repositoryToTest.findExecutableConfigurationsForProject(projectId, identifier, executorVersion);
        return found;
    }

    private ProductExecutionProfile addProject(ProductExecutionProfile profile, String... projectIds) {
        for (String projectId : projectIds) {
            profile.projectIds.add(projectId);
        }
        return profileRepository.save(profile);
    }

    private ProductExecutionProfile createProfile(String profileId, ProductExecutorConfig... configs) {
        ProductExecutionProfile profile = new ProductExecutionProfile();
        profile.id = profileId;
        for (ProductExecutorConfig config : configs) {
            profile.configurations.add(config);
        }
        return profileRepository.save(profile);
    }

    private ProductExecutorConfig createConfig(ProductIdentifier productIdentifier, int executorVersion) {
        ProductExecutorConfig config = new ProductExecutorConfig();
        config.enabled = true;
        config.productIdentifier = productIdentifier;
        config.executorVersion = executorVersion;

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
        config.setup = setup;

        assertNull(config.getUUID());

        ProductExecutorConfig stored = repositoryToTest.save(config);
        UUID uuid = stored.getUUID();
        assertNotNull(uuid);
        return stored;
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
