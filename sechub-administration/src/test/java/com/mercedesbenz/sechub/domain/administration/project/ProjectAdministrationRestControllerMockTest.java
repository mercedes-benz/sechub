// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static com.mercedesbenz.sechub.test.RestDocPathParameter.PROJECT_ID;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.https;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Errors;

import com.mercedesbenz.sechub.domain.administration.TestAdministrationSecurityConfiguration;
import com.mercedesbenz.sechub.domain.administration.project.ProjectJsonInput.ProjectMetaData;
import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { ProjectAdministrationRestController.class })
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@Import(TestAdministrationSecurityConfiguration.class)
public class ProjectAdministrationRestControllerMockTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ProjectCreationService creationService;

    @MockBean
    ProjectChangeOwnerService assignOwnerService;

    @MockBean
    ProjectAssignUserService assignUserService;

    @MockBean
    ProjectDeleteService projectDeleteService;

    @MockBean
    ProjectUnassignUserService unassignUserService;

    @MockBean
    ProjectDetailInformationService detailService;

    @MockBean
    ProjectDetailChangeService detailChangeService;

    @MockBean
    ListProjectsService listProjectsService;

    @MockBean
    ProjectRepository mockedProjectRepository;

    @MockBean
    ProjectTransactionService transactionService;

    @MockBean
    CreateProjectInputValidator createProjectInputvalidator;

    @MockBean
    ProjectChangeAccessLevelService projectChangeAccessLevelService;

    @Before
    public void before() {
        when(createProjectInputvalidator.supports(ProjectJsonInput.class)).thenReturn(true);
    }

    @Test
    public void when_admin_tries_to_list_all_projects_all_2_projects_from_repo_are_returned_in_string_array() throws Exception {
        /* prepare */
        List<String> projects = new ArrayList<>();
        projects.add("project1");
        projects.add("project2");

        when(listProjectsService.listProjects()).thenReturn(projects);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(https(PORT_USED).buildAdminListsProjectsUrl()).
        		contentType(MediaType.APPLICATION_JSON_VALUE)
        		).
        			andExpect(status().isOk()).
        			andExpect(jsonPath("$.[0]", CoreMatchers.equalTo("project1"))).
        			andExpect(jsonPath("$.[1]", CoreMatchers.equalTo("project2"))
        		);

		/* @formatter:on */
    }

    @Test
    public void when_validator_marks_no_errors___calling_create_project_url_calls_create_service_and_returns_http_200() throws Exception {

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).buildAdminCreatesProjectUrl()).
        		contentType(MediaType.APPLICATION_JSON_VALUE).

        		content("{\"name\":\"projectId1\",\"description\":\"description1\",\"owner\":\"ownerName1\",\"whiteList\":{\"uris\":[\"192.168.1.1\",\"192.168.1.2\"]}}")
        		).
        			andExpect(status().isCreated()
        		);

		verify(creationService).
			createProject("projectId1","description1","ownerName1", new LinkedHashSet<>(Arrays.asList(new URI("192.168.1.1"), new URI("192.168.1.2"))), new ProjectMetaData());
		/* @formatter:on */
    }

    @Test
    public void when_validator_marks_errors___calling_create_project_url_never_calls_create_service_but_returns_http_400() throws Exception {
        /* prepare */
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Errors errors = invocation.getArgument(1);
                errors.reject("testerror");
                return null;
            }
        }).when(createProjectInputvalidator).validate(any(ProjectJsonInput.class), any(Errors.class));

        /* execute + test @formatter:off */
		  this.mockMvc.perform(
	        		post(https(PORT_USED).buildAdminCreatesProjectUrl()).
	        		contentType(MediaType.APPLICATION_JSON_VALUE)
	        		).
	        			andExpect(status().isBadRequest()
	        		);


		  verifyNoInteractions(creationService);
		/* @formatter:on */
    }

    @Test
    public void delete_project_calls_delete_service() throws Exception {

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				delete(https(PORT_USED).buildAdminDeletesProject(PROJECT_ID.pathElement()),"projectId1").
				contentType(MediaType.APPLICATION_JSON_VALUE)
				).
		andExpect(status().isOk());

		/* @formatter:on */
        verify(projectDeleteService).deleteProject("projectId1");
    }

    @Test
    public void get_project_details_returns_project_details() throws Exception {

        Project project = new Project();
        project.id = "project1";
        project.description = "description";
        project.owner = new User();

        ProjectDetailInformation details = new ProjectDetailInformation(project);

        when(detailService.fetchDetails(matches("project1"))).thenReturn(details);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).buildAdminFetchProjectInfoUrl(PROJECT_ID.pathElement()), "project1").
                contentType(MediaType.APPLICATION_JSON_VALUE).
                accept(MediaType.APPLICATION_JSON_VALUE)
                ).
        andExpect(status().isOk()).
        andExpect(jsonPath("$.projectId", CoreMatchers.equalTo("project1"))).
        andExpect(jsonPath("$.description", CoreMatchers.equalTo("description"))).
        andExpect(jsonPath("$.owner", CoreMatchers.nullValue())).
        andExpect(jsonPath("$.users", CoreMatchers.notNullValue()));

        verify(detailService).fetchDetails(matches("project1"));
        /* @formatter:on */
    }

    @Test
    public void change_project_calls_change_details() throws Exception {

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                post(https(PORT_USED).buildAdminChangesProjectDescriptionUrl("project1")).
                contentType(MediaType.APPLICATION_JSON_VALUE).
                accept(MediaType.APPLICATION_JSON_VALUE).
                content("{\"description\":\"new description\"}")
                ).
        andDo(print()).
        andExpect(status().isOk()).
        andReturn();

        verify(detailChangeService).changeProjectDescription(matches("project1"), any());
        /* @formatter:on */
    }

    @Test
    public void when_admin_tries_to_change_project_description_but_request_body_is_missing() throws Exception {

        /* execute + test @formatter:off */

        this.mockMvc.perform(
                post(https(PORT_USED).buildAdminChangesProjectDescriptionUrl(PROJECT_ID.pathElement()), "project1").
                contentType(MediaType.APPLICATION_JSON).
                content("")
                ).
        andExpect(status().isBadRequest());

        /* @formatter:on */
    }
}
