// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.daimler.sechub.docgen.util.RestDocFactory;
import com.daimler.sechub.domain.administration.project.CreateProjectInputValidator;
import com.daimler.sechub.domain.administration.project.ListProjectsService;
import com.daimler.sechub.domain.administration.project.Project;
import com.daimler.sechub.domain.administration.project.ProjectAdministrationRestController;
import com.daimler.sechub.domain.administration.project.ProjectAssignUserService;
import com.daimler.sechub.domain.administration.project.ProjectChangeAccessLevelService;
import com.daimler.sechub.domain.administration.project.ProjectChangeOwnerService;
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
import com.daimler.sechub.server.SecHubWebMvcConfigurer;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.project.ProjectAccessLevel;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdminChangesProjectDescription;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdminCreatesProject;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdminDeleteProject;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdminListsAllProjects;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdminShowsProjectDetails;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorChangesProjectAccessLevel;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdminAssignsUserToProject;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdminChangesProjectOwner;
import com.daimler.sechub.sharedkernel.usecases.admin.user.UseCaseAdminUnassignsUserFromProject;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;
import com.epages.restdocs.apispec.ResourceSnippetParameters;

@RunWith(SpringRunner.class)
@WebMvcTest(ProjectAdministrationRestController.class)
@ContextConfiguration(classes = { ProjectAdministrationRestController.class, ProjectAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class,
        SecHubWebMvcConfigurer.class })
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
    ProjectChangeOwnerService assignOwnerService;

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

    @MockBean
    ListProjectsService listProjectsService;

    @MockBean
    ProjectChangeAccessLevelService projectChangeAccessLevelService;

    @Before
    public void before() {
        when(createProjectInputvalidator.supports(ProjectJsonInput.class)).thenReturn(true);
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminCreatesProject.class)
    public void restdoc_create_project() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminCreatesProjectUrl();
        Class<? extends Annotation> useCase = UseCaseAdminCreatesProject.class;

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				post(apiEndpoint).
				contentType(MediaType.APPLICATION_JSON_VALUE).
				content("{\"apiVersion\":\"1.0\", "
				        + "\"name\":\"projectId\", "
				        + "\"description\":\"A description of the project.\", "
				        + "\"owner\":\"ownerName1\", "
				        + "\"whiteList\":{\"uris\":[\"192.168.1.1\",\"https://my.special.server.com/myapp1/\"]}, "
				        + "\"metaData\":{\"key1\":\"value1\", \"key2\":\"value2\"}}")
				).
		andExpect(status().isCreated()).
		andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            requestSchema(OpenApiSchema.PROJECT.getSchema()).
                            requestFields(
                                    fieldWithPath(ProjectJsonInput.PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
                                    fieldWithPath(ProjectJsonInput.PROPERTY_NAME).description("Name of the project to create. Is also used as a unique ID!"),
                                    fieldWithPath(ProjectJsonInput.PROPERTY_DESCRIPTION).description("The description of the project.").optional(),
                                    fieldWithPath(ProjectJsonInput.PROPERTY_OWNER).description("Username of the owner of this project. An owner is the person in charge"),
                                    fieldWithPath(ProjectJsonInput.PROPERTY_WHITELIST+"."+ProjectWhiteList.PROPERTY_URIS).description("All URIs used now for whitelisting. Former parts will be replaced completely!"),
                                    fieldWithPath(ProjectJsonInput.PROPERTY_METADATA).description("An JSON object containing metadata key-value pairs defined for this project").optional(),
                                    fieldWithPath(ProjectJsonInput.PROPERTY_METADATA + ".*").description("An arbitrary metadata key-value pair").optional()
                            ).
                            build()
                         )
				));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminListsAllProjects.class)
    public void restdoc_list_all_projects() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminListsProjectsUrl();
        Class<? extends Annotation> useCase = UseCaseAdminListsAllProjects.class;

        List<String> ids = new LinkedList<>();
        ids.add("project1");
        ids.add("project2");

        when(listProjectsService.listProjects()).thenReturn(ids);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(apiEndpoint).
        		contentType(MediaType.APPLICATION_JSON_VALUE)).
                    andExpect(status().isOk()).
        			andDo(document(RestDocFactory.createPath(useCase),
        	                resource(
        	                        ResourceSnippetParameters.builder().
        	                            summary(RestDocFactory.createSummary(useCase)).
        	                            description(RestDocFactory.createDescription(useCase)).
        	                            tag(RestDocFactory.extractTag(apiEndpoint)).
        	                            responseSchema(OpenApiSchema.PROJECT_LIST.getSchema()).
        	                            responseFields(
        	                                    fieldWithPath("[]").description("List of project Ids").optional()
        	                            ).
        	                            build()
        	                         )
        			        ));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminDeleteProject.class)
    public void restdoc_delete_project() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminDeletesProject(PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminDeleteProject.class;

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				delete(apiEndpoint,"projectId1").
				contentType(MediaType.APPLICATION_JSON_VALUE)
				).
		andExpect(status().isOk()).
		andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            pathParameters(
                                    parameterWithName(PROJECT_ID.paramName()).description("The id for project to delete")
                            ).
                            build()
                         )
				));
		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminChangesProjectOwner.class)
    public void restdoc_change_project_owner() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminChangesProjectOwnerUrl(USER_ID.pathElement(), PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminChangesProjectOwner.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                post(apiEndpoint, "projectId1", "userId1").
                contentType(MediaType.APPLICATION_JSON_VALUE)
                ).
        andExpect(status().isOk()).
        andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            pathParameters(
                                    parameterWithName(PROJECT_ID.paramName()).description("The id for project"),
                                    parameterWithName(USER_ID.paramName()).description("The user id of the user to assign to project as the owner")
                            ).
                            build()
                         )
                ));
        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorChangesProjectAccessLevel.class)
    public void restdoc_change_project_access_level() throws Exception {

        /* prepare */
        StringBuilder acceptedValues = new StringBuilder();
        acceptedValues.append("Accepted values: ");
        for (Iterator<ProjectAccessLevel> it = Arrays.asList(ProjectAccessLevel.values()).iterator(); it.hasNext();) {
            ProjectAccessLevel level = it.next();
            acceptedValues.append(level.getId());
            String description = level.getDescription();
            if (description != null) {
                acceptedValues.append("(");
                acceptedValues.append(description);
                acceptedValues.append(")");
            }
            if (it.hasNext()) {
                acceptedValues.append(", ");
            }
        }

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                post(https(PORT_USED).buildAdminChangesProjectAccessLevelUrl(PROJECT_ID.pathElement(), PROJECT_ACCESS_LEVEL.pathElement()), "projectId1", ProjectAccessLevel.READ_ONLY.getId()).
                contentType(MediaType.APPLICATION_JSON_VALUE)
                ).
        andExpect(status().isOk()).
        andDo(document(RestDocFactory.createPath(UseCaseAdministratorChangesProjectAccessLevel.class),
                pathParameters(
                        parameterWithName(PROJECT_ID.paramName()).description("The id for project"),
                        parameterWithName(PROJECT_ACCESS_LEVEL.paramName()).description("The new project access level. "+acceptedValues.toString())
                        )
                ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminAssignsUserToProject.class)
    public void restdoc_assign_user2project() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminAssignsUserToProjectUrl(USER_ID.pathElement(), PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminAssignsUserToProject.class;

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				post(apiEndpoint, "projectId1", "userId1").
				contentType(MediaType.APPLICATION_JSON_VALUE)
				).
		andExpect(status().isOk()).
		andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            pathParameters(
                                    parameterWithName(PROJECT_ID.paramName()).description("The id for project"),
                                    parameterWithName(USER_ID.paramName()).description("The user id of the user to assign to project")
                            ).
                            build()
                         )
				));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminUnassignsUserFromProject.class)
    public void restdoc_unassign_userFromProject() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminUnassignsUserFromProjectUrl(USER_ID.pathElement(), PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminUnassignsUserFromProject.class;

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				delete(apiEndpoint,"userId1", "projectId1").
				contentType(MediaType.APPLICATION_JSON_VALUE)
				).
		andExpect(status().isOk()).
		andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            pathParameters(
                                    parameterWithName(PROJECT_ID.paramName()).description("The id for project"),
                                    parameterWithName(USER_ID.paramName()).description("The user id of the user to unassign from project")
                            ).
                            build()
                         )
				));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminShowsProjectDetails.class)
    public void restdoc_show_project_details() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminShowsProjectDetailsUrl(PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminShowsProjectDetails.class;

        Project project = mock(Project.class);
        when(project.getId()).thenReturn("projectId1");
        when(project.getAccessLevel()).thenReturn(ProjectAccessLevel.FULL);

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
        whiteList.add(new URI("http://www.sechub.example.com"));
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
				get(apiEndpoint,"projectId1").
				contentType(MediaType.APPLICATION_JSON_VALUE)
		).
		andDo(print()).
		andExpect(status().isOk()).
		andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            responseSchema(OpenApiSchema.PROJECT_DETAILS.getSchema()).
				pathParameters(
							parameterWithName(PROJECT_ID.paramName()).description("The id for project to show details for")
                            ).
				responseFields(
                            fieldWithPath(ProjectDetailInformation.PROPERTY_PROJECT_ID).description("The name of the project"),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_USERS).description("A list of all users having access to the project"),
							fieldWithPath(ProjectDetailInformation.PROPERTY_OWNER).description("Username of the owner of this project. An owner is the person in charge."),
							fieldWithPath(ProjectDetailInformation.PROPERTY_WHITELIST).description("A list of all whitelisted URIs. Only these ones can be scanned for the project!"),
							fieldWithPath(ProjectDetailInformation.PROPERTY_METADATA).description("An JSON object containing metadata key-value pairs defined for this project."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_METADATA + ".key1").description("An arbitrary metadata key"),
							fieldWithPath(ProjectDetailInformation.PROPERTY_ACCESSLEVEL).description("The project access level"),
							fieldWithPath(ProjectDetailInformation.PROPERTY_DESCRIPTION).description("The project description.")
                            ).
                            build()
						)
				));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminChangesProjectDescription.class)
    public void restdoc_change_project_description() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminChangesProjectDescriptionUrl(PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminChangesProjectDescription.class;

        Project project = mock(Project.class);
        when(project.getId()).thenReturn("projectId1");
        when(project.getAccessLevel()).thenReturn(ProjectAccessLevel.FULL);

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
                post(apiEndpoint, "projectId1").
                content("{\n"
                        + "  \"description\" : \"new description\"\n"
                        + "}").
                contentType(MediaType.APPLICATION_JSON_VALUE)
                )./*
                */
        andDo(print()).
        andExpect(status().isOk()).
        andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            responseSchema(OpenApiSchema.PROJECT_DETAILS.getSchema()).
                pathParameters(
                            parameterWithName(PROJECT_ID.paramName()).description("The id for project to change details for")
                            ).
                responseFields(
                            fieldWithPath(ProjectDetailInformation.PROPERTY_PROJECT_ID).description("The name of the project."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_USERS).description("A list of all users having access to the project."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_OWNER).description("Username of the owner of this project. An owner is the person in charge."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_WHITELIST).description("A list of all whitelisted URIs. Only these ones can be scanned for the project!"),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_METADATA).description("An JSON object containing metadata key-value pairs defined for this project."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_METADATA + ".key1").description("An arbitrary metadata key."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_ACCESSLEVEL).description("The project access level"),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_DESCRIPTION).description("The project description.")
                            ).
                            build()
                        )
                ));

        /* @formatter:on */
    }

    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

    }

}
