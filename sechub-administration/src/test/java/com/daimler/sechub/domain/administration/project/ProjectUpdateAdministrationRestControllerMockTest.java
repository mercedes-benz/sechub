// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.net.URI;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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
import org.springframework.validation.Errors;

import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(ProjectUpdateAdministrationRestController.class)
@ContextConfiguration(classes = { ProjectUpdateAdministrationRestController.class,
		ProjectUpdateAdministrationRestControllerMockTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({Profiles.TEST, Profiles.ADMIN_ACCESS})
public class ProjectUpdateAdministrationRestControllerMockTest {

	private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	ProjectUpdateWhitelistService mockedProjectUpdateWhiteListService;

	@MockBean
	UpdateProjectInputValidator mockedValidator;

	@MockBean
	ProjectRepository mockedProjectRepository;

	@Before
	public void before() {
		when(mockedValidator.supports(ProjectJsonInput.class)).thenReturn(true);
	}

	@Test
	public void when_validator_marks_no_errors___calling_update_project_url_calls_update_service_and_returns_http_200() throws Exception {

		/* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).buildUpdateProjectWhiteListUrl("projectId1")).
        		contentType(MediaType.APPLICATION_JSON_VALUE).
        		content("{\"whiteList\":{\"uris\":[\"192.168.1.1\",\"192.168.1.2\"]}}")
        		)./*andDo(print()).*/
        			andExpect(status().isOk()
        		);

		verify(mockedProjectUpdateWhiteListService).
			updateProjectWhitelist("projectId1",
				Arrays.asList(new URI("192.168.1.1"), new URI("192.168.1.2")));
		/* @formatter:on */
	}

	@Test
	public void when_validator_marks_errors___calling_update_project_url_never_calls_update_service_but_returns_http_400() throws Exception {
		/* prepare */
		doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Errors errors = invocation.getArgument(1);
                errors.reject("testerror");
                return null;
            }
        }).when(mockedValidator).validate(any(ProjectJsonInput.class), any(Errors.class));


		/* execute + test @formatter:off */
		  this.mockMvc.perform(
	        		post(https(PORT_USED).buildUpdateProjectWhiteListUrl("projectId1")).
	        		contentType(MediaType.APPLICATION_JSON_VALUE).
	        		content("{\"whiteList\":{\"uris\":[\"192.168.1.1\",\"192.168.1.2\"]}}")
	        		)./*andDo(print()).*/
	        			andExpect(status().isBadRequest()
	        		);


		  verifyNoInteractions(mockedProjectUpdateWhiteListService);
		/* @formatter:on */
	}

	@TestConfiguration
	@Profile(Profiles.TEST)
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

	}

}
