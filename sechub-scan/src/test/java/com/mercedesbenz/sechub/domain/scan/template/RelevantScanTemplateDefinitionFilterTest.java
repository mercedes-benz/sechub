// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.template.TemplateData;
import com.mercedesbenz.sechub.commons.model.template.TemplateDataResolver;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;

class RelevantScanTemplateDefinitionFilterTest {
    private RelevantScanTemplateDefinitionFilter filterToTest;
    private SecHubConfigurationModel configuration;
    private TemplateDataResolver templateDataResolver;
    private TemplateDefinition defininition1WebScanLogin;
    private TemplateDefinition defininition2NoTemplateType;
    private List<TemplateDefinition> templateDefinitions;

    @BeforeEach
    void beforeEach() {
        templateDataResolver = mock();
        configuration = mock();

        filterToTest = new RelevantScanTemplateDefinitionFilter();
        filterToTest.templateDataResolver = templateDataResolver;

        defininition1WebScanLogin = new TemplateDefinition();
        defininition1WebScanLogin.setType(TemplateType.WEBSCAN_LOGIN);

        defininition2NoTemplateType = new TemplateDefinition();
        defininition2NoTemplateType.setType(null);

        templateDefinitions = new ArrayList<>(2);
        templateDefinitions.add(defininition1WebScanLogin);
        templateDefinitions.add(defininition2NoTemplateType);

    }

    @Test
    void webscan_login_definition_inside_result_when_resolver_finds_template_data_and_scan_type_is_web_scan() {
        /* prepare */
        TemplateData templateData = mock();

        when(templateDataResolver.resolveTemplateData(TemplateType.WEBSCAN_LOGIN, configuration)).thenReturn(templateData);

        /* execute */
        List<TemplateDefinition> result = filterToTest.filter(templateDefinitions, ScanType.WEB_SCAN, configuration);

        /* test */
        assertThat(result).contains(defininition1WebScanLogin).doesNotContain(defininition2NoTemplateType).hasSize(1);
    }

    @ParameterizedTest
    @EnumSource(value = ScanType.class, mode = Mode.EXCLUDE, names = "WEB_SCAN")
    @NullSource
    void webscan_login_definition_not_inside_result_when_resolver_finds_template_data_but_scan_type_is_not_web_scan(ScanType scanType) {
        /* prepare */
        TemplateData templateData = mock();

        when(templateDataResolver.resolveTemplateData(TemplateType.WEBSCAN_LOGIN, configuration)).thenReturn(templateData);

        /* execute */
        List<TemplateDefinition> result = filterToTest.filter(templateDefinitions, scanType, configuration);

        /* test */
        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ScanType.class)
    @NullSource
    void no_login_definition_inside_result_when_resolver_does_not_find_template_data(ScanType scanType) {
        /* prepare */
        when(templateDataResolver.resolveTemplateData(TemplateType.WEBSCAN_LOGIN, configuration)).thenReturn(null);

        /* execute */
        List<TemplateDefinition> result = filterToTest.filter(templateDefinitions, scanType, configuration);

        /* test */
        assertThat(result).isEmpty();
    }

}
