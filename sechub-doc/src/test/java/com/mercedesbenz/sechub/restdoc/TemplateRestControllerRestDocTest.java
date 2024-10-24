// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.*;
import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
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
import com.mercedesbenz.sechub.domain.scan.template.TemplateRepository;
import com.mercedesbenz.sechub.domain.scan.template.TemplateRestController;
import com.mercedesbenz.sechub.domain.scan.template.TemplateService;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminCreatesOrUpdatesTemplate;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminDeletesTemplate;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesAllTemplateIds;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesTemplate;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(TemplateRestController.class)
@ContextConfiguration(classes = { TemplateRestController.class, TemplateRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class TemplateRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemplateRepository tempplateRepository;

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
        definition.getAssets().add("asset-id1");

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
			post(apiEndpoint, TEST_TEMPLATE_ID1).
			contentType(MediaType.APPLICATION_JSON_VALUE).
	        content(content).
	        header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
        ).
		andExpect(status().isOk()).
		andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    requestSchema(OpenApiSchema.TEMPLATES.getSchema()).
                and().
                document(
                            requestFields(
                                    fieldWithPath(PROPERTY_TYPE).description("The template type. Must be be defined when a new template is created. An update will ignore changes of this property because the type is immutable! Currently supported types are: "+ TemplateType.values()),

                                    fieldWithPath(PROPERTY_ASSETS).description("An array list containing ids of referenced assets"),
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
                header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                useCaseData(useCase).
                tag(RestDocFactory.extractTag(apiEndpoint)).
                requestSchema(OpenApiSchema.TEMPLATES.getSchema()).
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
            header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
        ).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    requestSchema(OpenApiSchema.TEMPLATES.getSchema()).
                and().
                document(
                            responseFields(
                                    fieldWithPath(PROPERTY_TYPE).description("The template type. Currently supported types are: "+ TemplateType.values()),

                                    fieldWithPath(PROPERTY_ID).description("The (unique) template id"),
                                    fieldWithPath(PROPERTY_ASSETS).description("An array list containing ids of referenced assets"),
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
                header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                useCaseData(useCase).
                tag(RestDocFactory.extractTag(apiEndpoint)).
                requestSchema(OpenApiSchema.TEMPLATES.getSchema()).
                and().
                document(
                        responseFields(
                                fieldWithPath("[]").description("Array contains all existing template identifiers")
                                )
                        ));

        /* @formatter:on */
    }

    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractSecHubAPISecurityConfiguration {

    }

}
