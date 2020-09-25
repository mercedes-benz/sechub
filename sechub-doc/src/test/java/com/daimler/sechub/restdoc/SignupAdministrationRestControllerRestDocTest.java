// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

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
import com.daimler.sechub.domain.administration.signup.SignupAdministrationRestController;
import com.daimler.sechub.domain.administration.signup.SignupDeleteService;
import com.daimler.sechub.domain.administration.signup.SignupRepository;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.admin.signup.UseCaseAdministratorDeletesSignup;
import com.daimler.sechub.sharedkernel.usecases.admin.signup.UseCaseAdministratorListsOpenUserSignups;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(SignupAdministrationRestController.class)
@ContextConfiguration(classes = { SignupAdministrationRestController.class,
		SignupAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({Profiles.TEST, Profiles.ADMIN_ACCESS})
@AutoConfigureRestDocs(uriScheme="https",uriHost=ExampleConstants.URI_SECHUB_SERVER,uriPort=443)
public class SignupAdministrationRestControllerRestDocTest {

	private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SignupDeleteService deleteService;

	@MockBean
	private SignupRepository signupRepository;

	@Before
	public void before() {
	}

	@Test
	@UseCaseRestDoc(useCase = UseCaseAdministratorListsOpenUserSignups.class)
	public void restdoc_list_user_signups() throws Exception {
		/* prepare */
		when(signupRepository.findAll()).thenReturn(Collections.emptyList());

		/* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(https(PORT_USED).buildAdminListsUserSignupsUrl())
        		)./*andDo(print()).*/
        			andExpect(status().isOk()).
        			andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorListsOpenUserSignups.class)
        		)

        	    );

		/* @formatter:on */
	}

	@Test
	@UseCaseRestDoc(useCase=UseCaseAdministratorDeletesSignup.class)
	public void restdoc_delete_signup() throws Exception {

		/* execute + test @formatter:off */
        this.mockMvc.perform(
        		delete(https(PORT_USED).buildAdminDeletesUserSignUpUrl(USER_ID.pathElement()),"userId1")
        		)./*andDo(print()).*/
        			andExpect(status().isOk()).
        			andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorDeletesSignup.class),
        					pathParameters(
									parameterWithName(USER_ID.paramName()).description("The userId of the signup which shall be deleted")
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
