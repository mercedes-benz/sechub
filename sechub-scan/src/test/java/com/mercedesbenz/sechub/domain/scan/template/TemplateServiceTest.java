// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigID;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigService;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.template.SecHubProjectToTemplate;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

class TemplateServiceTest {

    private static final String TEST_TEMPLATE_ID_ASSERTION_MESSAGE = "Template id may not be null!";
    private TemplateService serviceToTest;
    private TemplateRepository repository;
    private ScanProjectConfigService configService;
    private UserInputAssertion inputAssertion;
    private TemplateTypeScanConfigIdResolver resolver;
    private DomainMessageService domainMessageService;

    @BeforeEach
    void beforeEach() {
        repository = mock();
        configService = mock();
        inputAssertion = mock();
        domainMessageService = mock();

        doThrow(new IllegalArgumentException(TEST_TEMPLATE_ID_ASSERTION_MESSAGE)).when(inputAssertion).assertIsValidTemplateId(null);

        resolver = mock(TemplateTypeScanConfigIdResolver.class);

        serviceToTest = new TemplateService(repository, configService, inputAssertion, resolver, domainMessageService);
    }

    @Test
    void createOrUpdateTemplate_missing_parameter_throws_illegal_argument_exception() throws Exception {

        assertThatThrownBy(() -> serviceToTest.createOrUpdateTemplate(null, null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(TEST_TEMPLATE_ID_ASSERTION_MESSAGE);

        assertThatThrownBy(() -> serviceToTest.createOrUpdateTemplate(null, new TemplateDefinition())).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(TEST_TEMPLATE_ID_ASSERTION_MESSAGE);

        assertThatThrownBy(() -> serviceToTest.createOrUpdateTemplate("id1", null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template definition may not be null!");

    }

    @Test
    void delete_missing_parameter_throws_illegal_argument_exception() throws Exception {

        assertThatThrownBy(() -> serviceToTest.deleteTemplate(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(TEST_TEMPLATE_ID_ASSERTION_MESSAGE);

    }

    @Test
    void fetch_missing_parameter_throws_illegal_argument_exception() throws Exception {

        assertThatThrownBy(() -> serviceToTest.fetchTemplateDefinition(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(TEST_TEMPLATE_ID_ASSERTION_MESSAGE);

    }

    @Test
    void create_createOrUpdateTemplate_stores_template_with_given_template_identifier_and_definition() throws Exception {

        /* prepare */
        TemplateDefinition templateDefinition = mock(TemplateDefinition.class);
        when(templateDefinition.toFormattedJSON()).thenReturn("formatted-json");

        /* execute */
        serviceToTest.createOrUpdateTemplate("template1", templateDefinition);

        /* test */
        verify(templateDefinition).setId("template1");
        ArgumentCaptor<Template> captor = ArgumentCaptor.captor();
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo("template1");
        assertThat(captor.getValue().getDefinition()).isEqualTo("formatted-json");

    }

    @Test
    void update_createOrUpdateTemplate_stores_existing_template_with_new_template_definition() throws Exception {

        /* prepare */
        String templateId = "template1";

        TemplateVariable variable1 = new TemplateVariable();
        variable1.setName("var1");

        TemplateDefinition existingTemplateDefinition = new TemplateDefinition();
        existingTemplateDefinition.setId(templateId);
        existingTemplateDefinition.setType(TemplateType.WEBSCAN_LOGIN);
        existingTemplateDefinition.setAssetId("asset1");
        existingTemplateDefinition.getVariables().add(variable1);

        Template existingTemplate = new Template(templateId);
        existingTemplate.setDefinition(existingTemplateDefinition.toFormattedJSON());

        when(repository.findById(templateId)).thenReturn(Optional.of(existingTemplate));

        TemplateDefinition templateDefinition = new TemplateDefinition();
        templateDefinition.setId("some-other-id-which-will-be-ignored");
        templateDefinition.setType(null); // just set to null to have another value than the stored one

        /* execute */
        serviceToTest.createOrUpdateTemplate(templateId, templateDefinition);

        /* test */
        ArgumentCaptor<Template> captor = ArgumentCaptor.captor();
        verify(repository).save(captor.capture());

        Template storedTemplate = captor.getValue();
        TemplateDefinition storedDefinition = TemplateDefinition.from(storedTemplate.getDefinition());

        assertThat(storedTemplate.getId()).as("stored template id may not change").isEqualTo(templateId); //
        assertThat(storedDefinition.getType()).as("stored template type may not change").isEqualTo(TemplateType.WEBSCAN_LOGIN);
    }

    @Test
    void delete_deletes_assignments_and_entity() {
        /* prepare */
        String templateId = "template1";
        TemplateDefinition template1 = new TemplateDefinition();
        template1.setType(TemplateType.WEBSCAN_LOGIN);
        Template template = mock(Template.class);

        when(template.getDefinition()).thenReturn(template1.toJSON());
        Set<String> allTemplateConfigIds = Set.of("resolved-template-type1", "resolved-template-type2");
        when(resolver.resolveAllPossibleConfigIds()).thenReturn(allTemplateConfigIds);

        when(repository.findById(templateId)).thenReturn(Optional.of(template));

        /* execute */
        serviceToTest.deleteTemplate(templateId);

        /* test */
        verify(configService).deleteAllConfigurationsOfGivenConfigIdsAndValue(allTemplateConfigIds, templateId);
        verify(repository).deleteById(templateId);
    }

    @Test
    void delete_triggers_domain_request() {
        /* prepare */
        String templateId = "template1";
        TemplateDefinition template1 = new TemplateDefinition();
        template1.setType(TemplateType.WEBSCAN_LOGIN);
        Template template = mock(Template.class);

        when(template.getDefinition()).thenReturn(template1.toJSON());
        Set<String> allTemplateConfigIds = Set.of("resolved-template-type1", "resolved-template-type2");
        when(resolver.resolveAllPossibleConfigIds()).thenReturn(allTemplateConfigIds);

        when(repository.findById(templateId)).thenReturn(Optional.of(template));

        /* execute */
        serviceToTest.deleteTemplate(templateId);

        /* test */
        ArgumentCaptor<DomainMessage> messageCaptor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(domainMessageService).sendAsynchron(messageCaptor.capture());

        DomainMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getMessageId()).isEqualTo(MessageID.TEMPLATE_DELETED);
        SecHubProjectToTemplate data = sentMessage.get(MessageDataKeys.PROJECT_TO_TEMPLATE);
        assertThat(data).isNotNull();
        assertThat(data.getTemplateId()).isEqualTo(templateId);
    }

    @Test
    void delete_non_existing_template_id_throws_exception() {
        /* prepare */
        String templateId = "non-existing-template-id";
        when(repository.findById(templateId)).thenReturn(Optional.empty());

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.deleteTemplate(templateId)).isInstanceOf(NotFoundException.class).hasMessageContaining(templateId);
    }

    @Test
    void fetch_returns_definition_from_repo() {
        /* prepare */
        TemplateDefinition def = new TemplateDefinition();
        def.setId("template1");
        def.setType(TemplateType.WEBSCAN_LOGIN);

        Template template1 = mock(Template.class);
        when(template1.getDefinition()).thenReturn(def.toJSON());

        Optional<Template> template = Optional.of(template1);
        when(repository.findById("template1")).thenReturn(template);

        /* execute */
        TemplateDefinition result = serviceToTest.fetchTemplateDefinition("template1");

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.toFormattedJSON()).isEqualTo(def.toFormattedJSON());
    }

    @Test
    void fetchAssignedTemplateIdsForProject() {
        /* prepare */
        when(resolver.resolve(TemplateType.WEBSCAN_LOGIN)).thenReturn(ScanProjectConfigID.TEMPLATE_WEBSCAN_LOGIN);
        ScanProjectConfig config = mock();
        when(configService.get("project1",ScanProjectConfigID.TEMPLATE_WEBSCAN_LOGIN, false)).thenReturn(config);
        when(config.getData()).thenReturn("template-id-1");

        /* execute */
        Set<String> result = serviceToTest.fetchAssignedTemplateIdsForProject("project1");
        assertThat(result).contains("template-id-1").hasSize(1);
    }

    @Test
    void fetch_template_definitions_for_project() {
        /* prepare */

        when(resolver.resolve(TemplateType.WEBSCAN_LOGIN)).thenReturn(ScanProjectConfigID.TEMPLATE_WEBSCAN_LOGIN);
        ScanProjectConfig config = mock();
        when(configService.get("project1",ScanProjectConfigID.TEMPLATE_WEBSCAN_LOGIN, false)).thenReturn(config);
        when(config.getData()).thenReturn("template1");

        Template template1Entity = mock();
        TemplateDefinition t1 = TemplateDefinition.builder().assetId("asset1").templateId("template1").templateType(TemplateType.WEBSCAN_LOGIN).build();
        when(template1Entity.getDefinition()).thenReturn(t1.toFormattedJSON());

        when(repository.findById("template1")).thenReturn(Optional.of(template1Entity));

        /* execute */
        List<TemplateDefinition> templateDefinitions = serviceToTest.fetchAllTemplateDefinitionsForProject("project1");

        /* test */
        assertThat(templateDefinitions).contains(t1);

    }

    @Test
    void fetchAssignedTemplateIds() {

        /* prepare */
        when(resolver.resolve(any(TemplateType.class))).thenReturn(ScanProjectConfigID.TEMPLATE_WEBSCAN_LOGIN);
        when(configService.findAllData(ScanProjectConfigID.TEMPLATE_WEBSCAN_LOGIN)).thenReturn(List.of("t1","t2"));

        /* execute */
        Set<String> result = serviceToTest.fetchAllAssignedTemplateIds();

        /* test */
        assertThat(result).isNotNull();
        assertThat(result).contains("t1", "t2");
    }

    @Test
    void fetchProjectsUsingTemplate() {
        /* prepare */
        String templateId = "test-template1";
        Set<String> projects = Set.of("p1", "p2");
        Set<String> allConfigIds = Set.of("c1", "c2");
        when(resolver.resolveAllPossibleConfigIds()).thenReturn(allConfigIds);
        when(configService.findAllProjectsWhereConfigurationHasGivenData(allConfigIds, templateId)).thenReturn(projects);

        /* execute */
        Set<String> result = serviceToTest.fetchProjectsUsingTemplate(templateId);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result).contains("p1", "p2");

    }

}
