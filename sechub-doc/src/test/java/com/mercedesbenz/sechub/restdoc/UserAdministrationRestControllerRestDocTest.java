// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.mercedesbenz.sechub.test.TestURLBuilder.*;
import static com.mercedesbenz.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.administration.project.Project;
import com.mercedesbenz.sechub.domain.administration.signup.SignupRepository;
import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.domain.administration.user.UserAdministrationRestController;
import com.mercedesbenz.sechub.domain.administration.user.UserCreationService;
import com.mercedesbenz.sechub.domain.administration.user.UserDeleteService;
import com.mercedesbenz.sechub.domain.administration.user.UserDetailInformation;
import com.mercedesbenz.sechub.domain.administration.user.UserDetailInformationService;
import com.mercedesbenz.sechub.domain.administration.user.UserEmailAddressUpdateService;
import com.mercedesbenz.sechub.domain.administration.user.UserGrantSuperAdminRightsService;
import com.mercedesbenz.sechub.domain.administration.user.UserListService;
import com.mercedesbenz.sechub.domain.administration.user.UserRevokeSuperAdminRightsService;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.signup.UseCaseAdminAcceptsSignup;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminDeletesUser;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminGrantsAdminRightsToUser;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminListsAllAdmins;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminListsAllUsers;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminRevokesAdminRightsFromAdmin;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminShowsUserDetails;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminUpdatesUserEmailAddress;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestPortProvider;
import com.mercedesbenz.sechub.test.TestURLBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(UserAdministrationRestController.class)
@ContextConfiguration(classes = { UserAdministrationRestController.class, UserAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class UserAdministrationRestControllerRestDocTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserCreationService userCreationService;

    @MockBean
    private UserListService userListService;

    @MockBean
    private UserDeleteService userDeleteService;

    @MockBean
    private UserDetailInformationService userDetailService;

    @MockBean
    private UserGrantSuperAdminRightsService userGrantSuperAdminRightsService;

    @MockBean
    private UserRevokeSuperAdminRightsService userRevokeSuperAdminRightsService;

    @MockBean
    private UserEmailAddressUpdateService userEmailAddressUpdateService;
    
    @MockBean
    private SignupRepository signUpRepository;

    @Before
    public void before() {
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminUpdatesUserEmailAddress.class)
    public void restdoc_admin_updates_user_email_address() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminChangesUserEmailAddress(USER_ID.pathElement(),EMAIL_ADDRESS.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminUpdatesUserEmailAddress.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                put(apiEndpoint, USER_ID, EMAIL_ADDRESS)
                )./*andDo(print()).*/
        andExpect(status().isOk()).
        andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            pathParameters(
                                    parameterWithName(USER_ID.paramName()).description("The userId of the user whose email adress will be changed"),
                                    parameterWithName(EMAIL_ADDRESS.paramName()).description("The new email address")
                                    
                            ).
                            build()
                        )
                ));

        /* @formatter:on */
    }
    
    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminGrantsAdminRightsToUser.class)
    public void restdoc_grant_admin_rights_to_user() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminGrantsSuperAdminRightsTo(USER_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminGrantsAdminRightsToUser.class;

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				post(apiEndpoint, TestURLBuilder.RestDocPathParameter.USER_ID)
				)./*andDo(print()).*/
		andExpect(status().isOk()).
		andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            pathParameters(
                                    parameterWithName(USER_ID.paramName()).description("The userId of the user who becomes admin")
                            ).
                            build()
                        )
				));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminRevokesAdminRightsFromAdmin.class)
    public void restdoc_revoke_admin_rights_from_user() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminRevokesSuperAdminRightsFrom(USER_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminRevokesAdminRightsFromAdmin.class;

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				post(apiEndpoint,TestURLBuilder.RestDocPathParameter.USER_ID)
				).
		andExpect(status().isOk()).
		andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            pathParameters(
                                    parameterWithName(USER_ID.paramName()).description("The userId of the user who becomes admin")
                            ).
                            build()
                        )
				));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminDeletesUser.class)
    public void restdoc_delete_user() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminDeletesUserUrl(USER_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminDeletesUser.class;

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				delete(apiEndpoint, TestURLBuilder.RestDocPathParameter.USER_ID)
				).
		andExpect(status().isOk()).
		andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            pathParameters(
                                    parameterWithName(USER_ID.paramName()).description("The userId of the user who shall be deleted")
                            ).
                            build()
                        )
				));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminAcceptsSignup.class)
    public void restdoc_accept_user_signup() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminAcceptsUserSignUpUrl(USER_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminAcceptsSignup.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(apiEndpoint, "user1")
        		).
        			andExpect(status().isCreated()).
        			andDo(document(RestDocFactory.createPath(useCase),
        	                resource(
        	                        ResourceSnippetParameters.builder().
        	                            summary(RestDocFactory.createSummary(useCase)).
        	                            description(RestDocFactory.createDescription(useCase)).
        	                            tag(RestDocFactory.extractTag(apiEndpoint)).
        	                            pathParameters(
        	                                    parameterWithName(USER_ID.paramName()).description("The userId of the signup which shall be accepted")
        	                            ).
        	                            build()
        	                        )
        		));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminListsAllUsers.class)
    public void restdoc_list_all_users() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminListsUsersUrl();
        Class<? extends Annotation> useCase = UseCaseAdminListsAllUsers.class;

        List<String> users = new LinkedList<>();
        users.add("user1");
        users.add("user2");
        users.add("admin1");

        when(userListService.listUsers()).thenReturn(users);

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				get(apiEndpoint)
				).
		andExpect(status().isOk()).
		andDo(document(RestDocFactory.createPath(UseCaseAdminListsAllUsers.class),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            responseSchema(OpenApiSchema.USER_LIST.getSchema()).
                            responseFields(
                                    fieldWithPath("[]").description("List of user Ids").optional()
                            ).
                            build()
                        )
		        ));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminListsAllAdmins.class)
    public void restdoc_list_all_admins() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminListsAdminsUrl();
        Class<? extends Annotation> useCase = UseCaseAdminListsAllAdmins.class;

        List<String> admins = new LinkedList<>();
        admins.add("admin1");
        admins.add("admin2");

        when(userListService.listAdministrators()).thenReturn(admins);

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				get(apiEndpoint)
				)./*andDo(print()).*/
		andExpect(status().isOk()).
		andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            responseSchema(OpenApiSchema.USER_LIST.getSchema()).
                            responseFields(
                                    fieldWithPath("[]").description("List of admin Ids").optional()
                            ).
                            build()
                        )
				));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminShowsUserDetails.class)
    public void restdoc_show_user_details() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminShowsUserDetailsUrl(USER_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminShowsUserDetails.class;

        User user = mock(User.class);
        when(user.getName()).thenReturn("user1");
        when(user.getEmailAdress()).thenReturn("user1@example.org");
        Set<Project> projects = new LinkedHashSet<>();

        Project project1 = mock(Project.class);
        when(project1.getId()).thenReturn("project1");
        projects.add(project1);
        when(user.getProjects()).thenReturn(projects);
        UserDetailInformation info = new UserDetailInformation(user);

        when(userDetailService.fetchDetails("user1")).thenReturn(info);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(apiEndpoint, "user1")
        		).
        			andExpect(status().isOk()).
        			andDo(document(RestDocFactory.createPath(useCase),
        	                resource(
        	                        ResourceSnippetParameters.builder().
        	                            summary(RestDocFactory.createSummary(useCase)).
        	                            description(RestDocFactory.createDescription(useCase)).
        	                            tag(RestDocFactory.extractTag(apiEndpoint)).
        	                            responseSchema(OpenApiSchema.USER_DETAILS.getSchema()).
        	                            pathParameters(
        	                                    parameterWithName(USER_ID.paramName()).description("The user id of user to show details for")
        	                            ).
        	                            responseFields(
        	                                    fieldWithPath(UserDetailInformation.PROPERTY_USERNAME).description("The name of the user"),
        	                                    fieldWithPath(UserDetailInformation.PROPERTY_EMAIL).description("The mail adress of the user"),
        	                                    fieldWithPath(UserDetailInformation.PROPERTY_SUPERADMIN).description("True, when this user is a super administrator"),
        	                                    fieldWithPath(UserDetailInformation.PROPERTY_PROJECTS).description("The projects the user has access to"),
        	                                    fieldWithPath(UserDetailInformation.PROPERTY_OWNED_PROJECTS).description("The projects the user is owner of")
        	                            ).
        	                            build()
        	                        )

        					)
        		);

        /* @formatter:on */
    }

    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

    }

}
