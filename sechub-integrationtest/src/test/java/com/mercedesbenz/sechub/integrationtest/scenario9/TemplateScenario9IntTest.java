// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario9;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.as;
import static com.mercedesbenz.sechub.integrationtest.scenario9.Scenario9.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.domain.scan.template.TemplateHealthCheckEntry;
import com.mercedesbenz.sechub.domain.scan.template.TemplateHealthCheckProblemType;
import com.mercedesbenz.sechub.domain.scan.template.TemplatesHealthCheckResult;
import com.mercedesbenz.sechub.domain.scan.template.TemplatesHealthCheckStatus;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestExtension;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.WithTestScenario;

@ExtendWith(IntegrationTestExtension.class)
@WithTestScenario(Scenario9.class)
/**
 * This is similar to TemplateScenario1IntTest, but here we got errors inside
 * template check because the product (PDS) is supporting templates and the scan
 * type (Web scan) is supported as well!
 */
public class TemplateScenario9IntTest {

    private String templateId;

    private TemplateDefinition createDefinition;

    @BeforeEach
    void beforeEach() {

        templateId = "template-1_" + System.nanoTime();

        /* @formatter:off */
        createDefinition = TemplateDefinition.builder().
                templateId(templateId).
                templateType(TemplateType.WEBSCAN_LOGIN).
                assetId("asset1").
                build();
        /* @formatter:on */
        TemplateVariable usernameVariable = new TemplateVariable();
        usernameVariable.setName("username");

        TemplateVariable passwordVariable = new TemplateVariable();
        passwordVariable.setName("password");

        createDefinition.getVariables().add(usernameVariable);
        createDefinition.getVariables().add(passwordVariable);

        /*
         * we need to clear old template data , to be able to restart the test for
         * development
         */
        TestAPI.clearAllExistingTemplates();

    }

    @Test
    void template_healthcheck_test_for_situation_that_pds_product_is_able_to_use_templates_and_scan_type_also_supported() {
        /* prepare */

        /* execute + test */
        assertTemplateHealthCheckSaysOKwithoutAnyEntries(); // beforeEach drops any old template data, so we can test here

        as(SUPER_ADMIN).createOrUpdateTemplate(templateId, createDefinition);

        assertTemplateHealthCheckSaysOkButInfoThatTemplateIsNotAssigned();

        as(SUPER_ADMIN).assignTemplateToProject(templateId, PROJECT_1);

        assertTemplateHealthCheckSaysErrorBecauseAssetNotAvailable();

    }

    private void assertTemplateHealthCheckSaysOKwithoutAnyEntries() {
        TemplatesHealthCheckResult result = as(SUPER_ADMIN).executeTemplatesHealthcheck();

        executeResilient(() -> {
            assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.OK);
            assertThat(result.getEntries()).isEmpty();

        });
    }

    private void assertTemplateHealthCheckSaysOkButInfoThatTemplateIsNotAssigned() {
        TemplatesHealthCheckResult result = as(SUPER_ADMIN).executeTemplatesHealthcheck();

        executeResilient(() -> {
            assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.OK);
            assertThat(result.getEntries()).hasSize(1);
            TemplateHealthCheckEntry firstEntry = result.getEntries().iterator().next();
            assertThat(firstEntry.getType()).isEqualTo(TemplateHealthCheckProblemType.INFO);
            assertThat(firstEntry.getDescription()).contains("The template is defined, but not assigned to any project");

        });

    }

    private void assertTemplateHealthCheckSaysErrorBecauseAssetNotAvailable() {
        TemplatesHealthCheckResult result = as(SUPER_ADMIN).executeTemplatesHealthcheck();

        executeResilient(() -> {
            assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.ERROR);
            assertThat(result.getEntries()).hasSize(1);
            TemplateHealthCheckEntry firstEntry = result.getEntries().iterator().next();
            assertThat(firstEntry.getType()).isEqualTo(TemplateHealthCheckProblemType.ERROR);
            assertThat(firstEntry.getDescription()).contains("The file 'asset1/PDS_INTTEST_PRODUCT_WS_SARIF.zip' does not exist!");

        });
    }

}
