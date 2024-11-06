// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario1;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.as;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.domain.administration.project.ProjectDetailInformation;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigID;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;

public class TemplateScenario1IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario1.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    private String templateId;

    private TemplateDefinition createDefinition;

    private TemplateVariable usernameVariable;

    private TemplateVariable passwordVariable;

    private TemplateDefinition definitionWithId;
    private TemplateDefinition updateDefinition;

    @Before
    public void before() {

        templateId = "template-1_" + System.nanoTime();

        createDefinition = new TemplateDefinition();
        createDefinition.setId(null); // the creation does not need an id. To test this we explicit set to null...
        createDefinition.setType(TemplateType.WEBSCAN_LOGIN);
        createDefinition.getAssets().add("asset1");

        usernameVariable = new TemplateVariable();
        usernameVariable.setName("username");

        passwordVariable = new TemplateVariable();
        passwordVariable.setName("password");

        createDefinition.getVariables().add(usernameVariable);
        createDefinition.getVariables().add(passwordVariable);

        definitionWithId = TemplateDefinition.from(createDefinition.toJSON());
        definitionWithId.setId(templateId);

        updateDefinition = TemplateDefinition.from(createDefinition.toJSON());
        updateDefinition.setType(null); // the update does not need a type. To test this we explicit set to null..
    }

    @Test
    public void template_crud_test() {
        /* prepare */
        as(SUPER_ADMIN).createProject(Scenario1.PROJECT_1, SUPER_ADMIN); // not done in this scenario automatically

        /* check preconditions */
        assertTemplateNotInsideTemplateList();

        /* execute + test */
        assertTemplateCanBeCreated();

        assertTemplateCanBeUpdated();

        assertTemplateCanBeAssignedToProject();

        assertTemplateCanBeUnassignedFromProject();

        assertTemplateCanBeAssignedToProject();

        assertTemplateCanBeDeletedAndAssignmentIsPurged();

        assertTemplateCanBeRecreatedWithSameId();

        assertTemplateCanBeAssignedToProject();

        assertProjectDeleteDoesPurgeTemplateAssignment();

        assertTemplateExistsInTemplateListAndCanBeFetched();

        /*
         * cleanup - we remove the re-created template finally to have no garbage after
         * test
         */
        as(SUPER_ADMIN).deleteTemplate(templateId);

        // check cleanup worked
        assertTemplateNotInsideTemplateList();

    }

    private void assertTemplateNotInsideTemplateList() {
        List<String> templateIds = as(SUPER_ADMIN).fetchTemplateList();
        assertThat(templateIds).doesNotContain(templateId);
    }

    private void assertTemplateExistsInTemplateListAndCanBeFetched() {
        List<String> templateIds;
        // check template list still contains the test templte */
        templateIds = as(SUPER_ADMIN).fetchTemplateList();
        assertThat(templateIds).contains(templateId);
        assertThat(as(SUPER_ADMIN).fetchTemplateDefinitionOrNull(templateId)).isNotNull();
    }

    private void assertProjectDeleteDoesPurgeTemplateAssignment() {
        /* execute 7 - delete project */
        as(SUPER_ADMIN).deleteProject(Scenario1.PROJECT_1);

        /* test 7 - configuration for project is removed */
        List<ScanProjectConfig> configurations = fetchScanProjectConfigurations(Scenario1.PROJECT_1);
        assertThat(configurations).isEmpty();
    }

    private void assertTemplateCanBeRecreatedWithSameId() {
        /* execute 6 - create template with same id again */
        as(SUPER_ADMIN).createOrUpdateTemplate(templateId, createDefinition);

        /* test 6 - template is recreated */
        assertThat(as(SUPER_ADMIN).fetchTemplateDefinitionOrNull(templateId)).isNotNull();
    }

    private void assertTemplateCanBeDeletedAndAssignmentIsPurged() {
        /* execute 5 - delete template */
        as(SUPER_ADMIN).deleteTemplate(templateId);

        /* test 5.1 check delete unassigns template */
        ProjectDetailInformation info = as(SUPER_ADMIN).fetchProjectDetailInformation(Scenario1.PROJECT_1);
        assertThat(info.getTemplates()).contains(templateId);

        /* test 5.2 check template no longer exists */
        assertThat(as(SUPER_ADMIN).fetchTemplateDefinitionOrNull(templateId)).isNull();
    }

    private void assertTemplateCanBeUnassignedFromProject() {
        List<ScanProjectConfig> configurations;
        /* execute 4 - unassign */
        as(SUPER_ADMIN).unassignTemplateFromProject(templateId, Scenario1.PROJECT_1);

        /* test 4 - check assignment */
        ProjectDetailInformation info;
        info = as(SUPER_ADMIN).fetchProjectDetailInformation(Scenario1.PROJECT_1);
        assertThat(info.getTemplates()).isEmpty();

        configurations = fetchScanProjectConfigurations(Scenario1.PROJECT_1);
        assertThat(configurations).isEmpty();
    }

    private void assertTemplateCanBeAssignedToProject() {

        /* execute 3- assign */
        as(SUPER_ADMIN).assignTemplateToProject(templateId, Scenario1.PROJECT_1);

        /* test 3.1 - check assignment by project details in domain administration */
        ProjectDetailInformation info = as(SUPER_ADMIN).fetchProjectDetailInformation(Scenario1.PROJECT_1);
        assertThat(info.getTemplates()).contains(templateId);

        /* test 3.2 - check project scan configuration in domain scan */
        List<ScanProjectConfig> configurations = fetchScanProjectConfigurations(Scenario1.PROJECT_1);
        assertThat(configurations).isNotEmpty().hasSize(1)
                .contains(new ScanProjectConfig(ScanProjectConfigID.TEMPLATE_WEBSCAN_LOGIN, Scenario1.PROJECT_1.getProjectId()));
        assertThat(configurations.iterator().next().getData()).isEqualTo(templateId);
    }

    private void assertTemplateCanBeUpdated() {
        /* prepare 2 - update */
        updateDefinition.getAssets().add("asset2");

        /* execute 2 - update */
        as(SUPER_ADMIN).createOrUpdateTemplate(templateId, updateDefinition);

        /* test 2 - update works */
        TemplateDefinition loadedTemplate = as(SUPER_ADMIN).fetchTemplateDefinitionOrNull(templateId);
        assertThat(loadedTemplate.getAssets()).contains("asset1", "asset2");
        assertThat(loadedTemplate.getType()).isEqualTo(TemplateType.WEBSCAN_LOGIN);
        assertThat(loadedTemplate.getId()).isEqualTo(templateId);
    }

    private void assertTemplateCanBeCreated() {
        /* execute 1 - create */
        as(SUPER_ADMIN).createOrUpdateTemplate(templateId, createDefinition);

        /* test 1 - created definition has content as expected and contains id */
        TemplateDefinition loadedTemplate = as(SUPER_ADMIN).fetchTemplateDefinitionOrNull(templateId);
        assertThat(loadedTemplate.toFormattedJSON()).isEqualTo(definitionWithId.toFormattedJSON());
    }

}
