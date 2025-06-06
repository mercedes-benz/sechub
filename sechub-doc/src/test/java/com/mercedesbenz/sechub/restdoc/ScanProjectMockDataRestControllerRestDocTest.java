// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentationTest.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;

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

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.scan.project.ScanMockData;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectMockDataConfiguration;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectMockDataConfigurationService;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectMockDataRestController;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.UseCaseUserDefinesProjectMockdata;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.UseCaseUserRetrievesProjectMockdata;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.RestDocPathParameter;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { ScanProjectMockDataRestController.class })
@Import(TestRestDocSecurityConfiguration.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
@ActiveProfiles({ Profiles.MOCKED_PRODUCTS, Profiles.TEST })
public class ScanProjectMockDataRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final String PROJECT1_ID = "project1";

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScanProjectMockDataConfigurationService configService;

    @UseCaseRestDoc(useCase = UseCaseUserDefinesProjectMockdata.class)
    @Test
    @WithMockUser
    public void set_project_mock_configuration() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildSetProjectMockConfiguration(RestDocPathParameter.PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserDefinesProjectMockdata.class;

        ScanProjectMockDataConfiguration config = new ScanProjectMockDataConfiguration();
        config.setCodeScan(new ScanMockData(TrafficLight.RED));
        config.setWebScan(new ScanMockData(TrafficLight.YELLOW));
        config.setInfraScan(new ScanMockData(TrafficLight.GREEN));

        /* @formatter:off */
		/* execute + test */
	    this.mockMvc.perform(
	    		put(apiEndpoint,PROJECT1_ID).
	    			accept(MediaType.APPLICATION_JSON_VALUE).
	    			contentType(MediaType.APPLICATION_JSON_VALUE).
	    			content(config.toJSON()).
	    			header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
	    		).
	    			andExpect(status().isOk()).
	    			andDo(defineRestService().
	    	                with().
	    	                    useCaseData(useCase).
	    	                    tag(RestDocFactory.extractTag(apiEndpoint)).
	    	                    requestSchema(TestOpenApiSchema.MOCK_DATA_CONFIGURATION.getSchema()).
	    	                and().
	    	                document(
	                            		requestHeaders(

	                            		)
	    			));
	    /* @formatter:on */
    }

    @UseCaseRestDoc(useCase = UseCaseUserRetrievesProjectMockdata.class)
    @Test
    @WithMockUser
    public void get_project_mock_configuration() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildGetProjectMockConfiguration(RestDocPathParameter.PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserRetrievesProjectMockdata.class;

        ScanProjectMockDataConfiguration config = new ScanProjectMockDataConfiguration();
        config.setCodeScan(new ScanMockData(TrafficLight.RED));
        config.setWebScan(new ScanMockData(TrafficLight.YELLOW));
        config.setInfraScan(new ScanMockData(TrafficLight.GREEN));

        when(configService.retrieveProjectMockDataConfiguration(PROJECT1_ID)).thenReturn(config);

        /* @formatter:off */
		/* execute + test @formatter:off */
        this.mockMvc.perform(
        		get(apiEndpoint, PROJECT1_ID).
        			accept(MediaType.APPLICATION_JSON_VALUE).
        			contentType(MediaType.APPLICATION_JSON_VALUE).
        			header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
        		).
        			andExpect(status().isOk()).
        			andExpect(jsonPath("$.codeScan.result").value("RED")).
        			andExpect(jsonPath("$.webScan.result").value("YELLOW")).
        			andExpect(jsonPath("$.infraScan.result").value("GREEN")).

        			andDo(defineRestService().
                            with().
                                useCaseData(useCase).
                                tag(RestDocFactory.extractTag(apiEndpoint)).
                                responseSchema(TestOpenApiSchema.MOCK_DATA_CONFIGURATION.getSchema()).
                            and().
                            document(
	                            		requestHeaders(

	                            		)
                            		)
                    );

        /* @formatter:on */
    }

    @Before
    public void before() throws Exception {
    }

}
