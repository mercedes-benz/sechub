// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentationTest.*;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
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
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.signup.UseCaseAdminAcceptsSignup;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminDeletesUser;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminGrantsAdminRightsToUser;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminListsAllAdmins;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminListsAllUsers;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminRevokesAdminRightsFromAdmin;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminShowsUserDetails;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminShowsUserDetailsForEmailAddress;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminUpdatesUserEmailAddress;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.RestDocPathParameter;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { UserAdministrationRestController.class })
@Import(TestRestDocSecurityConfiguration.class)
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class UserAdministrationRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

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
        String apiEndpoint = https(PORT_USED).buildAdminChangesUserEmailAddress(USER_ID.pathElement(), EMAIL_ADDRESS.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminUpdatesUserEmailAddress.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                  put(apiEndpoint, USER_ID, EMAIL_ADDRESS).
                  	header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
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
                                    parameterWithName(USER_ID.paramName()).description("The userId of the user whose email address will be changed"),
                                    parameterWithName(EMAIL_ADDRESS.paramName()).description("The new email address")

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
				  post(apiEndpoint, RestDocPathParameter.USER_ID).
				  	header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
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
                                    parameterWithName(USER_ID.paramName()).description("The userId of the user who becomes admin")
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
				  post(apiEndpoint,RestDocPathParameter.USER_ID).
				  	header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
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
                                    parameterWithName(USER_ID.paramName()).description("The userId of the user who becomes admin")
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
					delete(apiEndpoint, RestDocPathParameter.USER_ID).
					header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
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
                                parameterWithName(USER_ID.paramName()).description("The userId of the user who shall be deleted")
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
        		  post(apiEndpoint, "user1").
        		  header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
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
        	                                 parameterWithName(USER_ID.paramName()).description("The userId of the signup which shall be accepted")
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
				  get(apiEndpoint).
				  header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
				).
		andExpect(status().isOk()).
		andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    responseSchema(TestOpenApiSchema.USER_LIST.getSchema()).
                and().
                document(
                            responseFields(
                                    fieldWithPath("[]").description("List of user Ids").optional()
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
				  get(apiEndpoint).
				  header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
				).
		andExpect(status().isOk()).
		andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    responseSchema(TestOpenApiSchema.USER_LIST.getSchema()).
                and().
                document(
	                		requestHeaders(

	                		),
                            responseFields(
                                    fieldWithPath("[]").description("List of admin Ids").optional()
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
        when(user.getEmailAddress()).thenReturn("user1@example.org");

        UserDetailInformation info = new UserDetailInformation(user, Set.of("project1"), Set.of());

        when(userDetailService.fetchDetailsById("user1")).thenReturn(info);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        			get(apiEndpoint, "user1").
  				  	header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
        		).
        			andExpect(status().isOk()).
        			andDo(defineRestService().
                            with().
                                useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                responseSchema(TestOpenApiSchema.USER_DETAILS.getSchema()).
                            and().
                            document(
	                            		requestHeaders(

	                            		),
        	                            pathParameters(
        	                                    parameterWithName(USER_ID.paramName()).description("The user id of user to show details for")
        	                            ),
        	                            responseFields(
        	                                    fieldWithPath(UserDetailInformation.PROPERTY_USERNAME).description("The name of the user"),
        	                                    fieldWithPath(UserDetailInformation.PROPERTY_EMAIL).description("The email address of the user"),
        	                                    fieldWithPath(UserDetailInformation.PROPERTY_SUPERADMIN).description("True, when this user is a super administrator"),
        	                                    fieldWithPath(UserDetailInformation.PROPERTY_PROJECTS).description("The projects the user has access to"),
        	                                    fieldWithPath(UserDetailInformation.PROPERTY_OWNED_PROJECTS).description("The projects the user is owner of")
        	                        )

        					)
        		);

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminShowsUserDetailsForEmailAddress.class)
    public void restdoc_show_user_details_for_email_address() throws Exception {
        /* prepare */
        String emailAddress = "user1@example.org";
        String userId = "user1";

        String apiEndpoint = https(PORT_USED).buildAdminShowsUserDetailsForEmailAddressUrl(EMAIL_ADDRESS.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminShowsUserDetailsForEmailAddress.class;

        User user = mock(User.class);
        when(user.getName()).thenReturn(userId);
        when(user.getEmailAddress()).thenReturn(emailAddress);

        UserDetailInformation info = new UserDetailInformation(user, Set.of("project1"), Set.of());

        when(userDetailService.fetchDetailsByEmailAddress(emailAddress)).thenReturn(info);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(apiEndpoint, emailAddress).
                header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
                ).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                useCaseData(useCase).
                tag(RestDocFactory.extractTag(apiEndpoint)).
                responseSchema(TestOpenApiSchema.USER_DETAILS.getSchema()).
                and().
                document(
                        requestHeaders(
                                headerWithName(TestAuthenticationHelper.HEADER_NAME).description(TestAuthenticationHelper.HEADER_DESCRIPTION)
                                ),
                        pathParameters(
                                parameterWithName(EMAIL_ADDRESS.paramName()).description("The email address of user to show details for")
                                ),
                        responseFields(
                                fieldWithPath(UserDetailInformation.PROPERTY_USERNAME).description("The name of the user"),
                                fieldWithPath(UserDetailInformation.PROPERTY_EMAIL).description("The mail address of the user"),
                                fieldWithPath(UserDetailInformation.PROPERTY_SUPERADMIN).description("True, when this user is a super administrator"),
                                fieldWithPath(UserDetailInformation.PROPERTY_PROJECTS).description("The projects the user has access to"),
                                fieldWithPath(UserDetailInformation.PROPERTY_OWNED_PROJECTS).description("The projects the user is owner of")
                                )

                        )
                );

        /* @formatter:on */
    }

}
