// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig.*;
import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static org.mockito.ArgumentMatchers.*;
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
import com.daimler.sechub.domain.scan.product.config.CreateProductExecutorConfigService;
import com.daimler.sechub.domain.scan.product.config.DeleteProductExecutorConfigService;
import com.daimler.sechub.domain.scan.product.config.FetchProductExecutorConfigListService;
import com.daimler.sechub.domain.scan.product.config.FetchProductExecutorConfigService;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigList;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigListEntry;
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
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorCreatesExecutorConfiguration;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorDeletesExecutorConfiguration;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorFetchesExecutorConfiguration;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorFetchesExecutorConfigurationList;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorUpdatesExecutorConfig;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;
import com.daimler.sechub.test.executorconfig.TestExecutorSetupJobParam;

@RunWith(SpringRunner.class)
@WebMvcTest(ProductExecutorConfigRestController.class)
@ContextConfiguration(classes = { ProductExecutorConfigRestController.class, ProductExecutorConfigRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class ProductExecutorConfigRestControllerRestDocTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    public static final String CREDENTIALS_USER_DESCRIPTION = "User name, either plain (not recommended) or with env:VARIABLENAME, in last case the user name will be from environment variable ";
    public static final String CREDENTIALS_PWD_DESCRIPTION = "Password, either plain (not recommended) or with env:VARIABLENAME, in last case the password will be from environment variable ";

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
    @UseCaseRestDoc(useCase = UseCaseAdministratorCreatesExecutorConfiguration.class)
    public void restdoc_admin_creates_executor_config() throws Exception {
        /* prepare */
        UUID randomUUID = UUID.randomUUID();

        when(createService.createProductExecutorConfig(any())).thenReturn(randomUUID.toString());

        TestExecutorConfig configFromUser = new TestExecutorConfig();
        configFromUser.enabled = false;
        configFromUser.name = "PDS gosec config 1";
        configFromUser.productIdentifier = ProductIdentifier.PDS_CODESCAN.name();
        configFromUser.executorVersion = 1;
        configFromUser.setup.baseURL = "https://productXYZ.example.com";
        configFromUser.setup.credentials.user = "env:EXAMPLE_USENAME";
        configFromUser.setup.credentials.password = "env:EXAMPLE_PASSWORD";

        TestExecutorSetupJobParam param1 = new TestExecutorSetupJobParam("example.key1", "A value");
        TestExecutorSetupJobParam param2 = new TestExecutorSetupJobParam("example.key2", "Another value");
        configFromUser.setup.jobParameters.add(param1);
        configFromUser.setup.jobParameters.add(param2);

        /* execute + test @formatter:off */
	    this.mockMvc.perform(
	    		post(https(PORT_USED).buildAdminCreatesProductExecutorConfig()).
	    			contentType(MediaType.APPLICATION_JSON_VALUE).
	    			content(JSONConverter.get().toJSON(configFromUser))
	    		).
	    			andExpect(status().isCreated()).
	    			andExpect(content().string(randomUUID.toString())).
	    			andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorCreatesExecutorConfiguration.class),
	    						requestFields(
										fieldWithPath(PROPERTY_NAME).description("A name for this configuration"),
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

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorUpdatesExecutorConfig.class)
    public void restdoc_admin_updates_executor_config() throws Exception {
        /* prepare */
        UUID randomUUID = UUID.randomUUID();

        TestExecutorConfig configFromUser = new TestExecutorConfig();
        configFromUser.enabled = false;
        configFromUser.name = "New name";
        configFromUser.productIdentifier = ProductIdentifier.PDS_CODESCAN.name();
        configFromUser.executorVersion = 1;
        configFromUser.setup.baseURL = "https://productNew.example.com";
        configFromUser.setup.credentials.user = "env:EXAMPLE_NEW_USENAME";
        configFromUser.setup.credentials.password = "env:EXAMPLE_NEW_PASSWORD";

        TestExecutorSetupJobParam param1 = new TestExecutorSetupJobParam("example.key1",
                "A value but changed. Remark: the other parameter (example.key2) has been removed by this call");
        configFromUser.setup.jobParameters.add(param1);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                put(https(PORT_USED).buildAdminUpdatesProductExecutorConfig(UUID_PARAMETER.pathElement()),randomUUID).
                    contentType(MediaType.APPLICATION_JSON_VALUE).
                    content(JSONConverter.get().toJSON(configFromUser))
                ).
                    andExpect(status().isOk()).
                    andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorUpdatesExecutorConfig.class),
                                requestFields(
                                        fieldWithPath(PROPERTY_NAME).description("The name of this configuration"),
                                        fieldWithPath(PROPERTY_PRODUCTIDENTIFIER).description("Executor product identifier"),
                                        fieldWithPath(PROPERTY_EXECUTORVERSION).description("Executor version"),
                                        fieldWithPath(PROPERTY_ENABLED).description("Enabled state of executor, per default false").optional(),
                                        fieldWithPath(PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_BASEURL).description("Base URL to the product"),
                                        fieldWithPath(PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_CREDENTIALS+"."+ProductExecutorConfigSetupCredentials.PROPERTY_USER).description(CREDENTIALS_USER_DESCRIPTION),
                                        fieldWithPath(PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_CREDENTIALS+"."+ProductExecutorConfigSetupCredentials.PROPERTY_PASSWORD).description(CREDENTIALS_PWD_DESCRIPTION),
                                        fieldWithPath(PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_JOBPARAMETERS+"[]."+ProductExecutorConfigSetupJobParameter.PROPERTY_KEY).description(JOBPARAM_KEY_DESCRIPTION).optional(),
                                        fieldWithPath(PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_JOBPARAMETERS+"[]."+ProductExecutorConfigSetupJobParameter.PROPERTY_VALUE).description(JOBPARAM_VALUE_DESCRIPTION).optional()
                                        ),

                                pathParameters(
                                        parameterWithName(UUID_PARAMETER.paramName()).description("The configuration uuid")
                                
                                        )
                                )
                );

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorFetchesExecutorConfiguration.class)
    public void restdoc_admin_fetches_executor_config() throws Exception {
        /* prepare */
        UUID uuid = UUID.randomUUID();

        TestExecutorConfig testConfig = new TestExecutorConfig();
        testConfig.uuid = uuid;
        testConfig.enabled = false;
        testConfig.name = "New name";
        testConfig.productIdentifier = ProductIdentifier.PDS_CODESCAN.name();
        testConfig.executorVersion = 1;
        testConfig.setup.baseURL = "https://product.example.com";
        testConfig.setup.credentials.user = "env:EXAMPLE_USENAME";
        testConfig.setup.credentials.password = "env:EXAMPLE_PASSWORD";

        TestExecutorSetupJobParam param1 = new TestExecutorSetupJobParam("example.key1", "A value");
        testConfig.setup.jobParameters.add(param1);

        ProductExecutorConfig config = JSONConverter.get().fromJSON(ProductExecutorConfig.class, JSONConverter.get().toJSON(testConfig));

        when(fetchService.fetchProductExecutorConfig(uuid)).thenReturn(config);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).buildAdminFetchesProductExecutorConfig(UUID_PARAMETER.pathElement()),uuid).
                    contentType(MediaType.APPLICATION_JSON_VALUE)
                ).
                    andExpect(status().isOk()).
                    andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorFetchesExecutorConfiguration.class),
                                responseFields(
                                        fieldWithPath(PROPERTY_UUID).description("The uuid of this configuration"),
                                        fieldWithPath(PROPERTY_NAME).description("The name of this configuration"),
                                        fieldWithPath(PROPERTY_PRODUCTIDENTIFIER).description("Executor product identifier"),
                                        fieldWithPath(PROPERTY_EXECUTORVERSION).description("Executor version"),
                                        fieldWithPath(PROPERTY_ENABLED).description("Enabled state of executor").optional(),
                                        fieldWithPath(PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_BASEURL).description("Base URL to the product"),
                                        fieldWithPath(PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_CREDENTIALS+"."+ProductExecutorConfigSetupCredentials.PROPERTY_USER).description(CREDENTIALS_USER_DESCRIPTION),
                                        fieldWithPath(PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_CREDENTIALS+"."+ProductExecutorConfigSetupCredentials.PROPERTY_PASSWORD).description(CREDENTIALS_PWD_DESCRIPTION),
                                        fieldWithPath(PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_JOBPARAMETERS+"[]."+ProductExecutorConfigSetupJobParameter.PROPERTY_KEY).description(JOBPARAM_KEY_DESCRIPTION).optional(),
                                        fieldWithPath(PROPERTY_SETUP+"."+ProductExecutorConfigSetup.PROPERTY_JOBPARAMETERS+"[]."+ProductExecutorConfigSetupJobParameter.PROPERTY_VALUE).description(JOBPARAM_VALUE_DESCRIPTION).optional()
                                        ),

                                pathParameters(
                                        parameterWithName(UUID_PARAMETER.paramName()).description("The configuration uuid")
                                
                                        )

                                )
                );

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorDeletesExecutorConfiguration.class)
    public void restDoc_admin_deletes_executor_config() throws Exception {

        /* execute + test @formatter:off */
	    UUID configUUID = UUID.randomUUID();
        this.mockMvc.perform(
                delete(https(PORT_USED).buildAdminDeletesProductExecutorConfig(UUID_PARAMETER.pathElement()),configUUID).
                    contentType(MediaType.APPLICATION_JSON_VALUE)
                ).
                    andExpect(status().isOk()).
                    andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorDeletesExecutorConfiguration.class),
                    

                            pathParameters(
                                    parameterWithName(UUID_PARAMETER.paramName()).description("The configuration uuid")
                            
                                    )
                            )
                );

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdministratorFetchesExecutorConfigurationList.class)
    public void restDoc_admin_fetches_executor_configuration() throws Exception {

        /* prepare */
        UUID uuid = UUID.randomUUID();

        ProductExecutorConfigList list = new ProductExecutorConfigList();
        ProductExecutorConfigListEntry entry = new ProductExecutorConfigListEntry(uuid, "example configuration", true);
        list.getExecutorConfigurations().add(entry);

        when(fetchListService.fetchProductExecutorConfigList()).thenReturn(list);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).buildAdminFetchesListOfProductExecutionConfigurations()).
                    contentType(MediaType.APPLICATION_JSON_VALUE)
                ).
                    andExpect(status().isOk()).
                    andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorFetchesExecutorConfigurationList.class),
                            responseFields(
                                    fieldWithPath("type").description("Always `executorConfigurationList` as an identifier for the list"),
                                    fieldWithPath("executorConfigurations[]."+PROPERTY_UUID).description("The uuid of the configuration"),
                                    fieldWithPath("executorConfigurations[]."+PROPERTY_NAME).description("The configuration name"),
                                    fieldWithPath("executorConfigurations[]."+PROPERTY_ENABLED).description("Enabled state of configuration")
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
