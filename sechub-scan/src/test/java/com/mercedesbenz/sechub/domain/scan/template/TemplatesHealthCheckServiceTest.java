package com.mercedesbenz.sechub.domain.scan.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.domain.scan.asset.AssetDetailData;
import com.mercedesbenz.sechub.domain.scan.asset.AssetFileData;
import com.mercedesbenz.sechub.domain.scan.asset.AssetService;
import com.mercedesbenz.sechub.domain.scan.asset.ProductExecutorConfigAssetFileNameService;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfile;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfileRepository;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;

class TemplatesHealthCheckServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(TemplatesHealthCheckServiceTest.class);

    private static final String NON_EXISTING_TEMPLATE_ID = "non-existing-template-id";
    private static final String ASSET1_EXECUTOR_CONFIG_FILENAME1 = "pds-product1-id.zip";
    private static final String ASSET1_EXECUTOR_CONFIG_FILENAME2 = "pds-product2-id.zip";

    private static final String PROFILE1_ID = "profile-1";
    private static final String PROFILE2_ID = "profile-2";

    private static final String ASSET1_ID = "asset-1";

    private static final String TEMPLATE1_ID = "template-1";

    private static final String PROJECT1_ID = "project-1";
    private static final String PROJECT2_ID = "project-2";

    private TemplatesHealthCheckService serviceToTest;

    @Mock
    TemplateService templateService;

    @Mock
    AssetService assetService;

    @Mock(name = "profileRepository1")
    ProductExecutionProfileRepository profileRepository;

    @Mock
    RelevantScanTemplateDefinitionFilter templateDefinitionFilter;

    @Mock
    ProductExecutorConfigAssetFileNameService assetFileNameService;

    @BeforeAll
    static void beforeAll() {

    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        serviceToTest = new TemplatesHealthCheckService(templateService, assetService, profileRepository, templateDefinitionFilter, assetFileNameService);
    }

    @Test
    void mock_defaults_return_an_empty_result() throws Exception {

        /* execute */
        TemplatesHealthCheckResult result = serviceToTest.executeHealthCheck();

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.OK);

    }

    @Test
    void template_defined_but_not_assigned_to_any_project() {
        /* prepare */
        prepareTemplate1AvailableButNotAssignedToAnyProject();

        /* execute */
        TemplatesHealthCheckResult result = serviceToTest.executeHealthCheck();

        /* test */
        dumpIfEnabled(result);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.OK);
        assertThat(result.getEntries()).hasSize(1);

        TemplateHealthCheckEntry first = result.getEntries().iterator().next();
        assertThat(first.getType()).isEqualTo(TemplateHealthCheckProblemType.INFO);
        assertThat(first.getTemplateId()).isEqualTo(TEMPLATE1_ID);
        assertThat(first.getDescription()).contains("not assigned to any project");
    }

    @Test
    void template_not_existing_but_defined_in_project() {
        /* prepare */
        prepareTemplate1AvailableButNotAssignedToAnyProject();
        when(templateService.fetchAllAssignedTemplateIds()).thenReturn(Set.of(TEMPLATE1_ID, NON_EXISTING_TEMPLATE_ID));
        when(templateService.fetchProjectIdsUsingTemplate(NON_EXISTING_TEMPLATE_ID)).thenReturn(Set.of("p1", "p2", "p3"));

        /* execute */
        TemplatesHealthCheckResult result = serviceToTest.executeHealthCheck();

        /* test */
        dumpIfEnabled(result);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.ERROR);
        assertThat(result.getEntries()).hasSize(1);

        TemplateHealthCheckEntry first = result.getEntries().iterator().next();
        assertThat(first.getType()).isEqualTo(TemplateHealthCheckProblemType.ERROR);
        assertThat(first.getTemplateId()).isEqualTo(NON_EXISTING_TEMPLATE_ID);
        assertThat(first.getDescription()).contains("template").contains("does not exist but is assigned to project(s)");
        assertThat(first.getProjects()).contains("p1", "p2", "p3");

    }

    @Test
    void template_defined_and_assigned_to_project1_but_no_profiles() {

        /* prepare */
        TemplateDefinition templateDefinition = prepareTemplate1AvailableAndAssignedToProjects(PROJECT1_ID);
        when(profileRepository.findExecutionProfilesForProject(PROJECT1_ID)).thenReturn(List.of());

        ensureTemplateSupportedByScanType(templateDefinition);

        /* execute */
        TemplatesHealthCheckResult result = serviceToTest.executeHealthCheck();

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.OK);
        assertThat(result.getEntries()).hasSize(0);

    }

    @Test
    void template_defined_and_assigned_to_project1_profile_but_no_executor() {

        /* prepare */
        ProductExecutionProfile profile1 = mock();
        when(profile1.getId()).thenReturn(PROFILE1_ID);

        TemplateDefinition templateDefinition = prepareTemplate1AvailableAndAssignedToProjects(PROJECT1_ID);

        when(profileRepository.findExecutionProfilesForProject(PROJECT1_ID)).thenReturn(List.of(profile1));

        ensureTemplateSupportedByScanType(templateDefinition);

        /* execute */
        TemplatesHealthCheckResult result = serviceToTest.executeHealthCheck();

        /* test */
        dumpIfEnabled(result);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.OK);
        assertThat(result.getEntries()).hasSize(0);

    }

    @Test
    void template_defined_and_assigned_to_project1_profile_and_executor_but_file_missing() {
        ProductExecutionProfile profile1 = mock();
        when(profile1.getId()).thenReturn(PROFILE1_ID);
        ProductExecutorConfig config1 = mock();
        UUID executorConfigUUID = UUID.randomUUID();

        when(config1.getUUID()).thenReturn(executorConfigUUID);
        ProductIdentifier pid = ProductIdentifier.PDS_WEBSCAN;
        when(config1.getProductIdentifier()).thenReturn(pid);

        when(profile1.getConfigurations()).thenReturn(Set.of(config1));

        /* prepare */
        TemplateDefinition templateDefinition = prepareTemplate1AvailableAndAssignedToProjects(PROJECT1_ID);

        when(assetFileNameService.resolveAssetFileName(config1)).thenReturn(ASSET1_EXECUTOR_CONFIG_FILENAME1);

        when(profileRepository.findExecutionProfilesForProject(PROJECT1_ID)).thenReturn(List.of(profile1));

        ensureTemplateSupportedByScanType(templateDefinition);

        /* execute */
        TemplatesHealthCheckResult result = serviceToTest.executeHealthCheck();

        /* test */
        dumpIfEnabled(result);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.WARNING);
        assertThat(result.getEntries()).hasSize(1);

        TemplateHealthCheckEntry first = result.getEntries().iterator().next();
        assertThat(first.getType()).isEqualTo(TemplateHealthCheckProblemType.WARNING);
        assertThat(first.getTemplateId()).isEqualTo(TEMPLATE1_ID);
        assertThat(first.getDescription()).contains("file").contains("does not exist").contains(ASSET1_EXECUTOR_CONFIG_FILENAME1);
        assertThat(first.getProfiles()).contains(PROFILE1_ID);
        assertThat(first.getProjects()).contains(PROJECT1_ID);
    }

    @Test
    void template_defined_and_assigned_to_project1_and_project2_same_profile_and_executor_but_file_missing() {
        ProductExecutionProfile profile1 = mock();
        when(profile1.getId()).thenReturn(PROFILE1_ID);
        ProductExecutorConfig config1 = mock();
        UUID executorConfigUUID = UUID.randomUUID();

        when(config1.getUUID()).thenReturn(executorConfigUUID);
        ProductIdentifier pid = ProductIdentifier.PDS_WEBSCAN;
        when(config1.getProductIdentifier()).thenReturn(pid);

        when(profile1.getConfigurations()).thenReturn(Set.of(config1));

        /* prepare */
        TemplateDefinition templateDefinition = prepareTemplate1AvailableAndAssignedToProjects(PROJECT1_ID, PROJECT2_ID);

        when(assetFileNameService.resolveAssetFileName(config1)).thenReturn(ASSET1_EXECUTOR_CONFIG_FILENAME1);

        when(profileRepository.findExecutionProfilesForProject(PROJECT1_ID)).thenReturn(List.of(profile1));
        when(profileRepository.findExecutionProfilesForProject(PROJECT2_ID)).thenReturn(List.of(profile1));

        ensureTemplateSupportedByScanType(templateDefinition);

        /* execute */
        TemplatesHealthCheckResult result = serviceToTest.executeHealthCheck();

        /* test */
        dumpIfEnabled(result);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.WARNING); // warning, because profile/executor is not enabled
        assertThat(result.getEntries()).hasSize(1);

        TemplateHealthCheckEntry first = result.getEntries().iterator().next();
        assertThat(first.getType()).isEqualTo(TemplateHealthCheckProblemType.WARNING);
        assertThat(first.getTemplateId()).isEqualTo(TEMPLATE1_ID);
        assertThat(first.getDescription()).contains("file").contains("does not exist").contains(ASSET1_EXECUTOR_CONFIG_FILENAME1);
        assertThat(first.getProfiles()).contains(PROFILE1_ID);
        assertThat(first.getProjects()).contains(PROJECT1_ID, PROJECT2_ID);
    }

    @Test
    void warn_profiles_not_enabled_template_defined_and_assigned_to_project1_and_project2_different_profiles_same_executor_but_same_file_missing() {
        template_defined_and_assigned_to_project1_and_project2_different_profiles_same_executor_but_same_file_missing(false, true);
    }

    @Test
    void warn_profiles_and_executor_not_enabled_template_defined_and_assigned_to_project1_and_project2_different_profiles_same_executor_but_same_file_missing() {
        template_defined_and_assigned_to_project1_and_project2_different_profiles_same_executor_but_same_file_missing(false, false);
    }

    @Test
    void warn_executor_not_enabled_template_defined_and_assigned_to_project1_and_project2_different_profiles_same_executor_but_same_file_missing() {
        template_defined_and_assigned_to_project1_and_project2_different_profiles_same_executor_but_same_file_missing(true, false);
    }

    @Test
    void error_template_defined_and_assigned_to_project1_and_project2_different_profiles_same_executor_but_same_file_missing() {
        template_defined_and_assigned_to_project1_and_project2_different_profiles_same_executor_but_same_file_missing(true, true);
    }

    void template_defined_and_assigned_to_project1_and_project2_different_profiles_same_executor_but_same_file_missing(boolean profilesEnabled,
            boolean executorConfigEnabled) {

        boolean expectOnlyWarning = !profilesEnabled || !executorConfigEnabled;

        ProductExecutionProfile profile1 = mock();
        when(profile1.getId()).thenReturn(PROFILE1_ID);
        when(profile1.getEnabled()).thenReturn(profilesEnabled);
        ProductExecutorConfig config1 = mock();
        when(config1.getEnabled()).thenReturn(executorConfigEnabled);
        UUID executorConfigUUID = UUID.randomUUID();

        ProductExecutionProfile profile2 = mock();
        when(profile2.getId()).thenReturn(PROFILE2_ID);
        when(profile2.getEnabled()).thenReturn(profilesEnabled);

        when(config1.getUUID()).thenReturn(executorConfigUUID);
        ProductIdentifier pid = ProductIdentifier.PDS_WEBSCAN;
        when(config1.getProductIdentifier()).thenReturn(pid);

        when(profile1.getConfigurations()).thenReturn(Set.of(config1));
        when(profile2.getConfigurations()).thenReturn(Set.of(config1));

        /* prepare */
        TemplateDefinition templateDefinition = new TemplateDefinition();
        templateDefinition.setAssetId(ASSET1_ID);
        templateDefinition.setType(TemplateType.WEBSCAN_LOGIN);
        templateDefinition.setId(TEMPLATE1_ID);

        when(templateService.fetchAllTemplateIds()).thenReturn(List.of(TEMPLATE1_ID));
        when(templateService.fetchAllAssignedTemplateIds()).thenReturn(Set.of(TEMPLATE1_ID));
        when(templateService.fetchTemplateDefinition(TEMPLATE1_ID)).thenReturn(templateDefinition);
        when(templateService.fetchAllTemplateIds()).thenReturn(List.of(TEMPLATE1_ID));
        when(templateService.fetchProjectIdsUsingTemplate(TEMPLATE1_ID)).thenReturn(Set.of(PROJECT1_ID, PROJECT2_ID));

        when(assetFileNameService.resolveAssetFileName(config1)).thenReturn(ASSET1_EXECUTOR_CONFIG_FILENAME1);

        when(profileRepository.findExecutionProfilesForProject(PROJECT1_ID)).thenReturn(List.of(profile1));
        when(profileRepository.findExecutionProfilesForProject(PROJECT2_ID)).thenReturn(List.of(profile2));

        ensureTemplateSupportedByScanType(templateDefinition);

        /* execute */
        TemplatesHealthCheckResult result = serviceToTest.executeHealthCheck();

        /* test */
        dumpIfEnabled(result);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(expectOnlyWarning ? TemplatesHealthCheckStatus.WARNING : TemplatesHealthCheckStatus.ERROR);
        assertThat(result.getEntries()).hasSize(1);

        TemplateHealthCheckEntry first = result.getEntries().iterator().next();
        assertThat(first.getType()).isEqualTo(expectOnlyWarning ? TemplateHealthCheckProblemType.WARNING : TemplateHealthCheckProblemType.ERROR);
        assertThat(first.getTemplateId()).isEqualTo(TEMPLATE1_ID);
        assertThat(first.getDescription()).contains("file").contains("does not exist").contains(ASSET1_EXECUTOR_CONFIG_FILENAME1);
        assertThat(first.getProfiles()).contains(PROFILE1_ID).contains(PROFILE2_ID).hasSize(2);
        assertThat(first.getProjects()).contains(PROJECT1_ID, PROJECT2_ID).hasSize(2);

        if (executorConfigEnabled) {
            assertThat(first.getHints()).contains("At least one executor config is enabled.");
        } else {
            assertThat(first.getHints()).contains("At least one executor config is not enabled.");
        }

        if (profilesEnabled) {
            assertThat(first.getHints()).contains("At least one profile is enabled.");
        } else {
            assertThat(first.getHints()).contains("At least one profile is not enabled.");
        }

        if (profilesEnabled && executorConfigEnabled) {
            assertThat(first.getHints()).contains("At least one combination of executor and profile is enabled.");
        }
    }

    @Test
    void template_defined_and_assigned_to_project1_and_project2_different_profiles_different_executors_but_same_file_missing() {
        ProductExecutionProfile profile1 = mock();
        when(profile1.getId()).thenReturn(PROFILE1_ID);

        ProductExecutionProfile profile2 = mock();
        when(profile2.getId()).thenReturn(PROFILE2_ID);

        ProductIdentifier pid = ProductIdentifier.PDS_WEBSCAN;
        ProductExecutorConfig config1 = mock();
        UUID executorConfig1UUID = UUID.fromString("349ea899-e780-4553-bd50-06c12fe96c9e");
        when(config1.getUUID()).thenReturn(executorConfig1UUID);
        when(config1.getProductIdentifier()).thenReturn(pid);

        ProductExecutorConfig config2 = mock();
        UUID executorConfig2UUID = UUID.fromString("2b25b007-f3d2-4591-ba42-409e19d9a5e8");
        when(config2.getUUID()).thenReturn(executorConfig2UUID);
        when(config2.getProductIdentifier()).thenReturn(pid);

        when(profile1.getConfigurations()).thenReturn(Set.of(config1));
        when(profile2.getConfigurations()).thenReturn(Set.of(config2));

        TemplateDefinition templateDefinition = prepareTemplate1AvailableAndAssignedToProjects(PROJECT1_ID, PROJECT2_ID);

        when(assetFileNameService.resolveAssetFileName(config1)).thenReturn(ASSET1_EXECUTOR_CONFIG_FILENAME1);
        when(assetFileNameService.resolveAssetFileName(config2)).thenReturn(ASSET1_EXECUTOR_CONFIG_FILENAME1);

        when(profileRepository.findExecutionProfilesForProject(PROJECT1_ID)).thenReturn(List.of(profile1));
        when(profileRepository.findExecutionProfilesForProject(PROJECT2_ID)).thenReturn(List.of(profile2));

        ensureTemplateSupportedByScanType(templateDefinition);

        /* execute */
        TemplatesHealthCheckResult result = serviceToTest.executeHealthCheck();

        /* test */
        dumpIfEnabled(result);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.WARNING);
        assertThat(result.getEntries()).hasSize(2);

        Iterator<TemplateHealthCheckEntry> iterator = result.getEntries().iterator();
        TemplateHealthCheckEntry first = iterator.next();
        TemplateHealthCheckEntry second = iterator.next();
        if (first.getExecutorConfigUUID().equals(executorConfig2UUID.toString())) {
            /*
             * the result is not sorted - to be deterministic we must change for testing to
             * avoid flaky tests:
             */
            TemplateHealthCheckEntry formerFirst = first;
            first = second;
            second = formerFirst;
        }
        assertThat(first.getType()).isEqualTo(TemplateHealthCheckProblemType.WARNING);
        assertThat(first.getTemplateId()).isEqualTo(TEMPLATE1_ID);
        assertThat(first.getDescription()).contains("file").contains("does not exist").contains(ASSET1_EXECUTOR_CONFIG_FILENAME1);
        assertThat(first.getProfiles()).contains(PROFILE1_ID).hasSize(1);
        assertThat(first.getProjects()).contains(PROJECT1_ID).hasSize(1);

        assertThat(second.getType()).isEqualTo(TemplateHealthCheckProblemType.WARNING);
        assertThat(second.getTemplateId()).isEqualTo(TEMPLATE1_ID);
        assertThat(second.getDescription()).contains("file").contains("does not exist").contains(ASSET1_EXECUTOR_CONFIG_FILENAME1);
        assertThat(second.getProfiles()).contains(PROFILE2_ID).hasSize(1);
        assertThat(second.getProjects()).contains(PROJECT2_ID).hasSize(1);

    }

    @Test
    void template_defined_and_assigned_to_project1_and_project2_different_profiles_different_executors_two_files_missing() {
        ProductExecutionProfile profile1 = mock();
        when(profile1.getId()).thenReturn(PROFILE1_ID);

        ProductExecutionProfile profile2 = mock();
        when(profile2.getId()).thenReturn(PROFILE2_ID);

        ProductIdentifier pid = ProductIdentifier.PDS_WEBSCAN;
        ProductExecutorConfig config1 = mock();
        UUID executorConfig1UUID = UUID.fromString("349ea899-e780-4553-bd50-06c12fe96c9e");
        when(config1.getUUID()).thenReturn(executorConfig1UUID);
        when(config1.getProductIdentifier()).thenReturn(pid);

        ProductExecutorConfig config2 = mock();
        UUID executorConfig2UUID = UUID.fromString("2b25b007-f3d2-4591-ba42-409e19d9a5e8");
        when(config2.getUUID()).thenReturn(executorConfig2UUID);
        when(config2.getProductIdentifier()).thenReturn(pid);

        when(profile1.getConfigurations()).thenReturn(Set.of(config1));
        when(profile2.getConfigurations()).thenReturn(Set.of(config2));

        when(profile1.getEnabled()).thenReturn(true);
        when(config1.getEnabled()).thenReturn(true);

        when(profile2.getEnabled()).thenReturn(true);
        when(config2.getEnabled()).thenReturn(false); // executor configuration 2 is disabled!

        TemplateDefinition templateDefinition = prepareTemplate1AvailableAndAssignedToProjects(PROJECT1_ID, PROJECT2_ID);

        when(assetFileNameService.resolveAssetFileName(config1)).thenReturn(ASSET1_EXECUTOR_CONFIG_FILENAME1);
        when(assetFileNameService.resolveAssetFileName(config2)).thenReturn(ASSET1_EXECUTOR_CONFIG_FILENAME2);

        when(profileRepository.findExecutionProfilesForProject(PROJECT1_ID)).thenReturn(List.of(profile1));
        when(profileRepository.findExecutionProfilesForProject(PROJECT2_ID)).thenReturn(List.of(profile2));

        ensureTemplateSupportedByScanType(templateDefinition);

        /* execute */
        TemplatesHealthCheckResult result = serviceToTest.executeHealthCheck();

        /* test */
        dumpIfEnabled(result);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.WARNING);
        assertThat(result.getEntries()).hasSize(2);

        Iterator<TemplateHealthCheckEntry> iterator = result.getEntries().iterator();
        TemplateHealthCheckEntry first = iterator.next();
        TemplateHealthCheckEntry second = iterator.next();
        if (first.getExecutorConfigUUID().equals(executorConfig2UUID.toString())) {
            /*
             * the result is not sorted - to be deterministic we must change for testing to
             * avoid flaky tests:
             */
            TemplateHealthCheckEntry formerFirst = first;
            first = second;
            second = formerFirst;
        }
        assertThat(first.getType()).isEqualTo(TemplateHealthCheckProblemType.ERROR);
        assertThat(first.getTemplateId()).isEqualTo(TEMPLATE1_ID);
        assertThat(first.getDescription()).contains("file").contains("does not exist").contains(ASSET1_EXECUTOR_CONFIG_FILENAME1);
        assertThat(first.getProfiles()).contains(PROFILE1_ID).hasSize(1);
        assertThat(first.getProjects()).contains(PROJECT1_ID).hasSize(1);

        assertThat(second.getType()).isEqualTo(TemplateHealthCheckProblemType.WARNING); // configuration 2 is disabled - only warned
        assertThat(second.getTemplateId()).isEqualTo(TEMPLATE1_ID);
        assertThat(second.getDescription()).contains("file").contains("does not exist").contains(ASSET1_EXECUTOR_CONFIG_FILENAME2);
        assertThat(second.getProfiles()).contains(PROFILE2_ID).hasSize(1);
        assertThat(second.getProjects()).contains(PROJECT2_ID).hasSize(1);

    }

    @Test
    void happy_flow_template_defined_and_assigned_at_two_projects() {
        ProductExecutionProfile profile1 = mock();
        when(profile1.getId()).thenReturn(PROFILE1_ID);

        ProductExecutionProfile profile2 = mock();
        when(profile2.getId()).thenReturn(PROFILE2_ID);

        ProductIdentifier pid = ProductIdentifier.PDS_WEBSCAN;
        ProductExecutorConfig config1 = mock();
        UUID executorConfig1UUID = UUID.fromString("349ea899-e780-4553-bd50-06c12fe96c9e");
        when(config1.getUUID()).thenReturn(executorConfig1UUID);
        when(config1.getProductIdentifier()).thenReturn(pid);

        ProductExecutorConfig config2 = mock();
        UUID executorConfig2UUID = UUID.fromString("2b25b007-f3d2-4591-ba42-409e19d9a5e8");
        when(config2.getUUID()).thenReturn(executorConfig2UUID);
        when(config2.getProductIdentifier()).thenReturn(pid);

        when(profile1.getConfigurations()).thenReturn(Set.of(config1));
        when(profile2.getConfigurations()).thenReturn(Set.of(config2));

        TemplateDefinition templateDefinition = prepareTemplate1AvailableAndAssignedToProjects(PROJECT1_ID, PROJECT2_ID);

        when(assetFileNameService.resolveAssetFileName(config1)).thenReturn(ASSET1_EXECUTOR_CONFIG_FILENAME1);
        when(assetFileNameService.resolveAssetFileName(config2)).thenReturn(ASSET1_EXECUTOR_CONFIG_FILENAME2);

        AssetDetailData asset1DetailData = mock();
        AssetFileData fileData1 = mock();
        when(fileData1.getFileName()).thenReturn(ASSET1_EXECUTOR_CONFIG_FILENAME1);
        AssetFileData fileData2 = mock();
        when(fileData2.getFileName()).thenReturn(ASSET1_EXECUTOR_CONFIG_FILENAME2);

        when(asset1DetailData.getFiles()).thenReturn(List.of(fileData1, fileData2));
        when(assetService.fetchAssetDetails(ASSET1_ID)).thenReturn(asset1DetailData);

        when(profileRepository.findExecutionProfilesForProject(PROJECT1_ID)).thenReturn(List.of(profile1));
        when(profileRepository.findExecutionProfilesForProject(PROJECT2_ID)).thenReturn(List.of(profile2));

        ensureTemplateSupportedByScanType(templateDefinition);

        /* execute */
        TemplatesHealthCheckResult result = serviceToTest.executeHealthCheck();

        /* test */
        dumpIfEnabled(result);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.OK);
        assertThat(result.getEntries()).hasSize(0);

    }

    @Test
    void template_defined_and_assigned_to_project1_and_project2_different_profiles_different_executors_two_files_one_missing() {
        ProductExecutionProfile profile1 = mock();
        when(profile1.getId()).thenReturn(PROFILE1_ID);

        ProductExecutionProfile profile2 = mock();
        when(profile2.getId()).thenReturn(PROFILE2_ID);

        ProductIdentifier pid = ProductIdentifier.PDS_WEBSCAN;
        ProductExecutorConfig config1 = mock();
        UUID executorConfig1UUID = UUID.fromString("349ea899-e780-4553-bd50-06c12fe96c9e");
        when(config1.getUUID()).thenReturn(executorConfig1UUID);
        when(config1.getProductIdentifier()).thenReturn(pid);
        when(config1.getEnabled()).thenReturn(true);

        ProductExecutorConfig config2 = mock();
        UUID executorConfig2UUID = UUID.fromString("2b25b007-f3d2-4591-ba42-409e19d9a5e8");
        when(config2.getUUID()).thenReturn(executorConfig2UUID);
        when(config2.getProductIdentifier()).thenReturn(pid);
        when(config2.getEnabled()).thenReturn(true);

        when(profile1.getConfigurations()).thenReturn(Set.of(config1));
        when(profile1.getEnabled()).thenReturn(true);

        when(profile2.getConfigurations()).thenReturn(Set.of(config2));
        when(profile2.getEnabled()).thenReturn(true);

        /* prepare */
        TemplateDefinition templateDefinition = prepareTemplate1AvailableAndAssignedToProjects(PROJECT1_ID, PROJECT2_ID);

        when(assetFileNameService.resolveAssetFileName(config1)).thenReturn(ASSET1_EXECUTOR_CONFIG_FILENAME1);
        when(assetFileNameService.resolveAssetFileName(config2)).thenReturn(ASSET1_EXECUTOR_CONFIG_FILENAME2);

        AssetDetailData asset1DetailData = mock();
        AssetFileData fileData1 = mock();
        when(fileData1.getFileName()).thenReturn(ASSET1_EXECUTOR_CONFIG_FILENAME1);

        when(asset1DetailData.getFiles()).thenReturn(List.of(fileData1));
        when(assetService.fetchAssetDetails(ASSET1_ID)).thenReturn(asset1DetailData);

        when(profileRepository.findExecutionProfilesForProject(PROJECT1_ID)).thenReturn(List.of(profile1));
        when(profileRepository.findExecutionProfilesForProject(PROJECT2_ID)).thenReturn(List.of(profile2));

        ensureTemplateSupportedByScanType(templateDefinition);

        /* execute */
        TemplatesHealthCheckResult result = serviceToTest.executeHealthCheck();

        /* test */
        dumpIfEnabled(result);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.ERROR);
        assertThat(result.getEntries()).hasSize(1);

        Iterator<TemplateHealthCheckEntry> iterator = result.getEntries().iterator();
        TemplateHealthCheckEntry first = iterator.next();
        assertThat(first.getType()).isEqualTo(TemplateHealthCheckProblemType.ERROR);
        assertThat(first.getTemplateId()).isEqualTo(TEMPLATE1_ID);
        assertThat(first.getDescription()).contains("file").contains("does not exist").contains(ASSET1_EXECUTOR_CONFIG_FILENAME2);
        assertThat(first.getProfiles()).contains(PROFILE2_ID).hasSize(1);
        assertThat(first.getProjects()).contains(PROJECT2_ID).hasSize(1);

    }

    private TemplateDefinition prepareTemplate1AvailableButNotAssignedToAnyProject() {
        return prepareTemplate1AvailableAndAssignedToProjects(false);
    }

    private TemplateDefinition prepareTemplate1AvailableAndAssignedToProjects(String... projectIds) {
        return prepareTemplate1AvailableAndAssignedToProjects(true, projectIds);
    }

    private TemplateDefinition prepareTemplate1AvailableAndAssignedToProjects(boolean assigned, String... projectIds) {
        TemplateDefinition templateDefinition = new TemplateDefinition();
        templateDefinition.setAssetId(ASSET1_ID);
        templateDefinition.setType(TemplateType.WEBSCAN_LOGIN);
        templateDefinition.setId(TEMPLATE1_ID);

        when(templateService.fetchAllTemplateIds()).thenReturn(List.of(TEMPLATE1_ID));
        if (assigned) {
            when(templateService.fetchAllAssignedTemplateIds()).thenReturn(Set.of(TEMPLATE1_ID));
            when(templateService.fetchProjectIdsUsingTemplate(TEMPLATE1_ID)).thenReturn(Set.of(projectIds));
        }
        when(templateService.fetchTemplateDefinition(TEMPLATE1_ID)).thenReturn(templateDefinition);
        when(templateService.fetchAllTemplateIds()).thenReturn(List.of(TEMPLATE1_ID));

        return templateDefinition;
    }

    private void ensureTemplateSupportedByScanType(TemplateDefinition templateDefinition) {
        when(templateDefinitionFilter.isScanTypeSupportingTemplate(any(ScanType.class), eq(templateDefinition))).thenReturn(true);
    }

    private void dumpIfEnabled(TemplatesHealthCheckResult result) {
        String json = result.toFormattedJSON();
        LOG.debug("result={}", json);
    }

}
