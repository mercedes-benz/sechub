// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.*;
import static com.mercedesbenz.sechub.restdoc.RestDocumentationTest.*;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;
import java.util.List;

import org.assertj.core.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariableValidation;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.scan.template.TemplateHealthCheckProblemType;
import com.mercedesbenz.sechub.domain.scan.template.TemplateRepository;
import com.mercedesbenz.sechub.domain.scan.template.TemplateRestController;
import com.mercedesbenz.sechub.domain.scan.template.TemplateService;
import com.mercedesbenz.sechub.domain.scan.template.TemplatesHealthCheckResult;
import com.mercedesbenz.sechub.domain.scan.template.TemplatesHealthCheckService;
import com.mercedesbenz.sechub.domain.scan.template.TemplatesHealthCheckStatus;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminCreatesOrUpdatesTemplate;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminDeletesTemplate;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminExecutesTemplatesHealthcheck;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesAllTemplateIds;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesTemplate;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(TemplateRestController.class)
@ContextConfiguration(classes = { TemplateRestController.class, TemplateRestControllerRestDocTest.class })
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@Import(TestRestDocSecurityConfiguration.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class TemplateRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemplateRepository templateRepository;

    @MockBean
    private TemplatesHealthCheckService templateHealthCheckService;

    @MockBean
    TemplateService templateService;

    @MockBean
    AuditLogService auditLogService;

    @MockBean
    LogSanitizer logSanitizer;

    private TemplateDefinition definition;

    private TemplateVariable usernameVariable;

    private TemplateVariable passwordVariable;

    private static final String TEST_TEMPLATE_ID1 = "template1";
    private static final String TEST_TEMPLATE_ID2 = "template2";

    @Before
    public void before() {
        definition = new TemplateDefinition();
        definition.setType(TemplateType.WEBSCAN_LOGIN);
        definition.setAssetId("asset-id1");

        usernameVariable = new TemplateVariable();
        usernameVariable.setName("username");
        TemplateVariableValidation usernameValidation = new TemplateVariableValidation();
        usernameValidation.setMinLength(3);
        usernameValidation.setMaxLength(15);
        usernameValidation.setRegularExpression("[a-zA-Z0-9_-].*");

        usernameVariable.setValidation(usernameValidation);

        passwordVariable = new TemplateVariable();
        passwordVariable.setName("password");
        TemplateVariableValidation passwordValidation = new TemplateVariableValidation();
        passwordValidation.setMaxLength(20);

        passwordVariable.setValidation(passwordValidation);

        definition.getVariables().add(usernameVariable);
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminCreatesOrUpdatesTemplate.class)
    public void restdoc_admin_creates_or_updates_template() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminCreatesOrUpdatesTemplate(TEMPLATE_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminCreatesOrUpdatesTemplate.class;

        String content = definition.toFormattedJSON();

        /* execute + test @formatter:off */
		this.mockMvc.perform(
			put(apiEndpoint, TEST_TEMPLATE_ID1).
			contentType(MediaType.APPLICATION_JSON_VALUE).
	        content(content).
	        header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
        ).
		andExpect(status().isOk()).
		andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    requestSchema(TestOpenApiSchema.TEMPLATES.getSchema()).
                and().
                document(
                            requestFields(
                                    fieldWithPath(PROPERTY_TYPE).description("The template type. Must be be defined when a new template is created. An update will ignore changes of this property because the type is immutable! Currently supported types are: "+ TemplateType.values()),

                                    fieldWithPath(PROPERTY_ASSET_ID).description("The asset id used by the template"),
                                    fieldWithPath(PROPERTY_VARIABLES+"[]."+ TemplateVariable.PROPERTY_NAME).description("The variable name"),
                                    fieldWithPath(PROPERTY_VARIABLES+"[]."+ TemplateVariable.PROPERTY_OPTIONAL).optional().description("Defines if the variable is optional. The default is false"),
                                    fieldWithPath(PROPERTY_VARIABLES+"[]."+ TemplateVariable.PROPERTY_VALIDATION).optional().description("Defines a simple validation segment."),
                                    fieldWithPath(PROPERTY_VARIABLES+"[]."+ TemplateVariable.PROPERTY_VALIDATION+"."+ TemplateVariableValidation.PROPERTY_MIN_LENGTH).optional().description("The minimum content length of this variable"),
                                    fieldWithPath(PROPERTY_VARIABLES+"[]."+ TemplateVariable.PROPERTY_VALIDATION+"."+ TemplateVariableValidation.PROPERTY_MAX_LENGTH).optional().description("The maximum content length of this variable"),
                                    fieldWithPath(PROPERTY_VARIABLES+"[]."+ TemplateVariable.PROPERTY_VALIDATION+"."+ TemplateVariableValidation.PROPERTY_REGULAR_EXPRESSION).optional().description("A regular expression which must match to accept the user input inside the variable")
                            ),
                            pathParameters(
                                    parameterWithName(TEMPLATE_ID.paramName()).description("The (unique) template id")
                         )
				));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminDeletesTemplate.class)
    public void restdoc_admin_deletes_template() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminDeletesTemplate(TEMPLATE_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminDeletesTemplate.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                delete(apiEndpoint, TEST_TEMPLATE_ID1).
                contentType(MediaType.APPLICATION_JSON_VALUE).
                header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
                ).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                useCaseData(useCase).
                tag(RestDocFactory.extractTag(apiEndpoint)).
                requestSchema(TestOpenApiSchema.TEMPLATES.getSchema()).
                and().
                document(
                        pathParameters(
                                parameterWithName(TEMPLATE_ID.paramName()).description("The (unique) template id")
                                )
                        ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminFetchesTemplate.class)
    public void restdoc_admin_fetches_template() throws Exception {
        /* prepare */
        definition.setId(TEST_TEMPLATE_ID1); // to have this in result as well, for create/delete it was not necessary, but
                                             // here we want it
        when(templateService.fetchTemplateDefinition(TEST_TEMPLATE_ID1)).thenReturn(definition);

        String apiEndpoint = https(PORT_USED).buildAdminFetchesTemplate(TEMPLATE_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminFetchesTemplate.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
            get(apiEndpoint, TEST_TEMPLATE_ID1).
            contentType(MediaType.APPLICATION_JSON_VALUE).
            header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
        ).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    requestSchema(TestOpenApiSchema.TEMPLATES.getSchema()).
                and().
                document(
                            responseFields(
                                    fieldWithPath(PROPERTY_TYPE).description("The template type. Currently supported types are: "+ TemplateType.values()),

                                    fieldWithPath(PROPERTY_ID).description("The (unique) template id"),
                                    fieldWithPath(PROPERTY_ASSET_ID).description("The asset id used by the template"),
                                    fieldWithPath(PROPERTY_VARIABLES+"[]."+ TemplateVariable.PROPERTY_NAME).description("The variable name"),
                                    fieldWithPath(PROPERTY_VARIABLES+"[]."+ TemplateVariable.PROPERTY_OPTIONAL).optional().description("Defines if the variable is optional. The default is false"),
                                    fieldWithPath(PROPERTY_VARIABLES+"[]."+ TemplateVariable.PROPERTY_VALIDATION).optional().description("Defines a simple validation segment."),
                                    fieldWithPath(PROPERTY_VARIABLES+"[]."+ TemplateVariable.PROPERTY_VALIDATION+"."+ TemplateVariableValidation.PROPERTY_MIN_LENGTH).optional().description("The minimum content length of this variable"),
                                    fieldWithPath(PROPERTY_VARIABLES+"[]."+ TemplateVariable.PROPERTY_VALIDATION+"."+ TemplateVariableValidation.PROPERTY_MAX_LENGTH).optional().description("The maximum content length of this variable"),
                                    fieldWithPath(PROPERTY_VARIABLES+"[]."+ TemplateVariable.PROPERTY_VALIDATION+"."+ TemplateVariableValidation.PROPERTY_REGULAR_EXPRESSION).optional().description("A regular expression which must match to accept the user input inside the variable")
                            ),
                            pathParameters(
                                    parameterWithName(TEMPLATE_ID.paramName()).description("The (unique) template id")
                         )
                ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminFetchesAllTemplateIds.class)
    public void restdoc_admin_fetches_templatelist() throws Exception {
        /* prepare */
        when(templateService.fetchAllTemplateIds()).thenReturn(List.of(TEST_TEMPLATE_ID1, TEST_TEMPLATE_ID2));

        String apiEndpoint = https(PORT_USED).buildAdminFetchesTemplateList();
        Class<? extends Annotation> useCase = UseCaseAdminFetchesAllTemplateIds.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(apiEndpoint, TEST_TEMPLATE_ID1).
                contentType(MediaType.APPLICATION_JSON_VALUE).
                header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
                ).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                useCaseData(useCase).
                tag(RestDocFactory.extractTag(apiEndpoint)).
                requestSchema(TestOpenApiSchema.TEMPLATES.getSchema()).
                and().
                document(
                        responseFields(
                                fieldWithPath("[]").description("Array contains all existing template identifiers")
                                )
                        ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminExecutesTemplatesHealthcheck.class)
    public void restdoc_admin_executes_templates_healthcheck() throws Exception {

        /* prepare */

        String json = """
                {
                  "status" : "WARNING",
                  "entries" : [ {
                    "type" : "ERROR",
                    "description" : "The file 'asset-1/pds-product1-id.zip' does not exist!",
                    "templateId" : "template-1",
                    "projects" : [ "project-1" ],
                    "executorConfigUUID" : "349ea899-e780-4553-bd50-06c12fe96c9e",
                    "profiles" : [ "profile-1" ],
                    "hints" : [ "At least one combination of executor and profile is enabled.", "At least one executor config is enabled.", "At least one profile is enabled." ],
                    "solution" : "Upload a file 'pds-product1-id.zip' to asset folder 'asset-1'",
                    "assetId" : "asset-1",
                    "fileName" : "pds-product1-id.zip"
                  }, {
                    "type" : "WARNING",
                    "description" : "The file 'asset-1/pds-product2-id.zip' does not exist!",
                    "templateId" : "template-1",
                    "projects" : [ "project-2" ],
                    "executorConfigUUID" : "2b25b007-f3d2-4591-ba42-409e19d9a5e8",
                    "profiles" : [ "profile-2" ],
                    "hints" : [ "At least one executor config is not enabled.", "At least one profile is enabled." ],
                    "solution" : "Upload a file 'pds-product2-id.zip' to asset folder 'asset-1'",
                    "assetId" : "asset-1",
                    "fileName" : "pds-product2-id.zip"
                  } ]
                }
                """;
        TemplatesHealthCheckResult healthCheckResult = TemplatesHealthCheckResult.fromJson(json);

        when(templateHealthCheckService.executeHealthCheck()).thenReturn(healthCheckResult);

        String apiEndpoint = https(PORT_USED).buildAdminExecutesTemplatesCheck();
        Class<? extends Annotation> useCase = UseCaseAdminExecutesTemplatesHealthcheck.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(apiEndpoint).
                contentType(MediaType.APPLICATION_JSON_VALUE).
                header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
                ).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                useCaseData(useCase).
                tag(RestDocFactory.extractTag(apiEndpoint)).
                requestSchema(TestOpenApiSchema.TEMPLATES.getSchema()).
                and().
                document(
                    responseFields(
                        fieldWithPath("status").description("Represents overall healthcheck status. Can be one of : "+ Arrays.asList(TemplatesHealthCheckStatus.values())),
                        fieldWithPath("entries").description("A list of healthcheck status entries. Each entry represents a problem or an information."),
                        fieldWithPath("entries[].type").description("Type of this entry. Can be one of : "+ Arrays.asList(TemplateHealthCheckProblemType.values())),
                        fieldWithPath("entries[].description").description("A description about the the entry"),
                        fieldWithPath("entries[].templateId").description("The template id for the template where the problem/information is related to"),
                        fieldWithPath("entries[].projects").description("A list of projects which have the template assigned"),
                        fieldWithPath("entries[].executorConfigUUID").description("The uuid of the product executor config where the problem/info is related to (in combination with template)"),
                        fieldWithPath("entries[].profiles").description("A list of the invovled profiles, means using executor config and being assigned to projects"),
                        fieldWithPath("entries[].hints").description("A list of hints which gives additinal information. E.g. A disabled executor configuration which leads to problems will lead to a WARNING, but not to an ERROR. In this case a hint 'At least one executor config is not enabled' would be added."),
                        fieldWithPath("entries[].solution").description("A solution how to fix/resolve the problem"),
                        fieldWithPath("entries[].assetId").description("The asset identifier which is used to locate the file"),
                        fieldWithPath("entries[].fileName").description("The name of the file inside the asset")
                        )
                    )
               );

        /* @formatter:on */
    }

}
