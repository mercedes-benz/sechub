// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.net.URI;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.daimler.sechub.docgen.util.RestDocPathFactory;
import com.daimler.sechub.domain.administration.project.CreateProjectInputValidator;
import com.daimler.sechub.domain.administration.project.Project;
import com.daimler.sechub.domain.administration.project.ProjectAdministrationRestController;
import com.daimler.sechub.domain.administration.project.ProjectAssignOwnerService;
import com.daimler.sechub.domain.administration.project.ProjectAssignUserService;
import com.daimler.sechub.domain.administration.project.ProjectCreationService;
import com.daimler.sechub.domain.administration.project.ProjectDeleteService;
import com.daimler.sechub.domain.administration.project.ProjectDetailChangeService;
import com.daimler.sechub.domain.administration.project.ProjectDetailInformation;
import com.daimler.sechub.domain.administration.project.ProjectDetailInformationService;
import com.daimler.sechub.domain.administration.project.ProjectJsonInput;
import com.daimler.sechub.domain.administration.project.ProjectJsonInput.ProjectWhiteList;
import com.daimler.sechub.domain.administration.project.ProjectMetaDataEntity;
import com.daimler.sechub.domain.administration.project.ProjectRepository;
import com.daimler.sechub.domain.administration.project.ProjectUnassignUserService;
import com.daimler.sechub.domain.administration.project.ProjectUpdateWhitelistService;
import com.daimler.sechub.domain.administration.user.User;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorChangesProjectDescription;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorCreatesProject;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorDeleteProject;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorListsAllProjects;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorShowsProjectDetails;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorChangesProjectOwner;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorAssignsUserToProject;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdministratorUnassignsUserFromProject;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(ProjectAdministrationRestController.class)
@ContextConfiguration(classes = { ProjectAdministrationRestController.class, ProjectAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class ProjectAdministrationRestControllerRestDocTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ProjectUpdateWhitelistService mockedProjectUpdateWhiteListService;

    @MockBean
    ProjectCreationService creationService;

    @MockBean
    ProjectAssignOwnerService assignOwnerService;

    @MockBean
    ProjectAssignUserService assignUserService;

    @MockBean
    ProjectDeleteService projectDeleteService;

    @MockBean
    ProjectUnassignUserService unassignUserService;

    @MockBean
    ProjectDetailInformationService detailInformationService;
    
    @MockBean
    ProjectDetailChangeService detailsChangeService;

    @MockBean
    ProjectRepository mockedProjectRepository;

    @MockBean
    CreateProjectInputValidator createProjectInputvalidator;

    @Before
    public void before() {
        when(createProjectInputvalidator.supports(ProjectJsonInput.class)).thenReturn(true);
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorCreatesProject.class)
    public void restdoc_create_project() throws Exception {

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				post(https(PORT_USED).buildAdminCreatesProjectUrl()).
				contentType(MediaType.APPLICATION_JSON_VALUE).
				content("{\"apiVersion\":\"1.0\", \"name\":\"projectId\", \"whiteList\":{\"uris\":[\"192.168.1.1\",\"https://my.special.server.com/myapp1/\"]}}")
				).
		andExpect(status().isCreated()).
		andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorCreatesProject.class),
				requestFields(
						fieldWithPath(ProjectJsonInput.PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
						fieldWithPath(ProjectJsonInput.PROPERTY_WHITELIST+"."+ProjectWhiteList.PROPERTY_URIS).description("All URIS used now for whitelisting. Former parts will be replaced completely!"),
						fieldWithPath(ProjectJsonInput.PROPERTY_NAME).description("Name of the project to create. Is also used as a unique ID!")
						)

				)

				);

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorListsAllProjects.class)
    public void restdoc_list_all_projects() throws Exception {

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(https(PORT_USED).buildAdminListsProjectsUrl()).
        		contentType(MediaType.APPLICATION_JSON_VALUE)).
        		
        			andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorListsAllProjects.class))).
        			andExpect(status().isOk()
        		);

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorDeleteProject.class)
    public void restdoc_delete_project() throws Exception {

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				delete(https(PORT_USED).buildAdminDeletesProject(PROJECT_ID.pathElement()),"projectId1").
				contentType(MediaType.APPLICATION_JSON_VALUE)
				).
		andExpect(status().isOk()).
		andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorDeleteProject.class),
				pathParameters(
							parameterWithName(PROJECT_ID.paramName()).description("The id for project to delete")
						)
				));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorChangesProjectOwner.class)
    public void restdoc_assign_owner2project() throws Exception {

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                post(https(PORT_USED).buildAdminAssignsOwnerToProjectUrl(PROJECT_ID.pathElement(), USER_ID.pathElement()), "projectId1", "userId1").
                contentType(MediaType.APPLICATION_JSON_VALUE)
                ).
        andExpect(status().isOk()).
        andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorChangesProjectOwner.class),
                pathParameters(
                        parameterWithName(PROJECT_ID.paramName()).description("The id for project"),
                        parameterWithName(USER_ID.paramName()).description("The user id of the user to assign to project as the owner")
                        )
                ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorAssignsUserToProject.class)
    public void restdoc_assign_user2project() throws Exception {

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				post(https(PORT_USED).buildAdminAssignsUserToProjectUrl(PROJECT_ID.pathElement(), USER_ID.pathElement()), "projectId1", "userId1").
				contentType(MediaType.APPLICATION_JSON_VALUE)
				).
		andExpect(status().isOk()).
		andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorAssignsUserToProject.class),
				pathParameters(
						parameterWithName(PROJECT_ID.paramName()).description("The id for project"),
						parameterWithName(USER_ID.paramName()).description("The user id of the user to assign to project")
						)
				));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorUnassignsUserFromProject.class)
    public void restdoc_unassign_userFromProject() throws Exception {

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				delete(https(PORT_USED).buildAdminUnassignsUserFromProjectUrl(PROJECT_ID.pathElement(), USER_ID.pathElement()), "projectId1", "userId1").
				contentType(MediaType.APPLICATION_JSON_VALUE)
				).
		andExpect(status().isOk()).
		andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorUnassignsUserFromProject.class),
				pathParameters(
						parameterWithName(PROJECT_ID.paramName()).description("The id for project"),
						parameterWithName(USER_ID.paramName()).description("The user id of the user to unassign from project")
						)
				));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorShowsProjectDetails.class)
    public void restdoc_show_project_details() throws Exception {
        /* prepare */
        Project project = mock(Project.class);
        when(project.getId()).thenReturn("projectId1");

        Set<User> users = new LinkedHashSet<>();
        User user1 = mock(User.class);
        when(user1.getName()).thenReturn("name1");

        User user2 = mock(User.class);
        when(user2.getName()).thenReturn("name2");

        users.add(user1);
        users.add(user2);

        when(project.getUsers()).thenReturn(users);
        when(project.getOwner()).thenReturn(user1);
        Set<URI> whiteList = new LinkedHashSet<>();
        whiteList.add(new URI("http://www.sechub.example.org"));
        when(project.getWhiteList()).thenReturn(whiteList);

        Set<ProjectMetaDataEntity> metaData = new LinkedHashSet<>();
        ProjectMetaDataEntity entry = new ProjectMetaDataEntity("projectId1", "key1", "value1");
        metaData.add(entry);

        when(project.getMetaData()).thenReturn(metaData);
        
        when(project.getDescription()).thenReturn("description");

        ProjectDetailInformation detailInformation = new ProjectDetailInformation(project);

        when(detailInformationService.fetchDetails("projectId1")).thenReturn(detailInformation);

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				get(https(PORT_USED).buildAdminShowsProjectDetailsUrl(PROJECT_ID.pathElement()),"projectId1").
				contentType(MediaType.APPLICATION_JSON_VALUE)
				)./*
				*/
		andDo(print()).
		andExpect(status().isOk()).
		andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorShowsProjectDetails.class),
				pathParameters(
							parameterWithName(PROJECT_ID.paramName()).description("The id for project to show details for")
						),
				responseFields(
							fieldWithPath(ProjectDetailInformation.PROPERTY_PROJECT_ID).description("The name of the project."),
							fieldWithPath(ProjectDetailInformation.PROPERTY_USERS).description("A list of all users having access to the project."),
							fieldWithPath(ProjectDetailInformation.PROPERTY_OWNER).description("Username of the owner ofthis project. An owner is the person in charge."),
							fieldWithPath(ProjectDetailInformation.PROPERTY_WHITELIST).description("A list of all whitelisted URIs. Only these ones can be scanned for the project!"),
							fieldWithPath(ProjectDetailInformation.PROPERTY_METADATA).description("An JSON object containing metadata key-value pairs defined for this project."),
							fieldWithPath(ProjectDetailInformation.PROPERTY_METADATA + ".key1").description("An arbitrary metadata key."),
							fieldWithPath(ProjectDetailInformation.PROPERTY_DESCRIPTION).description("The project description.")
						)
					)
				);

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorChangesProjectDescription.class)
    public void restdoc_change_project_description() throws Exception {
        /* prepare */
        Project project = mock(Project.class);
        when(project.getId()).thenReturn("projectId1");

        Set<User> users = new LinkedHashSet<>();
        User user1 = mock(User.class);
        when(user1.getName()).thenReturn("name1");

        User user2 = mock(User.class);
        when(user2.getName()).thenReturn("name2");

        users.add(user1);
        users.add(user2);

        when(project.getUsers()).thenReturn(users);
        when(project.getOwner()).thenReturn(user1);
        Set<URI> whiteList = new LinkedHashSet<>();
        whiteList.add(new URI("http://www.sechub.example.org"));
        when(project.getWhiteList()).thenReturn(whiteList);

        Set<ProjectMetaDataEntity> metaData = new LinkedHashSet<>();
        ProjectMetaDataEntity entry = new ProjectMetaDataEntity("projectId1", "key1", "value1");
        metaData.add(entry);

        when(project.getMetaData()).thenReturn(metaData);
        
        when(project.getDescription()).thenReturn("description");

        ProjectDetailInformation detailInformation = new ProjectDetailInformation(project);
        
        when(detailsChangeService.changeProjectDescription(any(), any())).thenReturn(detailInformation);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                post(https(PORT_USED).buildAdminChangesProjectDescriptionUrl(PROJECT_ID.pathElement()), "projectId1").
                content("{\n"
                        + "  \"description\" : \"new description\"\n"
                        + "}").
                contentType(MediaType.APPLICATION_JSON_VALUE)
                )./*
                */
        andDo(print()).
        andExpect(status().isOk()).
        andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorChangesProjectDescription.class),
                pathParameters(
                            parameterWithName(PROJECT_ID.paramName()).description("The id for project to change details for")
                        ),
                responseFields(
                            fieldWithPath(ProjectDetailInformation.PROPERTY_PROJECT_ID).description("The name of the project."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_USERS).description("A list of all users having access to the project."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_OWNER).description("Username of the owner ofthis project. An owner is the person in charge."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_WHITELIST).description("A list of all whitelisted URIs. Only these ones can be scanned for the project!"),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_METADATA).description("An JSON object containing metadata key-value pairs defined for this project."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_METADATA + ".key1").description("An arbitrary metadata key."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_DESCRIPTION).description("The project description.")
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
