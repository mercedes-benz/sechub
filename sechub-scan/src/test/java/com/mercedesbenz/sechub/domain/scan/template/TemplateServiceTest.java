// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.template;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

class TemplateServiceTest {

    private TemplateService serviceToTest;
    private TemplateRepository repository;
    private ScanProjectConfigService configService;
    private UserInputAssertion inputAssertion;
    private TemplateTypeScanConfigIdResolver resolver;

    @BeforeEach
    void beforeEach() {
        repository = mock(TemplateRepository.class);
        configService = mock(ScanProjectConfigService.class);
        inputAssertion = mock(UserInputAssertion.class);
        resolver = mock(TemplateTypeScanConfigIdResolver.class);

        serviceToTest = new TemplateService(repository, configService, inputAssertion, resolver);
    }

    @Test
    void createOrUpdateTemplate_missing_parameter_throws_illegal_argument_exception() throws Exception {

        assertThatThrownBy(() -> serviceToTest.createOrUpdateTemplate(null, null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template id may not be null!");

        assertThatThrownBy(() -> serviceToTest.createOrUpdateTemplate(null, new TemplateDefinition())).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template id may not be null!");

        assertThatThrownBy(() -> serviceToTest.createOrUpdateTemplate("id1", null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template definition may not be null!");

    }

    @Test
    void delete_missing_parameter_throws_illegal_argument_exception() throws Exception {

        assertThatThrownBy(() -> serviceToTest.deleteTemplate(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template id may not be null!");

    }

    @Test
    void fetch_missing_parameter_throws_illegal_argument_exception() throws Exception {

        assertThatThrownBy(() -> serviceToTest.fetchTemplateDefinition(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template id may not be null!");

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
        existingTemplateDefinition.getAssets().add("asset1");
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

}
