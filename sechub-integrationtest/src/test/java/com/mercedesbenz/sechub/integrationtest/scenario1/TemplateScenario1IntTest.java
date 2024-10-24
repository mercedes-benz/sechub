// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario1;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.as;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.domain.administration.project.ProjectDetailInformation;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigID;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;

/**
 * Inside these integration tests we only check that the inspector has been
 * called with 0 deletes (we have no old data - and we will not try to mock
 * this). The test does check that all expected domains are handled, which means
 * that the dedicated services are called automatically by a trigger service.
 * The logic itself about deletes, amount of deletes, calculation etc. is
 * already done inside DB tests and "normal" unit tests. So testing only for the
 * inspection calls is enough here.
 *
 */
public class TemplateScenario1IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario1.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test
    public void template_crud_test() {

        /* prepare */
        String templateId = "template-1_" + System.nanoTime();

        TemplateDefinition definition = new TemplateDefinition();
        definition.setType(TemplateType.WEBSCAN_LOGIN);
        definition.getAssets().add("asset1");

        TemplateVariable usernameVariable = new TemplateVariable();
        usernameVariable.setName("username");
        TemplateVariable passwordVariable = new TemplateVariable();
        passwordVariable.setName("password");

        definition.getVariables().add(usernameVariable);
        definition.getVariables().add(passwordVariable);

        TemplateDefinition definitionWithId = TemplateDefinition.from(definition.toJSON());
        definitionWithId.setId(templateId);

        /* execute 1 - create */
        as(SUPER_ADMIN).createOrUpdateTemplate(templateId, definition);

        /* test 1 - created definition has content as expected and contains id */
        TemplateDefinition loadedTemplate = as(SUPER_ADMIN).fetchTemplateDefinitionOrNull(templateId);
        assertThat(loadedTemplate.toFormattedJSON()).isEqualTo(definitionWithId.toFormattedJSON());

        /* prepare 2 - update */
        definition.setType(null);// must be ignored
        definition.setId(null);// must be ignored

        definition.getAssets().add("asset2");

        /* execute 2 - update */
        as(SUPER_ADMIN).createOrUpdateTemplate(templateId, definition);

        /* test 2 - update works */
        loadedTemplate = as(SUPER_ADMIN).fetchTemplateDefinitionOrNull(templateId);
        assertThat(loadedTemplate.getAssets()).contains("asset1", "asset2");
        assertThat(loadedTemplate.getType()).isEqualTo(TemplateType.WEBSCAN_LOGIN);
        assertThat(loadedTemplate.getId()).isEqualTo(templateId);

        /* prepare 3 - assign template to projects */
        as(SUPER_ADMIN).createProject(Scenario1.PROJECT_1, SUPER_ADMIN);

        /* execute 3- assign */
        as(SUPER_ADMIN).assignTemplateToProject(templateId, Scenario1.PROJECT_1);

        /* test 3 - check assignment */
        ProjectDetailInformation info = as(SUPER_ADMIN).fetchProjectDetailInformation(Scenario1.PROJECT_1);
        assertThat(info.getTemplates()).contains(templateId);

        List<ScanProjectConfig> configurations = fetchScanProjectConfigurations(Scenario1.PROJECT_1);
        assertThat(configurations).isNotEmpty().hasSize(1)
                .contains(new ScanProjectConfig(ScanProjectConfigID.TEMPLATE_WEBSCAN_LOGIN, Scenario1.PROJECT_1.getProjectId()));
        assertThat(configurations.iterator().next().getData()).isEqualTo(templateId);

        /* execute 4 - unassign */
        as(SUPER_ADMIN).unassignTemplateFromProject(templateId, Scenario1.PROJECT_1);

        /* test 4 - check assignment */
        info = as(SUPER_ADMIN).fetchProjectDetailInformation(Scenario1.PROJECT_1);
        assertThat(info.getTemplates()).isEmpty();

        configurations = fetchScanProjectConfigurations(Scenario1.PROJECT_1);
        assertThat(configurations).isEmpty();

        /* prepare 5 + check precondition - assign again */
        as(SUPER_ADMIN).assignTemplateToProject(templateId, Scenario1.PROJECT_1);
        info = as(SUPER_ADMIN).fetchProjectDetailInformation(Scenario1.PROJECT_1);
        assertThat(info.getTemplates()).contains(templateId);

        /* execute 5 - delete template */
        as(SUPER_ADMIN).deleteTemplate(templateId);

        /* test 5.1 check delete unassigns temlate */
        info = as(SUPER_ADMIN).fetchProjectDetailInformation(Scenario1.PROJECT_1);
        assertThat(info.getTemplates()).contains(templateId);

        /* test 5.2 check template no longer exists */
        assertThat(as(SUPER_ADMIN).fetchTemplateDefinitionOrNull(templateId)).isNull();

        /* execute 6 - create template with same id again */
        as(SUPER_ADMIN).createOrUpdateTemplate(templateId, definition);

        /* test 6 - template is recreated */
        assertThat(as(SUPER_ADMIN).fetchTemplateDefinitionOrNull(templateId)).isNotNull();

        /* prepare e7 - reassign to project */
        as(SUPER_ADMIN).assignTemplateToProject(templateId, Scenario1.PROJECT_1);
        configurations = fetchScanProjectConfigurations(Scenario1.PROJECT_1);
        assertThat(configurations).isNotEmpty().hasSize(1)
                .contains(new ScanProjectConfig(ScanProjectConfigID.TEMPLATE_WEBSCAN_LOGIN, Scenario1.PROJECT_1.getProjectId()));
        assertThat(configurations.iterator().next().getData()).isEqualTo(templateId);

        /* execute 7 - delete project */
        as(SUPER_ADMIN).deleteProject(Scenario1.PROJECT_1);

        /* test 7 - template exists, but config for project is removed */
        assertThat(as(SUPER_ADMIN).fetchTemplateDefinitionOrNull(templateId)).isNotNull();
        configurations = fetchScanProjectConfigurations(Scenario1.PROJECT_1);
        assertThat(configurations).isEmpty();

    }

    @AfterAll
    static void afterAll() {
        /* disable again */
        resetAutoCleanupDays(0);
    }

}
