// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;
import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.LinkedHashSet;
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

import com.daimler.sechub.docgen.util.RestDocPathFactory;
import com.daimler.sechub.domain.administration.project.Project;
import com.daimler.sechub.domain.administration.signup.SignupRepository;
import com.daimler.sechub.domain.administration.user.User;
import com.daimler.sechub.domain.administration.user.UserAdministrationRestController;
import com.daimler.sechub.domain.administration.user.UserCreationService;
import com.daimler.sechub.domain.administration.user.UserDeleteService;
import com.daimler.sechub.domain.administration.user.UserDetailInformation;
import com.daimler.sechub.domain.administration.user.UserDetailInformationService;
import com.daimler.sechub.domain.administration.user.UserGrantSuperAdminRightsService;
import com.daimler.sechub.domain.administration.user.UserListService;
import com.daimler.sechub.domain.administration.user.UserRevokeSuperAdminRightsService;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.admin.signup.UseCaseAdministratorAcceptsSignup;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorDeletesUser;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorGrantsAdminRightsToUser;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorListsAllAdmins;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorListsAllUsers;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorRevokesAdminRightsFromAdmin;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorShowsUserDetails;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;
import com.daimler.sechub.test.TestURLBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(UserAdministrationRestController.class)
@ContextConfiguration(classes = { UserAdministrationRestController.class,
		UserAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({Profiles.TEST, Profiles.ADMIN_ACCESS})
@AutoConfigureRestDocs(uriScheme="https",uriHost=ExampleConstants.URI_SECHUB_SERVER,uriPort=443)
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
	private SignupRepository signUpRepository;

	@Before
	public void before() {
	}

	@Test
	@UseCaseRestDoc(useCase = UseCaseAdministratorGrantsAdminRightsToUser.class)
	public void restdoc_grant_admin_rights_to_user() throws Exception {
		/* execute + test @formatter:off */
		this.mockMvc.perform(
				post(https(PORT_USED).buildAdminGrantsSuperAdminRightsTo(USER_ID.pathElement()),TestURLBuilder.RestDocPathParameter.USER_ID)
				)./*andDo(print()).*/
		andExpect(status().isOk()).
		andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorGrantsAdminRightsToUser.class),
				pathParameters(
					parameterWithName(USER_ID.paramName()).description("The userId of the user who becomes admin")
					)
				)

				);

		/* @formatter:on */
	}

	@Test
	@UseCaseRestDoc(useCase = UseCaseAdministratorRevokesAdminRightsFromAdmin.class)
	public void restdoc_revoke_admin_rights_from_user() throws Exception {
		/* execute + test @formatter:off */
		this.mockMvc.perform(
				post(https(PORT_USED).buildAdminGrantsSuperAdminRightsTo(USER_ID.pathElement()),TestURLBuilder.RestDocPathParameter.USER_ID)
				)./*andDo(print()).*/
		andExpect(status().isOk()).
		andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorRevokesAdminRightsFromAdmin.class),
				pathParameters(
					parameterWithName(USER_ID.paramName()).description("The userId of the user who becomes admin")
					)
				)

				);

		/* @formatter:on */
	}

	@Test
	@UseCaseRestDoc(useCase = UseCaseAdministratorDeletesUser.class)
	public void restdoc_delete_user() throws Exception {
		/* execute + test @formatter:off */
		this.mockMvc.perform(
				delete(https(PORT_USED).buildAdminDeletesUserUrl(USER_ID.pathElement()),TestURLBuilder.RestDocPathParameter.USER_ID)
				)./*andDo(print()).*/
		andExpect(status().isOk()).
		andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorDeletesUser.class),
				pathParameters(
					parameterWithName(USER_ID.paramName()).description("The userId of the user who shall be deleted")
					)
				)

				);

		/* @formatter:on */
	}
	@Test
	@UseCaseRestDoc(useCase = UseCaseAdministratorAcceptsSignup.class)
	public void restdoc_accept_user_signup() throws Exception {

		/* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).buildAdminAcceptsUserSignUpUrl(USER_ID.pathElement()),"user1")
        		)./*andDo(print()).*/
        			andExpect(status().isCreated()).
        			andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorAcceptsSignup.class),
        					pathParameters(
									parameterWithName(USER_ID.paramName()).description("The userId of the signup which shall be accepted")
								)
        					)

        		);

		/* @formatter:on */
	}

	@Test
	@UseCaseRestDoc(useCase = UseCaseAdministratorListsAllUsers.class)
	public void restdoc_list_all_users() throws Exception {

		/* execute + test @formatter:off */
		this.mockMvc.perform(
				get(https(PORT_USED).buildAdminListsUsersUrl())
				)./*andDo(print()).*/
		andExpect(status().isOk()).
		andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorListsAllUsers.class))

				);

		/* @formatter:on */
	}

	@Test
	@UseCaseRestDoc(useCase = UseCaseAdministratorListsAllAdmins.class)
	public void restdoc_list_all_admins() throws Exception {

		/* execute + test @formatter:off */
		this.mockMvc.perform(
				get(https(PORT_USED).buildAdminListsAdminsUrl())
				)./*andDo(print()).*/
		andExpect(status().isOk()).
		andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorListsAllAdmins.class))

				);

		/* @formatter:on */
	}

	@Test
	@UseCaseRestDoc(useCase = UseCaseAdministratorShowsUserDetails.class)
	public void restdoc_show_user_details() throws Exception{
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
        		get(https(PORT_USED).buildAdminShowsUserDetailsUrl(USER_ID.pathElement()),"user1")
        		)./*andDo(print()).*/
        			andExpect(status().isOk()).
        			andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorShowsUserDetails.class),
        					pathParameters(
        							parameterWithName(USER_ID.paramName()).description("The user id of user to show details for")),
        					responseFields(
        							fieldWithPath(UserDetailInformation.PROPERTY_USERNAME).description("The name of the user"),
        							fieldWithPath(UserDetailInformation.PROPERTY_EMAIL).description("The mail adress of the user"),
        							fieldWithPath(UserDetailInformation.PROPERTY_SUPERADMIN).description("True, when this user is a super administrator"),
        							fieldWithPath(UserDetailInformation.PROPERTY_PROJECTS).description("The projects the user has access to"),
        							fieldWithPath(UserDetailInformation.PROPERTY_OWNED_PROJECTS).description("The projects the user is owner of")
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
