// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentationTest.*;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
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
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.administration.project.CreateProjectInputValidator;
import com.mercedesbenz.sechub.domain.administration.project.ListProjectsService;
import com.mercedesbenz.sechub.domain.administration.project.Project;
import com.mercedesbenz.sechub.domain.administration.project.ProjectAdministrationRestController;
import com.mercedesbenz.sechub.domain.administration.project.ProjectAssignUserService;
import com.mercedesbenz.sechub.domain.administration.project.ProjectChangeAccessLevelService;
import com.mercedesbenz.sechub.domain.administration.project.ProjectChangeOwnerService;
import com.mercedesbenz.sechub.domain.administration.project.ProjectCreationService;
import com.mercedesbenz.sechub.domain.administration.project.ProjectDeleteService;
import com.mercedesbenz.sechub.domain.administration.project.ProjectDetailChangeService;
import com.mercedesbenz.sechub.domain.administration.project.ProjectDetailInformation;
import com.mercedesbenz.sechub.domain.administration.project.ProjectDetailInformationService;
import com.mercedesbenz.sechub.domain.administration.project.ProjectJsonInput;
import com.mercedesbenz.sechub.domain.administration.project.ProjectJsonInput.ProjectWhiteList;
import com.mercedesbenz.sechub.domain.administration.project.ProjectMetaDataEntity;
import com.mercedesbenz.sechub.domain.administration.project.ProjectRepository;
import com.mercedesbenz.sechub.domain.administration.project.ProjectTemplateService;
import com.mercedesbenz.sechub.domain.administration.project.ProjectUnassignUserService;
import com.mercedesbenz.sechub.domain.administration.project.ProjectUpdateWhitelistService;
import com.mercedesbenz.sechub.domain.administration.user.User;
import com.mercedesbenz.sechub.server.SecHubWebMvcConfigurer;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminAssignsTemplateToProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUnassignsTemplateFromProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminChangesProjectAccessLevel;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminChangesProjectDescription;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminCreatesProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminDeleteProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminListsAllProjects;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminShowsProjectDetails;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { ProjectAdministrationRestController.class, SecHubWebMvcConfigurer.class })
@Import(TestRestDocSecurityConfiguration.class)
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class ProjectAdministrationRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ProjectUpdateWhitelistService mockedProjectUpdateWhiteListService;

    @MockBean
    ProjectCreationService creationService;

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

    @MockBean
    ProjectTemplateService projectTemplateService;

    @MockBean
    ProjectChangeOwnerService assignOwnerService;

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
		mockMvc.perform(
				post(apiEndpoint).
					header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue()).
					contentType(MediaType.APPLICATION_JSON_VALUE).
					content("{\"apiVersion\":\"1.0\", "
					        + "\"name\":\"projectId\", "
					        + "\"description\":\"A description of the project.\", "
					        + "\"owner\":\"ownerName1\", "
					        + "\"whiteList\":{\"uris\":[\"192.168.1.1\",\"https://my.special.server.com/myapp1/\"]}, "
					        + "\"metaData\":{\"key1\":\"value1\", \"key2\":\"value2\"}}")
				).
		andExpect(status().isCreated()).
		andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    requestSchema(TestOpenApiSchema.PROJECT.getSchema()).
                and().
                document(
	                		requestHeaders(

	                		),
                            requestFields(
                                    fieldWithPath(ProjectJsonInput.PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
                                    fieldWithPath(ProjectJsonInput.PROPERTY_NAME).description("Name of the project to create. Is also used as a unique ID!"),
                                    fieldWithPath(ProjectJsonInput.PROPERTY_DESCRIPTION).description("The description of the project.").optional(),
                                    fieldWithPath(ProjectJsonInput.PROPERTY_OWNER).description("Username of the owner of this project. An owner is the person in charge"),
                                    fieldWithPath(ProjectJsonInput.PROPERTY_WHITELIST+"."+ProjectWhiteList.PROPERTY_URIS).description("All URIs used now for whitelisting. Former parts will be replaced completely!"),
                                    fieldWithPath(ProjectJsonInput.PROPERTY_METADATA).description("An JSON object containing metadata key-value pairs defined for this project").optional(),
                                    fieldWithPath(ProjectJsonInput.PROPERTY_METADATA + ".*").description("An arbitrary metadata key-value pair").optional()
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
		mockMvc
				.perform(
						get(apiEndpoint).
							header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue()).
							contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andDo(defineRestService().
						with().
							useCaseData(useCase).
							tag(RestDocFactory.extractTag(apiEndpoint)).
							responseSchema(TestOpenApiSchema.PROJECT_LIST.getSchema()).
						and().
						document(
								requestHeaders(

								),
								responseFields(
										fieldWithPath("[]").description("List of project Ids").optional()
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
		mockMvc.perform(
				delete(apiEndpoint,"projectId1").
					contentType(MediaType.APPLICATION_JSON_VALUE).
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
                                    parameterWithName(PROJECT_ID.paramName()).description("The project id to delete")
                            )
				));
		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminChangesProjectAccessLevel.class)
    public void restdoc_change_project_access_level() throws Exception {

        Class<UseCaseAdminChangesProjectAccessLevel> useCase = UseCaseAdminChangesProjectAccessLevel.class;
        String apiEndpoint = https(PORT_USED).buildAdminChangesProjectAccessLevelUrl(PROJECT_ID.pathElement(), PROJECT_ACCESS_LEVEL.pathElement());

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
        mockMvc.perform(
                post(apiEndpoint, "projectId1", ProjectAccessLevel.READ_ONLY.getId()).
	                contentType(MediaType.APPLICATION_JSON_VALUE).
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
		                        parameterWithName(PROJECT_ID.paramName()).description("The project id"),
		                        parameterWithName(PROJECT_ACCESS_LEVEL.paramName()).description("The new project access level. "+acceptedValues)
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
		mockMvc.perform(
				get(apiEndpoint,"projectId1").
					contentType(MediaType.APPLICATION_JSON_VALUE).
					header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
		).
		andDo(print()).
		andExpect(status().isOk()).
		andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    responseSchema(TestOpenApiSchema.PROJECT_DETAILS.getSchema()).
                and().
                document(
                		requestHeaders(

                		),
                        pathParameters(
							parameterWithName(PROJECT_ID.paramName()).description("The project id to show details for")
                        ),
                        responseFields(
                            fieldWithPath(ProjectDetailInformation.PROPERTY_PROJECT_ID).description("The name of the project"),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_USERS).description("A list of all users having access to the project"),
							fieldWithPath(ProjectDetailInformation.PROPERTY_OWNER).description("Username of the owner of this project. An owner is the person in charge."),
							fieldWithPath(ProjectDetailInformation.PROPERTY_WHITELIST).description("A list of all whitelisted URIs. Only these ones can be scanned for the project!"),
							fieldWithPath(ProjectDetailInformation.PROPERTY_TEMPLATE_IDS).description("A list of all templates assigned to the project"),
							fieldWithPath(ProjectDetailInformation.PROPERTY_METADATA).description("An JSON object containing metadata key-value pairs defined for this project."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_METADATA + ".key1").description("An arbitrary metadata key"),
							fieldWithPath(ProjectDetailInformation.PROPERTY_ACCESSLEVEL).description("The project access level"),
							fieldWithPath(ProjectDetailInformation.PROPERTY_DESCRIPTION).description("The project description.")
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
        mockMvc.perform(
                post(apiEndpoint, "projectId1").
	                header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue()).
	                content("{\n"
	                        + "  \"description\" : \"new description\"\n"
	                        + "}").
	                contentType(MediaType.APPLICATION_JSON_VALUE)
                )./*
                */
        andDo(print()).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    responseSchema(TestOpenApiSchema.PROJECT_DETAILS.getSchema()).
                and().
                document(
                		requestHeaders(

                		),
                        pathParameters(
                            parameterWithName(PROJECT_ID.paramName()).description("The project id to change details for")
                        ),
                        responseFields(
                            fieldWithPath(ProjectDetailInformation.PROPERTY_PROJECT_ID).description("The name of the project."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_USERS).description("A list of all users having access to the project."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_OWNER).description("Username of the owner of this project. An owner is the person in charge."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_WHITELIST).description("A list of all whitelisted URIs. Only these ones can be scanned for the project!"),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_TEMPLATE_IDS).description("A list of all templates assigned to the project"),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_METADATA).description("An JSON object containing metadata key-value pairs defined for this project."),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_METADATA + ".key1").description("An arbitrary metadata key"),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_ACCESSLEVEL).description("The project access level"),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_ACCESSLEVEL).description("The project access level"),
                            fieldWithPath(ProjectDetailInformation.PROPERTY_DESCRIPTION).description("The project description.")
                        )
                ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminAssignsTemplateToProject.class)
    public void restdoc_assign_template2project() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminAssignsTemplateToProjectUrl(TEMPLATE_ID.pathElement(), PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminAssignsTemplateToProject.class;

        /* execute + test @formatter:off */
        mockMvc.perform(
                put(apiEndpoint, "projectId1", "template1").
                    contentType(MediaType.APPLICATION_JSON_VALUE).
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
                                    parameterWithName(PROJECT_ID.paramName()).description("The project id"),
                                    parameterWithName(TEMPLATE_ID.paramName()).description("The id of the template to assign to project")
                            )
                ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminUnassignsTemplateFromProject.class)
    public void restdoc_unassign_templateFromproject() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminUnAssignsTemplateToProjectUrl(TEMPLATE_ID.pathElement(), PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminUnassignsTemplateFromProject.class;

        /* execute + test @formatter:off */
        mockMvc.perform(
                delete(apiEndpoint, "projectId1", "template1").
                contentType(MediaType.APPLICATION_JSON_VALUE).
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
                                parameterWithName(PROJECT_ID.paramName()).description("The project id"),
                                parameterWithName(TEMPLATE_ID.paramName()).description("The id of the template to unassign from project")
                                )
                        ));

        /* @formatter:on */
    }

}
