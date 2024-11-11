// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;
import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.List;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.docgen.util.RestDocTestFileSupport;
import com.mercedesbenz.sechub.domain.scan.asset.AssetDetailData;
import com.mercedesbenz.sechub.domain.scan.asset.AssetFileData;
import com.mercedesbenz.sechub.domain.scan.asset.AssetFileRepository;
import com.mercedesbenz.sechub.domain.scan.asset.AssetRestController;
import com.mercedesbenz.sechub.domain.scan.asset.AssetService;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc.SpringRestDocOutput;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminDeletesAssetCompletely;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminDeletesOneFileFromAsset;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminDownloadsAssetFile;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesAssetDetails;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesAssetIds;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUploadsAssetFile;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(AssetRestController.class)
@ContextConfiguration(classes = { AssetRestController.class, AssetRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class AssetRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final String TEST_CHECKSUM1 = "c6965634c4ec8e4f5e72dffd36ea725860e8b485216260264a0973073805e422";

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssetFileRepository assetFileRepository;

    @MockBean
    AssetService assetService;

    @MockBean
    AuditLogService auditLogService;

    @MockBean
    LogSanitizer logSanitizer;

    private static final String TEST_ASSET_ID1 = "asset-1";
    private static final String TEST_ASSET_ID2 = "asset-2s";

    private static final String TEST_FILENAME1 = "PRODUCT1.zip";

    @Before
    public void before() {
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminDeletesOneFileFromAsset.class)
    public void restdoc_admin_deletes_one_file_from_asset() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminDeletesAssetFile(ASSET_ID.pathElement(), FILE_NAME.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminDeletesOneFileFromAsset.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                delete(apiEndpoint, TEST_ASSET_ID1, TEST_FILENAME1).
                header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                useCaseData(useCase).
                tag(RestDocFactory.extractTag(apiEndpoint)).
                requestSchema(OpenApiSchema.ASSETS.getSchema()).
                and().
                document(
                        pathParameters(
                                parameterWithName(ASSET_ID.paramName()).description("The asset identifier"),
                                parameterWithName(FILE_NAME.paramName()).description("The name of the file to delete inside the asset")
                                )
                        ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminDeletesAssetCompletely.class)
    public void restdoc_admin_deletes_asset_completely() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminDeletesAsset(ASSET_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminDeletesAssetCompletely.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                delete(apiEndpoint, TEST_ASSET_ID1).
                contentType(MediaType.APPLICATION_JSON_VALUE).
                header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                useCaseData(useCase).
                tag(RestDocFactory.extractTag(apiEndpoint)).
                requestSchema(OpenApiSchema.ASSETS.getSchema()).
                and().
                document(
                        pathParameters(
                                parameterWithName(ASSET_ID.paramName()).description("The asset identifier for the asset which shall be deleted completely")
                                )
                        ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminFetchesAssetIds.class)
    public void restdoc_admin_fetches_all_asset_ids() throws Exception {
        /* prepare */
        when(assetService.fetchAllAssetIds()).thenReturn(List.of(TEST_ASSET_ID1, TEST_ASSET_ID2));

        String apiEndpoint = https(PORT_USED).buildAdminFetchesAllAssetIds();
        Class<? extends Annotation> useCase = UseCaseAdminFetchesAssetIds.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(apiEndpoint).
                header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                useCaseData(useCase).
                tag(RestDocFactory.extractTag(apiEndpoint)).
                requestSchema(OpenApiSchema.ASSETS.getSchema()).
                and().
                document(
                        responseFields(
                                fieldWithPath("[]").description("Array contains all existing asset identifiers")
                                )
                        ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminFetchesAssetDetails.class)
    public void restdoc_admin_fetches_asset_details() throws Exception {
        AssetDetailData asset1Details = new AssetDetailData();
        asset1Details.setAssetId(TEST_ASSET_ID1);
        AssetFileData fileInfo = new AssetFileData();
        fileInfo.setChecksum(TEST_CHECKSUM1);
        fileInfo.setFileName(TEST_FILENAME1);
        asset1Details.getFiles().add(fileInfo);
        /* prepare */
        when(assetService.fetchAssetDetails(TEST_ASSET_ID1)).thenReturn(asset1Details);

        String apiEndpoint = https(PORT_USED).buildAdminFetchesAssetDetails(ASSET_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminFetchesAssetDetails.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                get(apiEndpoint, TEST_ASSET_ID1).
                contentType(MediaType.APPLICATION_JSON_VALUE).
                header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                useCaseData(useCase).
                tag(RestDocFactory.extractTag(apiEndpoint)).
                requestSchema(OpenApiSchema.ASSETS.getSchema()).
                and().
                document(
                        responseFields(
                                fieldWithPath("assetId").description("The asset identifier"),
                                fieldWithPath("files[]").description("Array containing data about files from asset"),
                                fieldWithPath("files[].fileName").description("Name of file"),
                                fieldWithPath("files[].checksum").description("Checksum for file")
                                )
                        ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminUploadsAssetFile.class)
    public void restDoc_admin_uploads_assetfile() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminUploadsAssetFile(ASSET_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminUploadsAssetFile.class;

        InputStream inputStreamTo = RestDocTestFileSupport.getTestfileSupport().getInputStreamTo("upload/zipfile_contains_only_test1.txt.zip");
        MockMultipartFile file1 = new MockMultipartFile("file", inputStreamTo);
        /* execute + test @formatter:off */
        this.mockMvc.perform(
                multipart(apiEndpoint, TEST_ASSET_ID1).
                    file(file1).
                    queryParam(MULTIPART_CHECKSUM, TEST_CHECKSUM1)
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
                                            parameterWithName(ASSET_ID.paramName()).description("The id of the asset to which the uploaded file belongs to")
                                    ),
                                    queryParameters(
                                            parameterWithName(MULTIPART_CHECKSUM).description("A sha256 checksum for file upload validation")
                                    ),
                                    requestParts(
                                            partWithName(MULTIPART_FILE).description("The asset file to upload")
                                    )
                    ));

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminDownloadsAssetFile.class, wanted = { SpringRestDocOutput.PATH_PARAMETERS, SpringRestDocOutput.REQUEST_FIELDS,
            SpringRestDocOutput.CURL_REQUEST })
    public void restdoc_admin_downloads_assetfile() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminDownloadsAssetFile(ASSET_ID.pathElement(), FILE_NAME.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminDownloadsAssetFile.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                    get(apiEndpoint,TEST_ASSET_ID1, TEST_FILENAME1).
                    contentType(MediaType.APPLICATION_JSON_VALUE).
                    header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    responseSchema(OpenApiSchema.ASSETS.getSchema()).
                and().
                document(
                            requestHeaders(

                            ),
                            pathParameters(
                                    parameterWithName(ASSET_ID.paramName()).description("The asset identifier"),
                                    parameterWithName(FILE_NAME.paramName()).description("The name of the file to download from asset")
                )
        ));

        /* @formatter:on */
    }

    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractSecHubAPISecurityConfiguration {

    }

}
