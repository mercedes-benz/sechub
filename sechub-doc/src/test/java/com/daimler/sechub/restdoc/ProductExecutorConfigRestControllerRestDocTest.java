// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig.*;
import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
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
import com.daimler.sechub.domain.scan.product.config.CreateProductExecutorConfigService;
import com.daimler.sechub.domain.scan.product.config.DeleteProductExecutorConfigService;
import com.daimler.sechub.domain.scan.product.config.FetchProductExecutorConfigListService;
import com.daimler.sechub.domain.scan.product.config.FetchProductExecutorConfigService;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigRepository;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigRestController;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupCredentials;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigValidation;
import com.daimler.sechub.domain.scan.product.config.UpdateProductExecutorConfigService;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorAddsExecutorConfiguration;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;
import com.daimler.sechub.test.executorconfig.TestExecutorSetupJobParam;

@RunWith(SpringRunner.class)
@WebMvcTest(ProductExecutorConfigRestController.class)
@ContextConfiguration(classes = { ProductExecutorConfigRestController.class,
		ProductExecutorConfigRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({Profiles.TEST, Profiles.ADMIN_ACCESS})
@AutoConfigureRestDocs(uriScheme="https",uriHost=ExampleConstants.URI_SECHUB_SERVER,uriPort=443)
public class ProductExecutorConfigRestControllerRestDocTest {
    
	private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();
	
	public static final String CREDENTIALS_USER_DESCRIPTION="User name, either plain (not recommended) or with env:VARIABLENAME, in last case the user name will be from environment variable ";
	public static final String CREDENTIALS_PWD_DESCRIPTION="Password, either plain (not recommended) or with env:VARIABLENAME, in last case the password will be from environment variable ";

    private static final String JOBPARAM_KEY_DESCRIPTION = "Job parameter key";
    private static final Object JOBPARAM_VALUE_DESCRIPTION = "Job parameter value";

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private ProductExecutorConfigRepository repo;

	@MockBean
	ProductExecutorConfigValidation validation;
	   
	@MockBean
	CreateProductExecutorConfigService createService;
	
	@MockBean
	DeleteProductExecutorConfigService deleteService;
	
	@MockBean
    FetchProductExecutorConfigListService fetchListService;

	@MockBean
    UpdateProductExecutorConfigService updateService;

	@MockBean
    FetchProductExecutorConfigService fetchService;
	
	@MockBean
	AuditLogService auditLogService;

	@Test
	@UseCaseRestDoc(useCase = UseCaseAdministratorAddsExecutorConfiguration.class)
	public void restdoc_admin_adds_executor_config() throws Exception {
		/* prepare */
		UUID randomUUID = UUID.randomUUID();

		when(createService.createProductExecutorConfig(any())).thenReturn(randomUUID.toString());

		TestExecutorConfig configFromUser = new TestExecutorConfig();
		configFromUser.enabled=false;
		configFromUser.name="PDS gosec config 1";
		configFromUser.productIdentifier=ProductIdentifier.PDS_CODESCAN.name();
		configFromUser.executorVersion=1;
		configFromUser.setup.baseURL="https://productXYZ.example.com";
		configFromUser.setup.credentials.user="env:EXAMPLE_USENAME";
		configFromUser.setup.credentials.password="env:EXAMPLE_PASSWORD";
		
		TestExecutorSetupJobParam param1 = new TestExecutorSetupJobParam("example.key1", "example.value1");
        configFromUser.setup.jobParameters.add(param1);
		        
		/* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		post(https(PORT_USED).buildAdminCreatesProductExecutorConfig()).
	    			contentType(MediaType.APPLICATION_JSON_VALUE).
	    			content(JSONConverter.get().toJSON(configFromUser))
	    		)./*andDo(print()).*/
	    			andExpect(status().isCreated()).
	    			andExpect(content().string(randomUUID.toString())).
	    			andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorAddsExecutorConfiguration.class),
	    						requestFields(
										fieldWithPath(PROPERTY_NAME).description("The uuid of this configuration, generated by system"),
										fieldWithPath(PROPERTY_PRODUCTIDENTIFIER).description("Executor product identifier"),
										fieldWithPath(PROPERTY_EXECUTORVERSION).description("Executor version"),
										fieldWithPath(PROPERTY_ENABLED).description("Enabled state of executor, per default false").optional(),
										fieldWithPath(PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_BASEURL).description("Base URL to the product"),
										fieldWithPath(PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_CREDENTIALS+"."+ProductExecutorConfigSetupCredentials.PROPERTY_USER).description(CREDENTIALS_USER_DESCRIPTION),
										fieldWithPath(PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_CREDENTIALS+"."+ProductExecutorConfigSetupCredentials.PROPERTY_PASSWORD).description(CREDENTIALS_PWD_DESCRIPTION),
										fieldWithPath(PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_JOBPARAMETERS+"[]."+ProductExecutorConfigSetupJobParameter.PROPERTY_KEY).description(JOBPARAM_KEY_DESCRIPTION).optional(),
										fieldWithPath(PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_JOBPARAMETERS+"[]."+ProductExecutorConfigSetupJobParameter.PROPERTY_VALUE).description(JOBPARAM_VALUE_DESCRIPTION).optional()
										)

	    						)
	    		);

	    /* @formatter:on */
	}
//	@Test
//    @UseCaseRestDoc(useCase = UseCaseAdministratorRemovesExecutorConfiguration.class)
//    public void restDoc_admin_removes_executor_config() throws Exception {
//	    
//	}

	@TestConfiguration
	@Profile(Profiles.TEST)
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

	}
}
