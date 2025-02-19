// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelSupport;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.model.template.TemplateData;
import com.mercedesbenz.sechub.commons.model.template.TemplateDataResolver;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.commons.model.template.TemplateUsageValidator;
import com.mercedesbenz.sechub.commons.model.template.TemplateUsageValidator.TemplateUsageValidatorResult;
import com.mercedesbenz.sechub.domain.scan.template.RelevantScanTemplateDefinitionFilter;
import com.mercedesbenz.sechub.domain.scan.template.TemplateService;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

class ScanSecHubConfigurationRuntimeInspectorTest {

    private static final String TEST_PROJECT_ID = "p1";
    private TemplateService templateService;
    private TemplateDataResolver templateDataResolver;
    private RelevantScanTemplateDefinitionFilter templateDefinitionFilter;
    private SecHubConfigurationModelSupport configurationModelSupport;
    private TemplateUsageValidator templateUsageValidator;

    private TemplateData templateData;
    private SecHubConfiguration config;
    private TemplateDefinition templateDefinition;
    private List<TemplateDefinition> listOfTemplateDefinitionsforProject;

    private ScanSecHubConfigurationRuntimeInspector inspectorToTest;

    @BeforeEach
    void beforeEach() {

        /* services */
        templateService = mock();
        templateDataResolver = mock();
        templateDefinitionFilter = mock();
        configurationModelSupport = mock();
        templateUsageValidator = mock();

        /* test data */
        templateData = mock();
        config = mock();
        templateDefinition = mock();

        listOfTemplateDefinitionsforProject = new ArrayList<>();

        /* defaults in test data */
        when(config.getProjectId()).thenReturn(TEST_PROJECT_ID);
        when(templateService.fetchAllTemplateDefinitionsForProject(TEST_PROJECT_ID)).thenReturn(listOfTemplateDefinitionsforProject);
        when(templateDefinition.getType()).thenReturn(TemplateType.WEBSCAN_LOGIN);

        /* initialize object to test */
        inspectorToTest = new ScanSecHubConfigurationRuntimeInspector(templateService, templateDefinitionFilter, templateDataResolver,
                configurationModelSupport, templateUsageValidator);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void template_defined_for_project_but_not_in_config_but_variables_without_mandatory(boolean atLeastOneOptional) {
        /* prepare */
        listOfTemplateDefinitionsforProject.add(templateDefinition);

        when(configurationModelSupport.collectScanTypes(config)).thenReturn(Set.of(ScanType.WEB_SCAN));
        when(templateDefinitionFilter.filter(listOfTemplateDefinitionsforProject, ScanType.WEB_SCAN, config)).thenReturn(listOfTemplateDefinitionsforProject);

        when(templateDataResolver.resolveTemplateData(any(), any())).thenReturn(null);

        if (atLeastOneOptional) {
            TemplateVariable variable = new TemplateVariable();
            variable.setName("optional-variable");
            variable.setOptional(true);

            when(templateDefinition.getVariables()).thenReturn(List.of(variable));
        }

        /* execute */
        SecHubMessagesList result = inspectorToTest.inspect(config);

        /* test */
        assertThat(result.getSecHubMessages()).hasSize(0);
        verify(templateUsageValidator, never()).validate(any(), any());

    }

    @Test
    void template_defined_for_project_but_not_in_config_and_variables_are_mandatory() {
        /* prepare */
        listOfTemplateDefinitionsforProject.add(templateDefinition);

        when(configurationModelSupport.collectScanTypes(config)).thenReturn(Set.of(ScanType.WEB_SCAN));
        when(templateDefinitionFilter.filter(listOfTemplateDefinitionsforProject, ScanType.WEB_SCAN, config)).thenReturn(listOfTemplateDefinitionsforProject);

        when(templateDataResolver.resolveTemplateData(any(), any())).thenReturn(null);

        TemplateVariable var1 = new TemplateVariable();
        var1.setName("variable1");
        var1.setOptional(false);

        when(templateDefinition.getVariables()).thenReturn(List.of(var1));

        /* execute */
        SecHubMessagesList result = inspectorToTest.inspect(config);

        /* test */
        assertThat(result.getSecHubMessages()).hasSize(1);
        SecHubMessage message1 = result.getSecHubMessages().iterator().next();
        assertThat(message1.getText()).contains("Template data missing. This is necessary to provide webscan-login for the project.").contains("- variable1");

        verify(templateUsageValidator, never()).validate(any(), any());

    }

    @Test
    void when_template_NOT_defined_for_project_and_no_data_available_result_will_not_contain_error_messages() {
        /* prepare */
        listOfTemplateDefinitionsforProject = Collections.emptyList();

        /* execute */
        SecHubMessagesList result = inspectorToTest.inspect(config);

        /* test */
        assertThat(result.getSecHubMessages()).isEmpty();
        verify(templateUsageValidator, never()).validate(any(), any());

    }

    @Test
    void when_validator_does_not_fail_result_contains_no_sechubmessages() {
        /* prepare */
        prepareValidationResultWillBe(true, "I found no failure");

        /* execute */
        SecHubMessagesList result = inspectorToTest.inspect(config);

        /* test */
        assertThat(result.getSecHubMessages()).isEmpty();

    }

    @Test
    void when_validator_does_fail_result_contains_sechubmessage_error_with_validator_message() {
        /* prepare */
        prepareValidationResultWillBe(false, "I found a failure");

        /* execute */
        SecHubMessagesList result = inspectorToTest.inspect(config);

        /* test */
        assertThat(result.getSecHubMessages()).isNotEmpty().hasSize(1);
        SecHubMessage message = result.getSecHubMessages().iterator().next();

        assertThat(message.getType()).isEqualTo(SecHubMessageType.ERROR);
        assertThat(message.getText()).isEqualTo("I found a failure");

    }

    private void prepareValidationResultWillBe(boolean validResult, String text) {
        listOfTemplateDefinitionsforProject.add(templateDefinition);

        when(configurationModelSupport.collectScanTypes(config)).thenReturn(Set.of(ScanType.WEB_SCAN));
        when(templateDefinitionFilter.filter(listOfTemplateDefinitionsforProject, ScanType.WEB_SCAN, config)).thenReturn(listOfTemplateDefinitionsforProject);
        when(templateDataResolver.resolveTemplateData(TemplateType.WEBSCAN_LOGIN, config)).thenReturn(templateData);

        TemplateUsageValidatorResult validatorResult = mock();
        when(validatorResult.isValid()).thenReturn(validResult);
        when(validatorResult.getMessage()).thenReturn(text);

        when(templateUsageValidator.validate(templateDefinition, templateData)).thenReturn(validatorResult);
    }

}
