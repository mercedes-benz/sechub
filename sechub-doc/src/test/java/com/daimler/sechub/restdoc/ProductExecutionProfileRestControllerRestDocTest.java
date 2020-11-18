// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.domain.scan.product.config.ProductExecutionProfile.*;
import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.docgen.util.RestDocPathFactory;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.config.CreateProductExecutionProfileService;
import com.daimler.sechub.domain.scan.product.config.DeleteProductExecutionProfileService;
import com.daimler.sechub.domain.scan.product.config.FetchProductExecutionProfileListService;
import com.daimler.sechub.domain.scan.product.config.FetchProductExecutionProfileService;
import com.daimler.sechub.domain.scan.product.config.ProductExecutionProfile;
import com.daimler.sechub.domain.scan.product.config.ProductExecutionProfileRestController;
import com.daimler.sechub.domain.scan.product.config.ProductExecutionProfilesList;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigRepository;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupCredentials;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigValidation;
import com.daimler.sechub.domain.scan.product.config.UpdateProductExecutionProfileService;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorAssignsExecutionProfileToProject;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorCreatesExecutionProfile;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorDeletesExecutionProfile;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorFetchesExecutionProfile;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorFetchesExecutionProfileList;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorUnassignsExecutionProfileFromProject;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorUpdatesExecutionProfile;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;
import com.daimler.sechub.test.executionprofile.TestExecutionProfile;
import com.daimler.sechub.test.executionprofile.TestExecutionProfileList;
import com.daimler.sechub.test.executionprofile.TestExecutionProfileListEntry;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;
import com.daimler.sechub.test.executorconfig.TestExecutorSetupJobParam;
@RunWith(SpringRunner.class)
@WebMvcTest(ProductExecutionProfileRestController.class)
@ContextConfiguration(classes = { ProductExecutionProfileRestController.class,
		ProductExecutionProfileRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({Profiles.TEST, Profiles.ADMIN_ACCESS})
@AutoConfigureRestDocs(uriScheme="https",uriHost=ExampleConstants.URI_SECHUB_SERVER,uriPort=443)
public class ProductExecutionProfileRestControllerRestDocTest {
    
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
	@UseCaseRestDoc(useCase = UseCaseAdministratorCreatesExecutionProfile.class)
	public void restdoc_admin_creates_profile() throws Exception {
		/* prepare */
	    String profileId="new-profile-1";
	    
	    TestExecutionProfile profile = new TestExecutionProfile();
	    profile.description="a short description for profile";
	    profile.enabled=false;

        /* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		post(https(PORT_USED).buildAdminCreatesProductExecutionProfile(PROFILE_ID.pathElement()),profileId).
	    			contentType(MediaType.APPLICATION_JSON_VALUE).
	    			content(JSONConverter.get().toJSON(profile))
	    		).
	    			andExpect(status().isCreated()).
	    			andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorCreatesExecutionProfile.class),
	    						requestFields(
										fieldWithPath(PROPERTY_DESCRIPTION).description("A short description for the profile"),
										fieldWithPath(PROPERTY_ENABLED).description("Enabled state of profile, default is false").optional(),
										fieldWithPath(PROPERTY_CONFIGURATIONS+"[]").description("Configurations can be linked at creation time as well - see update description").optional(),
										fieldWithPath(PROPERTY_PROJECT_IDS+"[]").description("Projects can be linked by their ids at creation time as well - see update description").optional()
										),
	    						 pathParameters(
	    	                            parameterWithName(PROFILE_ID.paramName()).description("The profile id")
	    	                            )

	    						)
	    		);

	    /* @formatter:on */
	}
	
	@Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorUpdatesExecutionProfile.class)
    public void restdoc_admin_updates_profile() throws Exception {
        /* prepare */
	    String profileId="existing-profile-1";
        
        TestExecutionProfile profile = new TestExecutionProfile();
        profile.description="changed description";
        profile.enabled=true;
        UUID randomUUID = UUID.randomUUID();

        TestExecutorConfig configFromUser = new TestExecutorConfig();
        configFromUser.uuid=randomUUID;
        
        profile.configurations.add(configFromUser);
        profile.projectIds=null;
                
        /* execute + test @formatter:off */
        this.mockMvc.perform(
                put(https(PORT_USED).buildAdminUpdatesProductExecutionProfile(PROFILE_ID.pathElement()),profileId).
                    contentType(MediaType.APPLICATION_JSON_VALUE).
                    content(JSONConverter.get().toJSON(profile))
                ).
                    andExpect(status().isOk()).
                    andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorUpdatesExecutionProfile.class),
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
                                )
                );

        /* @formatter:on */
    }
	
	@Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorAssignsExecutionProfileToProject.class)
    public void restdoc_admin_assigns_executionprofile_to_project() throws Exception {
        /* prepare */
        String profileId="profile-1";
        String projectId="project-1";

        
                
        /* execute + test @formatter:off */
        this.mockMvc.perform(
                post(https(PORT_USED).buildAdminAddsProjectToExecutionProfile(PROFILE_ID.pathElement(),PROJECT_ID.pathElement()),profileId,projectId).
                    contentType(MediaType.APPLICATION_JSON_VALUE)
                ).
                    andExpect(status().isCreated()).
                    andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorAssignsExecutionProfileToProject.class),
                            pathParameters(
                               parameterWithName(PROJECT_ID.paramName()).description("The project id "),
                               parameterWithName(PROFILE_ID.paramName()).description("The profile id")
                            )
                    )
                );

        /* @formatter:on */
    }
	
	@Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorUnassignsExecutionProfileFromProject.class)
    public void restdoc_admin_unassigns_executionprofile_from_project() throws Exception {
        /* prepare */
        String profileId="profile-1";
        String projectId="project-1";
                
        /* execute + test @formatter:off */
        this.mockMvc.perform(
                post(https(PORT_USED).buildAdminAddsProjectToExecutionProfile(PROFILE_ID.pathElement(),PROJECT_ID.pathElement()),profileId,projectId).
                    contentType(MediaType.APPLICATION_JSON_VALUE)
                ).
                    andExpect(status().isCreated()).
                    andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorUnassignsExecutionProfileFromProject.class),
                            pathParameters(
                                    parameterWithName(PROJECT_ID.paramName()).description("The project id "),
                                    parameterWithName(PROFILE_ID.paramName()).description("The profile id")
                            )
                    )
                );

        /* @formatter:on */
    }
	
	@Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorFetchesExecutionProfile.class)
    public void restdoc_admin_fetches_profile() throws Exception {
        /* prepare */
	    /* prepare */
        String profileId="existing-profile-1";
        
        TestExecutionProfile testprofile = new TestExecutionProfile();
        testprofile.description="a description";
        testprofile.enabled=true;
        UUID randomUUID = UUID.randomUUID();

        TestExecutorConfig configFromUser = new TestExecutorConfig();
        configFromUser.enabled=false;
        configFromUser.name="New name";
        configFromUser.productIdentifier=ProductIdentifier.PDS_CODESCAN.name();
        configFromUser.executorVersion=1;
        configFromUser.setup.baseURL="https://product.example.com";
        configFromUser.setup.credentials.user="env:EXAMPLE_USENAME";
        configFromUser.setup.credentials.password="env:EXAMPLE_PASSWORD";
        configFromUser.uuid=randomUUID;
        
        TestExecutorSetupJobParam param1 = new TestExecutorSetupJobParam("example.key1", "A value but changed. Remark: the other parameter (example.key2) has been removed by this call");
        configFromUser.setup.jobParameters.add(param1);
        
        testprofile.configurations.add(configFromUser);
        testprofile.projectIds.add("project-1");
        testprofile.projectIds.add("project-2");
        
        ProductExecutionProfile profile = JSONConverter.get().fromJSON(ProductExecutionProfile.class, JSONConverter.get().toJSON(testprofile));
                
        when(fetchService.fetchProductExecutorConfig(profileId)).thenReturn(profile);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).buildAdminFetchesProductExecutionProfile(PROFILE_ID.pathElement()),profileId).
                    contentType(MediaType.APPLICATION_JSON_VALUE)
                ).
                    andExpect(status().isOk()).
                    andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorFetchesExecutionProfile.class),
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

                                )
                );

        /* @formatter:on */
    }
    
	
	@Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorDeletesExecutionProfile.class)
    public void restDoc_admin_deletes_profile() throws Exception {
	    
	    /* execute + test @formatter:off */
	    String profileId= "profile-to-delete-1";
	    this.mockMvc.perform(
                delete(https(PORT_USED).buildAdminDeletesProductExecutionProfile(PROFILE_ID.pathElement()),profileId).
                    contentType(MediaType.APPLICATION_JSON_VALUE)
                ).
                    andExpect(status().isOk()).
                    andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorDeletesExecutionProfile.class),
                            pathParameters(
                                    parameterWithName(PROFILE_ID.paramName()).description("The profile id")
                                    )
)
                );

        /* @formatter:on */
	}
	
	@Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorFetchesExecutionProfileList.class)
    public void restDoc_admin_fetches_profiles_list() throws Exception {
        
	    /* prepare */
        TestExecutionProfileList profileList = new TestExecutionProfileList();
        TestExecutionProfileListEntry entry1 = new TestExecutionProfileListEntry();
        entry1.description="A short decription for profile1";
        entry1.id="profile1";
        
        TestExecutionProfileListEntry entry2 = new TestExecutionProfileListEntry();
        entry2.description="A short decription for profile2";
        entry2.id="profile2";
        
        profileList.executionProfiles.add(entry1);
        profileList.executionProfiles.add(entry2);

        
        ProductExecutionProfilesList list = JSONConverter.get().fromJSON(ProductExecutionProfilesList.class,JSONConverter.get().toJSON(profileList));
        
        when(fetchListService.fetchProductExecutionProfileList()).thenReturn(list);
	    
        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).buildAdminFetchesListOfProductExecutionProfiles()).
                    contentType(MediaType.APPLICATION_JSON_VALUE)
                ).
                    andExpect(status().isOk()).
                    andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorFetchesExecutionProfileList.class),
                            responseFields(
                                    fieldWithPath("type").description("Always `executorProfileList` as an identifier for the list"),
                                    fieldWithPath("executionProfiles[]."+PROPERTY_ID).description("The profile id"),
                                    fieldWithPath("executionProfiles[]."+PROPERTY_DESCRIPTION).description("A profile description"),
                                    fieldWithPath("executionProfiles[]."+PROPERTY_ENABLED).description("Enabled state of profile")
                                    )
                            )
                );

        /* @formatter:on */
    }

	@TestConfiguration
	@Profile(Profiles.TEST)
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

	}
}
