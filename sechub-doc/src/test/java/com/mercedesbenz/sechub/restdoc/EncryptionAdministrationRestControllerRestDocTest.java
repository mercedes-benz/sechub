// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.docgen.util.RestDocFactory.*;
import static com.mercedesbenz.sechub.restdoc.RestDocumentationTest.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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

import com.mercedesbenz.sechub.commons.model.job.ExecutionState;
import com.mercedesbenz.sechub.domain.administration.encryption.AdministrationEncryptionRotationService;
import com.mercedesbenz.sechub.domain.administration.encryption.AdministrationEncryptionStatusService;
import com.mercedesbenz.sechub.domain.administration.encryption.EncryptionAdministrationRestController;
import com.mercedesbenz.sechub.domain.administration.job.JobAdministrationRestController;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherAlgorithm;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherPasswordSourceType;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubDomainEncryptionData;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubDomainEncryptionStatus;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionData;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionDataValidator;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionStatus;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubPasswordSource;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseAdminFetchesEncryptionStatus;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseAdminStartsEncryptionRotation;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(JobAdministrationRestController.class)
@ContextConfiguration(classes = { EncryptionAdministrationRestController.class, SecHubEncryptionDataValidator.class })
@Import(TestRestDocSecurityConfiguration.class)
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@EnableAutoConfiguration
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class EncryptionAdministrationRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AdministrationEncryptionRotationService encryptionRotationService;

    @MockBean
    AdministrationEncryptionStatusService encryptionStatusService;

    @Before
    public void before() {

    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminStartsEncryptionRotation.class)
    public void restdoc_admin_starts_encryption_rotation() throws Exception {

        /* prepare */

        SecHubEncryptionData data = new SecHubEncryptionData();
        data.setAlgorithm(SecHubCipherAlgorithm.AES_GCM_SIV_256);
        data.setPasswordSourceType(SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE);
        data.setPasswordSourceData("SECRET_1");

        String apiEndpoint = https(PORT_USED).buildAdminStartsEncryptionRotation();
        Class<? extends Annotation> useCase = UseCaseAdminStartsEncryptionRotation.class;

        /* execute + test @formatter:off */

        this.mockMvc.perform(
                        post(apiEndpoint).
                                contentType(MediaType.APPLICATION_JSON_VALUE).
                                content(data.toFormattedJSON()).
                                header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
                ).
                andExpect(status().isOk()).
                andDo(defineRestService().
                        with().
                        useCaseData(useCase).
                        tag(extractTag(apiEndpoint)).
                        and().
                        document(
                                requestHeaders(

                                )
                        ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminFetchesEncryptionStatus.class)
    public void restdoc_admin_fetches_encryption_status() throws Exception {

        /* prepare */

        SecHubEncryptionStatus status = createEncryptionStatusExample();

        when(encryptionStatusService.fetchStatus()).thenReturn(status);

        String apiEndpoint = https(PORT_USED).buildAdminFetchesEncryptionStatus();
        Class<? extends Annotation> useCase = UseCaseAdminFetchesEncryptionStatus.class;

        /* execute + test @formatter:off */
        String domains = SecHubEncryptionStatus.PROPERTY_DOMAINS+"[].";
        String domainData = domains+SecHubDomainEncryptionStatus.PROPERTY_DATA+"[].";

        this.mockMvc.perform(
                        get(apiEndpoint).
                                contentType(MediaType.APPLICATION_JSON_VALUE).
                                header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
                ).
                andExpect(status().isOk()).
                andDo(defineRestService().
                        with().
                        useCaseData(useCase).
                        tag(extractTag(apiEndpoint)).
                        responseSchema(TestOpenApiSchema.ENCRYPTION_STATUS.getSchema()).
                        and().
                        document(
                                requestHeaders(

                                ),
                                responseFields(
                                        fieldWithPath(SecHubEncryptionStatus.PROPERTY_TYPE).description("The type description of the json content"),
                                        fieldWithPath(domains+SecHubDomainEncryptionStatus.PROPERTY_NAME).description("Name of the domain which will provide this encryption data elements"),
                                        fieldWithPath(domainData+SecHubDomainEncryptionData.PROPERTY_ID).description("Unique identifier"),
                                        fieldWithPath(domainData+SecHubDomainEncryptionData.PROPERTY_ALGORITHM).description("Algorithm used for encryption"),
                                        fieldWithPath(domainData+SecHubDomainEncryptionData.PROPERTY_PASSWORDSOURCE+"."+ SecHubPasswordSource.PROPERTY_TYPE).description("Type of password source. Can be "+List.of(SecHubCipherPasswordSourceType.values())),
                                        fieldWithPath(domainData+SecHubDomainEncryptionData.PROPERTY_PASSWORDSOURCE+"."+ SecHubPasswordSource.PROPERTY_DATA).description("Data for password source. If type is "+SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE+" then it is the the name of the environment variable."),
                                        fieldWithPath(domainData+SecHubDomainEncryptionData.PROPERTY_USAGE).description("Map containing information about usage of this encryption"),
                                        fieldWithPath(domainData+SecHubDomainEncryptionData.PROPERTY_USAGE+".*").description("Key value data"),
                                        fieldWithPath(domainData+SecHubDomainEncryptionData.PROPERTY_CREATED).description("Creation timestamp"),
                                        fieldWithPath(domainData+SecHubDomainEncryptionData.PROPERTY_CREATED_FROM).description("User id of admin who created the encryption entry")
                                )
                        ));

        /* @formatter:on */
    }

    private SecHubEncryptionStatus createEncryptionStatusExample() {
        SecHubEncryptionStatus status = new SecHubEncryptionStatus();
        SecHubDomainEncryptionStatus scheduleDomainEncryptionStatus = new SecHubDomainEncryptionStatus();
        scheduleDomainEncryptionStatus.setName("schedule");

        // create some example domain encryption data like in really
        SecHubDomainEncryptionData scheduleDomainEncryptionData = new SecHubDomainEncryptionData();
        scheduleDomainEncryptionData.setAlgorithm(SecHubCipherAlgorithm.AES_GCM_SIV_256);
        scheduleDomainEncryptionData.setCreated(LocalDateTime.of(2024, 8, 1, 9, 26));
        scheduleDomainEncryptionData.setCreatedFrom("admin-username");
        scheduleDomainEncryptionData.setId("1");
        scheduleDomainEncryptionData.getPasswordSource().setType(SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE);
        scheduleDomainEncryptionData.getPasswordSource().setData("SECRET_1");

        long value = 1;
        for (ExecutionState state : ExecutionState.values()) {
            scheduleDomainEncryptionData.getUsage().put("job.state." + state.name().toLowerCase(), value++);
        }

        scheduleDomainEncryptionStatus.getData().add(scheduleDomainEncryptionData);
        status.getDomains().add(scheduleDomainEncryptionStatus);
        return status;
    }

}
