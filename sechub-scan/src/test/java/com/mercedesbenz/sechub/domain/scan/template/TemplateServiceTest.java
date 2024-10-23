package com.mercedesbenz.sechub.domain.scan.template;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;

class TemplateServiceTest {

    private TemplateService serviceToTest;
    private TemplateRepository repository;

    @BeforeEach
    void beforeEach() {
        repository = mock(TemplateRepository.class);

        serviceToTest = new TemplateService(repository);
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

        assertThatThrownBy(() -> serviceToTest.fetchTemplate(null)).isInstanceOf(IllegalArgumentException.class)
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
        TemplateVariable variable1 = new TemplateVariable();
        variable1.setName("var1");
        
        TemplateDefinition existingTemplateDefinition = new TemplateDefinition();
        existingTemplateDefinition.setId("template1");
        existingTemplateDefinition.setType(TemplateType.WEBSCAN_LOGIN);
        existingTemplateDefinition.getAssets().add("asset1");
        existingTemplateDefinition.getVariables().add(variable1);

        Template existingTemplate = new Template("template1");
        existingTemplate.setDefinition(existingTemplateDefinition.toFormattedJSON());
        
        when(repository.findById("template1")).thenReturn(Optional.of(existingTemplate));
        
        TemplateDefinition templateDefinition = new TemplateDefinition();
        templateDefinition.setId("some-other-id-which-will-be-ignored");
        templateDefinition.setType(null); //just set to null to have another value than the stored one

        /* execute */
        serviceToTest.createOrUpdateTemplate("template1", templateDefinition);

        /* test */
        ArgumentCaptor<Template> captor = ArgumentCaptor.captor();
        verify(repository).save(captor.capture());

        Template storedTemplate = captor.getValue();
        TemplateDefinition storedDefinition = TemplateDefinition.from(storedTemplate.getDefinition());
        
        assertThat(storedTemplate.getId()).as("stored template id may not change").isEqualTo("template1"); //
        assertThat(storedDefinition.getType()).as("stored template type may not change").isEqualTo(TemplateType.WEBSCAN_LOGIN);
    }

    @Test
    void delete_deletes_in_repo() {

        /* execute */
        serviceToTest.deleteTemplate("template1");

        /* test */
        verify(repository).deleteById("template1");
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
        TemplateDefinition result = serviceToTest.fetchTemplate("template1");

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.toFormattedJSON()).isEqualTo(def.toFormattedJSON());
    }

}
