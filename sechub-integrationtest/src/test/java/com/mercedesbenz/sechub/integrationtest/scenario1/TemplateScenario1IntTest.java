// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario1;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.as;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.client.HttpClientErrorException.UnprocessableEntity;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigID;
import com.mercedesbenz.sechub.domain.scan.template.TemplateHealthCheckEntry;
import com.mercedesbenz.sechub.domain.scan.template.TemplateHealthCheckProblemType;
import com.mercedesbenz.sechub.domain.scan.template.TemplatesHealthCheckResult;
import com.mercedesbenz.sechub.domain.scan.template.TemplatesHealthCheckStatus;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestExtension;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.api.WithTestScenario;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestTemplateFile;

@ExtendWith(IntegrationTestExtension.class)
@WithTestScenario(Scenario1.class)
/**
 * Info: There are similarities to TemplateScenario9IntTest, but here we have no
 * product in executor configurations which supports templates. This means that
 * testing templates here, can only check common parts - e.g. info that template
 * exists but is not assigned to any project.
 */
public class TemplateScenario1IntTest {

    private String templateId;

    private TemplateDefinition createDefinition;

    private TemplateDefinition definitionWithId;
    private TemplateDefinition updateDefinition;

    @BeforeEach
    void beforeEach() {

        templateId = "test-template-1";

        /* @formatter:off */
        TemplateDefinition fullTemplateDefinition = TemplateDefinition.from("""
                {
                  "id" : "%s",
                  "type" : "WEBSCAN_LOGIN",
                  "variables" : [

                      {
                          "name" : "mandatory-variable-1",
                          "optional" : false
                      }

                  ],
                  "assetId" : "asset1"
                }
                """.formatted(templateId));

        System.out.println(fullTemplateDefinition.toFormattedJSON());

        /* @formatter:on */

        String fullTemplateDefinitionJson = fullTemplateDefinition.toFormattedJSON();
        createDefinition = TemplateDefinition.from(fullTemplateDefinitionJson.replace(templateId, "does-not-matter-will-be-overriden"));

        definitionWithId = TemplateDefinition.from(fullTemplateDefinitionJson);

        // update will try to change template id and add some new variables
        updateDefinition = TemplateDefinition.from(fullTemplateDefinitionJson.replace(templateId, "will-not-be-changed-by-update"));

        TemplateVariable usernameVariable = new TemplateVariable();
        usernameVariable.setName("username");

        TemplateVariable passwordVariable = new TemplateVariable();
        passwordVariable.setName("password");

        updateDefinition.getVariables().add(usernameVariable); // important: user name is added before, keep this, otherwise test fails!
        updateDefinition.getVariables().add(passwordVariable);

        /*
         * we need to clear old template data , to be able to restart the test for
         * development
         */
        TestAPI.clearAllExistingTemplates();

    }

    @Test
    void template_crud_and_healthcheck_test() {
        /* prepare */
        as(SUPER_ADMIN).createProject(Scenario1.PROJECT_1, SUPER_ADMIN).assignUserToProject(SUPER_ADMIN, Scenario1.PROJECT_1); // not done in this scenario
                                                                                                                               // automatically
        /* check preconditions */
        assertTemplateNotInsideTemplateList();

        /* execute + test */

        assertTemplateHealthCheckSaysOKwithoutAnyEntries(); // beforeEach drops any old template data, so we can test here

        assertTemplateCanBeCreated();

        assertTemplateHealthCheckSaysOkButInfoThatTemplateIsNotAssigned();

        assertTemplateCanBeAssignedToProject();

        // will not work, because test template file has not mandatory variable defined
        assertThatThrownBy(() -> as(SUPER_ADMIN).createWebScan(Scenario1.PROJECT_1, IntegrationTestTemplateFile.WEBSCAN_1))
                .isInstanceOf(UnprocessableEntity.class).hasMessageContaining("mandatory-variable-1");
        ;

        as(SUPER_ADMIN).createWebScan(Scenario1.PROJECT_1, IntegrationTestTemplateFile.WEBSCAN_2); // will work, because here variable is defined

        assertTemplateCanBeUpdated();

        assertThatThrownBy(() -> as(SUPER_ADMIN).createWebScan(Scenario1.PROJECT_1, IntegrationTestTemplateFile.WEBSCAN_2))
                .isInstanceOf(UnprocessableEntity.class).hasMessageContaining("username");

        assertTemplateCanBeUnassignedFromProject();

        assertTemplateCanBeAssignedToProject();

        assertTemplateHealthCheckSaysOkWithoutEntries(); // ok without entries... why? because template is assigned, no asset file
                                                         // exists, but... there is no product which would support templates, so no
                                                         // runtime problems!

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

    private void assertTemplateHealthCheckSaysOKwithoutAnyEntries() {
        TemplatesHealthCheckResult result = as(SUPER_ADMIN).executeTemplatesHealthcheck();

        executeResilient(() -> {
            assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.OK);
            assertThat(result.getEntries()).isEmpty();

        });
    }

    private void assertTemplateHealthCheckSaysOkWithoutEntries() {
        TemplatesHealthCheckResult result = as(SUPER_ADMIN).executeTemplatesHealthcheck();

        executeResilient(() -> {
            assertThat(result.getStatus()).isEqualTo(TemplatesHealthCheckStatus.OK);
            assertThat(result.getEntries()).hasSize(0);

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

    private void assertTemplateNotInsideTemplateList() {
        List<String> templateIds = as(SUPER_ADMIN).fetchTemplateList();
        executeResilient(() -> assertThat(templateIds).doesNotContain(templateId));
    }

    private void assertTemplateExistsInTemplateListAndCanBeFetched() {
        // check template list still contains the test template */
        executeResilient(() -> assertThat(as(SUPER_ADMIN).fetchTemplateList()).contains(templateId));
        executeResilient(() -> assertThat(as(SUPER_ADMIN).fetchTemplateDefinitionOrNull(templateId)).isNotNull());
    }

    private void assertProjectDeleteDoesPurgeTemplateAssignment() {
        /* execute 7 - delete project */
        as(SUPER_ADMIN).deleteProject(Scenario1.PROJECT_1);

        /* test 7 - configuration for project is removed */
        executeResilient(() -> assertThat(fetchScanProjectConfigurations(Scenario1.PROJECT_1)).isEmpty());

    }

    private void assertTemplateCanBeRecreatedWithSameId() {
        /* execute 6 - create template with same id again */
        as(SUPER_ADMIN).createOrUpdateTemplate(templateId, createDefinition);

        /* test 6 - template is recreated */
        executeResilient(() -> assertThat(as(SUPER_ADMIN).fetchTemplateDefinitionOrNull(templateId)).isNotNull());
    }

    private void assertTemplateCanBeDeletedAndAssignmentIsPurged() {
        /* execute 5 - delete template */
        as(SUPER_ADMIN).deleteTemplate(templateId);

        /* test 5.1 check delete unassigns template */
        executeResilient(() -> assertThat(as(SUPER_ADMIN).fetchProjectDetailInformation(Scenario1.PROJECT_1).getTemplateIds()).contains(templateId));

        /* test 5.2 check template no longer exists */
        executeResilient(() -> assertThat(as(SUPER_ADMIN).fetchTemplateDefinitionOrNull(templateId)).isNull());
    }

    private void assertTemplateCanBeUnassignedFromProject() {
        /* execute 4 - unassign */
        as(SUPER_ADMIN).unassignTemplateFromProject(templateId, Scenario1.PROJECT_1);

        /* test 4 - check assignment */
        executeResilient(() -> assertThat(as(SUPER_ADMIN).fetchProjectDetailInformation(Scenario1.PROJECT_1).getTemplateIds()).isEmpty());
        executeResilient(() -> assertThat(fetchScanProjectConfigurations(Scenario1.PROJECT_1)).isEmpty());
    }

    private void assertTemplateCanBeAssignedToProject() {

        /* execute 3- assign */
        as(SUPER_ADMIN).assignTemplateToProject(templateId, Scenario1.PROJECT_1);

        /* test 3.1 - check assignment by project details in domain administration */
        executeResilient(() -> assertThat(as(SUPER_ADMIN).fetchProjectDetailInformation(Scenario1.PROJECT_1).getTemplateIds()).contains(templateId));

        /* test 3.2 - check project scan configuration in domain scan */
        executeResilient(() -> {
            List<ScanProjectConfig> configurations = fetchScanProjectConfigurations(Scenario1.PROJECT_1);
            assertThat(configurations).isNotEmpty().hasSize(1)
                    .contains(new ScanProjectConfig(ScanProjectConfigID.TEMPLATE_WEBSCAN_LOGIN, Scenario1.PROJECT_1.getProjectId()));
            assertThat(configurations.iterator().next().getData()).isEqualTo(templateId);
        });
    }

    private void assertTemplateCanBeUpdated() {
        /* prepare 2 - update */
        updateDefinition.setAssetId("asset2");

        /* execute 2 - update */
        as(SUPER_ADMIN).createOrUpdateTemplate(templateId, updateDefinition);

        /* test 2 - update works */
        executeResilient(() -> {
            TemplateDefinition loadedTemplate = as(SUPER_ADMIN).fetchTemplateDefinitionOrNull(templateId);
            assertThat(loadedTemplate.getAssetId()).isEqualTo("asset2");
            assertThat(loadedTemplate.getType()).isEqualTo(TemplateType.WEBSCAN_LOGIN);
            assertThat(loadedTemplate.getId()).isEqualTo(templateId);
        });
    }

    private void assertTemplateCanBeCreated() {
        /* execute 1 - create */
        as(SUPER_ADMIN).createOrUpdateTemplate(templateId, createDefinition);

        /* test 1 - created definition has content as expected and contains id */
        executeResilient(
                () -> assertThat(as(SUPER_ADMIN).fetchTemplateDefinitionOrNull(templateId).toFormattedJSON()).isEqualTo(definitionWithId.toFormattedJSON()));
    }

}
