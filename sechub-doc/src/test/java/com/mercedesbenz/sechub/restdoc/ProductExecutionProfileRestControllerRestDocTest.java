// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfile.*;
import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.scan.product.config.CreateProductExecutionProfileService;
import com.mercedesbenz.sechub.domain.scan.product.config.DeleteProductExecutionProfileService;
import com.mercedesbenz.sechub.domain.scan.product.config.FetchProductExecutionProfileListService;
import com.mercedesbenz.sechub.domain.scan.product.config.FetchProductExecutionProfileService;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfile;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfileRestController;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfilesList;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigRepository;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupCredentials;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigValidation;
import com.mercedesbenz.sechub.domain.scan.product.config.UpdateProductExecutionProfileService;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminAssignsExecutionProfileToProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminCreatesExecutionProfile;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminDeletesExecutionProfile;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesExecutionProfile;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesExecutionProfileList;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUnassignsExecutionProfileFromProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUpdatesExecutionProfile;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;
import com.mercedesbenz.sechub.test.executionprofile.TestExecutionProfile;
import com.mercedesbenz.sechub.test.executionprofile.TestExecutionProfileList;
import com.mercedesbenz.sechub.test.executionprofile.TestExecutionProfileListEntry;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorConfig;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorSetupJobParam;

@RunWith(SpringRunner.class)
@WebMvcTest(ProductExecutionProfileRestController.class)
@ContextConfiguration(classes = { ProductExecutionProfileRestController.class, ProductExecutionProfileRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class ProductExecutionProfileRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductExecutorConfigRepository repo;

    @MockBean
    ProductExecutorConfigValidation validation;

    @MockBean
    CreateProductExecutionProfileService createService;

    @MockBean
    DeleteProductExecutionProfileService deleteService;

    @MockBean
    FetchProductExecutionProfileListService fetchListService;

    @MockBean
    UpdateProductExecutionProfileService updateService;

    @MockBean
    FetchProductExecutionProfileService fetchService;

    @MockBean
    AuditLogService auditLogService;

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminCreatesExecutionProfile.class)
    public void restdoc_admin_creates_profile() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminCreatesProductExecutionProfile(PROFILE_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminCreatesExecutionProfile.class;

        String profileId = "new-profile-1";

        TestExecutionProfile profile = new TestExecutionProfile();
        profile.description = "a short description for profile";
        profile.enabled = false;

        /* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		post(apiEndpoint, profileId).
	    			contentType(MediaType.APPLICATION_JSON_VALUE).
	    			content(JSONConverter.get().toJSON(profile)).
	    			header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
	    		).
	    			andExpect(status().isCreated()).
	    			andDo(defineRestService().
                            with().
                                useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                requestSchema(OpenApiSchema.EXECUTION_PROFILE_CREATE.getSchema()).
                            and().
                            document(
	                            		requestHeaders(

	                            		),
                                        requestFields(
                                                fieldWithPath(PROPERTY_DESCRIPTION).description("A short description for the profile"),
                                                fieldWithPath(PROPERTY_ENABLED).description("Enabled state of profile, default is false").optional(),
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]").description("Configurations can be linked at creation time as well - see update description").optional(),
                                                fieldWithPath(PROPERTY_PROJECT_IDS+"[]").description("Projects can be linked by their ids at creation time as well - see update description").optional()
                                        ),
                                        pathParameters(
                                                parameterWithName(PROFILE_ID.paramName()).description("The profile id")
                                     )
	    			        ));

	    /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminUpdatesExecutionProfile.class)
    public void restdoc_admin_updates_profile() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminUpdatesProductExecutionProfile(PROFILE_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminUpdatesExecutionProfile.class;

        String profileId = "existing-profile-1";

        TestExecutionProfile profile = new TestExecutionProfile();
        profile.description = "changed description";
        profile.enabled = true;
        UUID randomUUID = UUID.randomUUID();

        TestExecutorConfig configFromUser = new TestExecutorConfig();
        configFromUser.uuid = randomUUID;

        profile.configurations.add(configFromUser);
        profile.projectIds = null;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                put(apiEndpoint, profileId).
                    contentType(MediaType.APPLICATION_JSON_VALUE).
                    content(JSONConverter.get().toJSON(profile)).
                    header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
                    andExpect(status().isOk()).
                    andDo(defineRestService().
                            with().
                                useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                requestSchema(OpenApiSchema.EXECUTION_PROFILE_UPDATE.getSchema()).
                            and().
                            document(
	                            		requestHeaders(

	                            		),
                                        requestFields(
                                                fieldWithPath(PROPERTY_DESCRIPTION).description("A short description for the profile"),
                                                fieldWithPath(PROPERTY_ENABLED).description("Enabled state of profile, default is false").optional(),
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]."+ProductExecutorConfig.PROPERTY_UUID).description("Add uuid for configuration to use here"),
                                                /* ignore next parts - only inside test json, also ignored at update, because there only uuid is used */
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]."+ProductExecutorConfig.PROPERTY_ENABLED).ignored(),
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]."+ProductExecutorConfig.PROPERTY_EXECUTORVERSION).ignored(),
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]."+ProductExecutorConfig.PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_CREDENTIALS).ignored(),
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]."+ProductExecutorConfig.PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_JOBPARAMETERS).ignored()
                                        ),
                                        pathParameters(
                                                parameterWithName(PROFILE_ID.paramName()).description("The profile id")
                                     )
                            ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminAssignsExecutionProfileToProject.class)
    public void restdoc_admin_assigns_executionprofile_to_project() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminAddsProjectToExecutionProfile(PROFILE_ID.pathElement(), PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminAssignsExecutionProfileToProject.class;

        String profileId = "profile-1";
        String projectId = "project-1";

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                	post(apiEndpoint, profileId,projectId).
                    contentType(MediaType.APPLICATION_JSON_VALUE).
                    header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
                    andExpect(status().isCreated()).
                    andDo(defineRestService().
                            with().
                                useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                            and().
                            document(
	                            		requestHeaders(

	                            		),
	                                    pathParameters(
	                                                parameterWithName(PROJECT_ID.paramName()).description("The project id "),
	                                                parameterWithName(PROFILE_ID.paramName()).description("The profile id")
	                                    )
                    ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminUnassignsExecutionProfileFromProject.class)
    public void restdoc_admin_unassigns_executionprofile_from_project() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminRemovesProjectFromExecutionProfile(PROFILE_ID.pathElement(), PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminUnassignsExecutionProfileFromProject.class;

        String profileId = "profile-1";
        String projectId = "project-1";

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                delete(apiEndpoint, profileId,projectId).
                    contentType(MediaType.APPLICATION_JSON_VALUE).
                    header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
                    andExpect(status().isOk()).
                    andDo(defineRestService().
                            with().
                                useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                            and().
                            document(
	                            		requestHeaders(

	                            		),
                                        pathParameters(
                                                parameterWithName(PROJECT_ID.paramName()).description("The project id "),
                                                parameterWithName(PROFILE_ID.paramName()).description("The profile id")
                                        )
                    ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminFetchesExecutionProfile.class)
    public void restdoc_admin_fetches_profile() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminFetchesProductExecutionProfile(PROFILE_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminFetchesExecutionProfile.class;

        String profileId = "existing-profile-1";

        TestExecutionProfile testprofile = new TestExecutionProfile();
        testprofile.description = "a description";
        testprofile.enabled = true;
        UUID randomUUID = UUID.randomUUID();

        TestExecutorConfig configFromUser = new TestExecutorConfig();
        configFromUser.enabled = false;
        configFromUser.name = "New name";
        configFromUser.productIdentifier = ProductIdentifier.PDS_CODESCAN.name();
        configFromUser.executorVersion = 1;
        configFromUser.setup.baseURL = "https://product.example.com";
        configFromUser.setup.credentials.user = "env:EXAMPLE_USENAME";
        configFromUser.setup.credentials.password = "env:EXAMPLE_PASSWORD";
        configFromUser.uuid = randomUUID;

        TestExecutorSetupJobParam param1 = new TestExecutorSetupJobParam("example.key1",
                "A value but changed. Remark: the other parameter (example.key2) has been removed by this call");
        configFromUser.setup.jobParameters.add(param1);

        testprofile.configurations.add(configFromUser);
        testprofile.projectIds.add("project-1");
        testprofile.projectIds.add("project-2");

        ProductExecutionProfile profile = JSONConverter.get().fromJSON(ProductExecutionProfile.class, JSONConverter.get().toJSON(testprofile));

        when(fetchService.fetchProductExecutorConfig(profileId)).thenReturn(profile);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(apiEndpoint, profileId).
                    contentType(MediaType.APPLICATION_JSON_VALUE).
                    header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
                    andExpect(status().isOk()).
                    andDo(defineRestService().
                            with().
                                useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                responseSchema(OpenApiSchema.EXECUTION_PROFILE_FETCH.getSchema()).
                            and().
                            document(
	                            		requestHeaders(

	                            		),
                                        responseFields(
                                                fieldWithPath(PROPERTY_ID).optional().ignored(),
                                                fieldWithPath(PROPERTY_DESCRIPTION).description("A short description for the profile"),
                                                fieldWithPath(PROPERTY_ENABLED).description("Enabled state of profile, default is false").optional(),
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]."+ProductExecutorConfig.PROPERTY_UUID).description("uuid of configuration"),
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]."+ProductExecutorConfig.PROPERTY_NAME).description("name of configuration"),
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]."+ProductExecutorConfig.PROPERTY_ENABLED).description("enabled state of this config"),
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]."+ProductExecutorConfig.PROPERTY_PRODUCTIDENTIFIER).description("executed product"),
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]."+ProductExecutorConfig.PROPERTY_EXECUTORVERSION).description("executor version"),
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]."+ProductExecutorConfig.PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_BASEURL).ignored(),
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]."+ProductExecutorConfig.PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_CREDENTIALS+"."+ProductExecutorConfigSetupCredentials.PROPERTY_USER).ignored(),
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]."+ProductExecutorConfig.PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_CREDENTIALS+"."+ProductExecutorConfigSetupCredentials.PROPERTY_PASSWORD).ignored(),
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]."+ProductExecutorConfig.PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_JOBPARAMETERS+"[]."+ProductExecutorConfigSetupJobParameter.PROPERTY_KEY).ignored(),
                                                fieldWithPath(PROPERTY_CONFIGURATIONS+"[]."+ProductExecutorConfig.PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_JOBPARAMETERS+"[]."+ProductExecutorConfigSetupJobParameter.PROPERTY_VALUE).ignored(),
                                                fieldWithPath(PROPERTY_PROJECT_IDS+"[]").description("Projects can be linked by their ids here")
                                        ),
                                        pathParameters(
                                                parameterWithName(PROFILE_ID.paramName()).description("The profile id")
                                     )
                            ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminDeletesExecutionProfile.class)
    public void restDoc_admin_deletes_profile() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminDeletesProductExecutionProfile(PROFILE_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminDeletesExecutionProfile.class;

        /* execute + test @formatter:off */
	    String profileId= "profile-to-delete-1";
	    this.mockMvc.perform(
                delete(apiEndpoint, profileId).
                    contentType(MediaType.APPLICATION_JSON_VALUE).
                    header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
                    andExpect(status().isOk()).
                    andDo(defineRestService().
                            with().
                                useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                            and().
                            document(
	                            		requestHeaders(

	                            		),
                                        pathParameters(
                                                parameterWithName(PROFILE_ID.paramName()).description("The profile id")
                                        )
                            ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminFetchesExecutionProfileList.class)
    public void restDoc_admin_fetches_profiles_list() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminFetchesListOfProductExecutionProfiles();
        Class<? extends Annotation> useCase = UseCaseAdminFetchesExecutionProfileList.class;

        TestExecutionProfileList profileList = new TestExecutionProfileList();
        TestExecutionProfileListEntry entry1 = new TestExecutionProfileListEntry();
        entry1.description = "A short decription for profile1";
        entry1.id = "profile1";

        TestExecutionProfileListEntry entry2 = new TestExecutionProfileListEntry();
        entry2.description = "A short decription for profile2";
        entry2.id = "profile2";

        profileList.executionProfiles.add(entry1);
        profileList.executionProfiles.add(entry2);

        ProductExecutionProfilesList list = JSONConverter.get().fromJSON(ProductExecutionProfilesList.class, JSONConverter.get().toJSON(profileList));

        when(fetchListService.fetchProductExecutionProfileList()).thenReturn(list);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(apiEndpoint).
                    contentType(MediaType.APPLICATION_JSON_VALUE).
                    header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
                    andExpect(status().isOk()).
                    andDo(defineRestService().
                            with().
                                useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                responseSchema(OpenApiSchema.EXECUTION_PROFILE_LIST.getSchema()).
                            and().
                            document(
	                            		requestHeaders(

	                            		),
                                        responseFields(
                                                fieldWithPath("type").description("Always `executorProfileList` as an identifier for the list"),
                                                fieldWithPath("executionProfiles[]."+PROPERTY_ID).description("The profile id"),
                                                fieldWithPath("executionProfiles[]."+PROPERTY_DESCRIPTION).description("A profile description"),
                                                fieldWithPath("executionProfiles[]."+PROPERTY_ENABLED).description("Enabled state of profile")
                                        )
                            ));

        /* @formatter:on */
    }

    @TestConfiguration
    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

    }
}
