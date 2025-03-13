// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.commons.model.ClientCertificateConfiguration;
import com.mercedesbenz.sechub.commons.model.HTTPHeaderConfiguration;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSourceDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubTimeUnit;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanApiConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanApiType;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.WebLogoutConfiguration;
import com.mercedesbenz.sechub.commons.model.WebScanDurationConfiguration;
import com.mercedesbenz.sechub.commons.model.login.*;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.commons.pds.data.PDSTemplateMetaData;
import com.mercedesbenz.sechub.commons.pds.data.PDSTemplateMetaData.PDSAssetData;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterDefinition;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSServerConfiguration;
import com.mercedesbenz.sechub.test.TestFileReader;

/**
 * In our asciidoc files we have multiple references to example files. Inside
 * this test we check some of them to contain expected values.
 *
 * @author Albert Tregnaghi
 *
 */
class ExampleFilesValidTest {

    @Test
    void check_pds_config_example1_can_be_loaded_and_is_valid() throws Exception {

        /* execute */
        String json = TestFileReader.readTextFromFile("src/docs/asciidoc/documents/pds/product_delegation_server_config_example1.json");
        PDSServerConfiguration configuration = PDSServerConfiguration.fromJSON(json);

        /* test */
        assertNotNull(configuration);
        List<PDSProductSetup> products = configuration.getProducts();
        assertEquals(2, products.size());
        PDSProductSetup productSetup1 = null;
        PDSProductSetup productSetup2 = null;

        for (PDSProductSetup setup : products) {
            if ("PRODUCT_1".equals(setup.getId())) {
                productSetup1 = setup;
            } else if ("PRODUCT_2".equals(setup.getId())) {
                productSetup2 = setup;
            } else {
                fail("Unexpeted product found: " + setup.getId());
            }
        }

        // check both setup are found
        assertNotNull(productSetup1);
        assertNotNull(productSetup2);

        // make same spear checks
        assertDefaultValue(productSetup1, false, PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SUPPORTED_DATATYPES, "source");
        assertDefaultValue(productSetup2, true, PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SUPPORTED_DATATYPES, "none");
    }

    @ParameterizedTest
    @EnumSource(TestSecHubConfigExampleFile.class)
    void every_sechub_config_file_is_valid(TestSecHubConfigExampleFile file) {
        /* prepare */
        String json = TestFileReader.readTextFromFile(file.getPath());
        SecHubScanConfiguration config = null;

        /* execute */
        try {
            config = SecHubScanConfiguration.createFromJSON(json);
        } catch (JSONConverterException e) {
            fail("Could not create SecHubScanConfiguration from json for file: " + file.getPath());
        }

        /* test */
        assertNotNull(config);
    }

    @ParameterizedTest
    @EnumSource(value = TestSecHubConfigExampleFile.class, names = { "WEBSCAN_ANONYMOUS", "WEBSCAN_BASIC_AUTH", "WEBSCAN_FORM_BASED_SCRIPT_AUTH",
            "WEBSCAN_OPENAPI_WITH_DATA_REFERENCE", "WEBSCAN_HEADER_SCAN", "WEBSCAN_CLIENT_CERTIFICATE",
            "WEBSCAN_FORM_BASED_SCRIPT_AUTH_WITH_TOTP" }, mode = EnumSource.Mode.INCLUDE)
    void every_sechub_config_webscan_file_is_valid_and_has_a_target_uri(TestSecHubConfigExampleFile file) {
        /* prepare */
        String json = TestFileReader.readTextFromFile(file.getPath());

        /* execute */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        Optional<SecHubWebScanConfiguration> webScanOpt = config.getWebScan();
        assertTrue(webScanOpt.isPresent(), "Webscan configuration does exist for file: " + file.getPath());

        SecHubWebScanConfiguration webScan = webScanOpt.get();
        assertNotNull(webScan.getUrl(), "No URI set in file: " + file.getPath());
    }

    @Test
    void webscan_anonymous_can_be_read_and_contains_expected_config() {
        /* prepare */
        String json = TestFileReader.readTextFromFile(TestSecHubConfigExampleFile.WEBSCAN_ANONYMOUS.getPath());

        /* execute */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        SecHubWebScanConfiguration webScanConfig = config.getWebScan().get();
        assertEquals("https://www.gamechanger.example.org", webScanConfig.getUrl().toString());
        assertEquals(7, webScanConfig.getIncludes().get().size());
        assertEquals(7, webScanConfig.getExcludes().get().size());

        WebScanDurationConfiguration maxScanDuration = webScanConfig.getMaxScanDuration().get();
        assertEquals(SecHubTimeUnit.HOUR, maxScanDuration.getUnit());
        assertEquals(1, maxScanDuration.getDuration());
    }

    @Test
    void webscan_basic_auth_can_be_read_and_contains_expected_config() {
        /* prepare */
        String json = TestFileReader.readTextFromFile(TestSecHubConfigExampleFile.WEBSCAN_BASIC_AUTH.getPath());

        /* execute */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        SecHubWebScanConfiguration webScanConfig = config.getWebScan().get();
        assertEquals("https://productfailure.demo.example.org", webScanConfig.getUrl().toString());

        WebLoginConfiguration login = webScanConfig.getLogin().get();
        assertEquals("https://productfailure.demo.example.org/login", login.getUrl().toString());

        BasicLoginConfiguration basicAuth = login.getBasic().get();
        String user = new String(basicAuth.getUser());
        String pwd = new String(basicAuth.getPassword());
        assertEquals("{{ .LOGIN_USER }}", user);
        assertEquals("{{ .LOGIN_PWD }}", pwd);
        assertEquals("{{ .LOGIN_REALM }}", basicAuth.getRealm().get());
    }

    @Test
    void webscan_form_based_script_auth_can_be_read_and_contains_expected_config() {
        /* prepare */
        String json = TestFileReader.readTextFromFile(TestSecHubConfigExampleFile.WEBSCAN_FORM_BASED_SCRIPT_AUTH.getPath());

        /* execute */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        SecHubWebScanConfiguration webScanConfig = config.getWebScan().get();
        assertEquals("https://productfailure.demo.example.org", webScanConfig.getUrl().toString());

        WebLoginConfiguration login = webScanConfig.getLogin().get();
        assertEquals("https://productfailure.demo.example.org/login", login.getUrl().toString());

        FormLoginConfiguration form = login.getForm().get();
        Script script = form.getScript().get();
        List<Page> pages = script.getPages().get();

        assertEquals(2, pages.size());
        assertValidFormScriptFirstPage(pages.get(0));
        assertValidFormScriptSecondPage(pages.get(1));
    }

    @Test
    void webscan_openapi_with_data_reference_can_be_read_and_contains_expected_config() {
        /* prepare */
        String json = TestFileReader.readTextFromFile(TestSecHubConfigExampleFile.WEBSCAN_OPENAPI_WITH_DATA_REFERENCE.getPath());

        /* execute */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        SecHubDataConfiguration data = config.getData().get();
        List<SecHubSourceDataConfiguration> sources = data.getSources();
        assertEquals(1, sources.size());

        SecHubSourceDataConfiguration source = sources.get(0);
        assertOpenApiPart(config, source);
    }

    @Test
    void webscan_client_certificate_with_data_reference_can_be_read_and_contains_expected_config() {
        /* prepare */
        String json = TestFileReader.readTextFromFile(TestSecHubConfigExampleFile.WEBSCAN_CLIENT_CERTIFICATE_WITH_OPENAPI.getPath());

        /* execute */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        SecHubDataConfiguration data = config.getData().get();
        List<SecHubSourceDataConfiguration> sources = data.getSources();
        assertEquals(2, sources.size());

        SecHubSourceDataConfiguration source1 = sources.get(0);
        assertOpenApiPart(config, source1);

        SecHubSourceDataConfiguration source2 = sources.get(1);
        assertClientCertificatePart(config, source2);
    }

    @Test
    void webscan_client_certificate_with_openapi_can_be_read_and_contains_expected_config() {
        /* prepare */
        String json = TestFileReader.readTextFromFile(TestSecHubConfigExampleFile.WEBSCAN_CLIENT_CERTIFICATE.getPath());

        /* execute */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        SecHubDataConfiguration data = config.getData().get();
        List<SecHubSourceDataConfiguration> sources = data.getSources();
        assertEquals(1, sources.size());

        SecHubSourceDataConfiguration source = sources.get(0);
        assertClientCertificatePart(config, source);
    }

    @Test
    void webscan_header_scan_can_be_read_and_contains_expected_config() {
        /* prepare */
        String json = TestFileReader.readTextFromFile(TestSecHubConfigExampleFile.WEBSCAN_HEADER_SCAN.getPath());

        /* execute */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        SecHubWebScanConfiguration webScanConfig = config.getWebScan().get();
        assertEquals("https://productfailure.demo.example.org", webScanConfig.getUrl().toString());

        List<HTTPHeaderConfiguration> headers = webScanConfig.getHeaders().get();
        assertEquals(2, headers.size());

        HTTPHeaderConfiguration header1 = headers.get(0);
        assertEquals("Authorization", header1.getName());
        assertEquals("{{ .HEADER_VALUE }}", header1.getValue());
        assertTrue(header1.getOnlyForUrls().isEmpty());
        assertTrue(header1.isSensitive());

        HTTPHeaderConfiguration header2 = headers.get(1);
        assertEquals("x-file-size", header2.getName());
        assertEquals("123456", header2.getValue());
        assertEquals(3, header2.getOnlyForUrls().get().size());
        assertFalse(header2.isSensitive());
    }

    @Test
    void webscan_header_to_identify_sechub_requests_can_be_read_and_contains_expected_config() {
        /* prepare */
        String json = TestFileReader.readTextFromFile(TestSecHubConfigExampleFile.WEBSCAN_HEADER_TO_IDENTIFY_SECHUB_REQUESTS.getPath());

        /* execute */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        SecHubWebScanConfiguration webScanConfig = config.getWebScan().get();
        assertEquals("https://example.org", webScanConfig.getUrl().toString());

        List<HTTPHeaderConfiguration> headers = webScanConfig.getHeaders().get();
        assertEquals(1, headers.size());

        HTTPHeaderConfiguration header = headers.get(0);
        assertEquals("sechub-webscan", header.getName());
        assertEquals("unique-identifier", header.getValue());
        assertTrue(header.isSensitive());
        assertTrue(header.getOnlyForUrls().isEmpty());
    }

    @Test
    void webscan_header_from_data_reference_can_be_read_and_contains_expected_config() {
        /* prepare */
        String json = TestFileReader.readTextFromFile(TestSecHubConfigExampleFile.WEBSCAN_HEADER_FROM_DATA_REFERENCE.getPath());

        /* execute */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        SecHubDataConfiguration data = config.getData().get();
        List<SecHubSourceDataConfiguration> sources = data.getSources();
        assertEquals(1, sources.size());

        SecHubSourceDataConfiguration source = sources.get(0);
        assertEquals("header-value-file-reference", source.getUniqueName());

        List<String> folders = source.getFileSystem().get().getFolders();
        assertTrue(folders.isEmpty());
        List<String> files = source.getFileSystem().get().getFiles();
        assertEquals(1, files.size());
        assertEquals("header_value.txt", files.get(0));

        SecHubWebScanConfiguration webScanConfig = config.getWebScan().get();
        assertEquals("https://productfailure.demo.example.org", webScanConfig.getUrl().toString());

        HTTPHeaderConfiguration header = webScanConfig.getHeaders().get().get(0);
        assertEquals("Authorization", header.getName());

        Set<String> uses = header.getNamesOfUsedDataConfigurationObjects();
        assertEquals(1, uses.size());
        assertTrue(uses.contains("header-value-file-reference"));
    }

    @Test
    void webscan_form_based_script_auth_with_totp_can_be_read_and_contains_expected_config() {
        /* prepare */
        String json = TestFileReader.readTextFromFile(TestSecHubConfigExampleFile.WEBSCAN_FORM_BASED_SCRIPT_AUTH_WITH_TOTP.getPath());

        /* execute */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        SecHubWebScanConfiguration webScanConfig = config.getWebScan().get();
        assertEquals("https://productfailure.demo.example.org", webScanConfig.getUrl().toString());

        WebLoginConfiguration login = webScanConfig.getLogin().get();
        assertEquals("https://productfailure.demo.example.org/login", login.getUrl().toString());

        FormLoginConfiguration form = login.getForm().get();
        Script script = form.getScript().get();
        List<Page> pages = script.getPages().get();

        assertEquals(1, pages.size());
        assertValidFormScriptFirstPage(pages.get(0));

        WebLoginTOTPConfiguration totp = login.getTotp();
        assertEquals("example-seed", totp.getSeed());
        assertEquals(60, totp.getValidityInSeconds());
        assertEquals(8, totp.getTokenLength());
        assertEquals(TOTPHashAlgorithm.HMAC_SHA256, totp.getHashAlgorithm());
        assertEquals(EncodingType.BASE64, totp.getEncodingType());
    }

    @Test
    void webscan_with_login_validation_contains_expected_values() {
        /* prepare */
        String json = TestFileReader.readTextFromFile(TestSecHubConfigExampleFile.WEBSCAN_LOGIN_VALIDATION.getPath());

        /* execute */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        SecHubWebScanConfiguration webScanConfig = config.getWebScan().get();
        assertEquals("https://productfailure.demo.example.org", webScanConfig.getUrl().toString());

        WebLoginConfiguration login = webScanConfig.getLogin().get();
        assertEquals("https://productfailure.demo.example.org/login", login.getUrl().toString());

        WebLoginVerificationConfiguration verification = login.getVerification();
        assertEquals("https://productfailure.demo.example.org/verify", verification.getUrl().toString());
        assertEquals(204, verification.getResponseCode());
    }

    @Test
    void pds_param_template_metadata_array_syntax_example_is_valid() {
        /* prepare */
        String json = TestFileReader.readTextFromFile(TestPDSDataExampleFile.PDS_PARAM_TEMPLATE_META_DATA_SYNTAX.getPath());

        /* execute */
        List<PDSTemplateMetaData> result = JSONConverter.get().fromJSONtoListOf(PDSTemplateMetaData.class, json);

        /* test */
        assertEquals(1, result.size());
        PDSTemplateMetaData data = result.iterator().next();
        assertEquals("templateId", data.getTemplateId());
        assertEquals(TemplateType.WEBSCAN_LOGIN, data.getTemplateType());

        PDSAssetData assetData = data.getAssetData();
        assertNotNull(assetData);
        assertEquals("assetId", assetData.getAssetId());
        assertEquals("fileChecksum", assetData.getChecksum());
        assertEquals("fileName", assetData.getFileName());

    }

    @Test
    void webscan_logout_config_can_be_read_and_contains_expected_config() {
        /* prepare */
        String json = TestFileReader.readTextFromFile(TestSecHubConfigExampleFile.WEBSCAN_LOGOUT_CONFIGURATION.getPath());

        /* execute */
        SecHubScanConfiguration config = SecHubScanConfiguration.createFromJSON(json);

        /* test */
        SecHubWebScanConfiguration webScanConfig = config.getWebScan().get();
        assertEquals("https://example.org", webScanConfig.getUrl().toString());

        WebLoginConfiguration login = webScanConfig.getLogin().get();
        assertEquals("https://example.org/login", login.getUrl().toString());

        WebLogoutConfiguration logout = webScanConfig.getLogout();
        assertEquals("//*[@id=\"logoutButton\"]", logout.getXpath());
        assertEquals("button", logout.getHtmlElement());
    }

    private void assertDefaultValue(PDSProductSetup setup, boolean isMandatory, String parameterKey, String expectedDefault) {
        PDSProductParameterSetup parameters = setup.getParameters();
        List<PDSProductParameterDefinition> list = null;
        if (isMandatory) {
            list = parameters.getMandatory();
        } else {
            list = parameters.getOptional();
        }

        for (PDSProductParameterDefinition definition : list) {
            if (parameterKey.equals(definition.getKey())) {
                assertEquals(expectedDefault, definition.getDefault(), " Product:" + setup.getId() + " has unexpectedd default value");
                return;
            }
        }
        fail("No parameter with key:" + parameterKey + " found in (" + (isMandatory ? "mandatory" : "optional") + " configuration of product:" + setup.getId()
                + " !");
    }

    private void assertValidFormScriptFirstPage(Page firstPage) {
        List<Action> actionsFirstPage = firstPage.getActions().get();
        assertEquals(3, actionsFirstPage.size());

        Action action1 = actionsFirstPage.get(0);
        assertEquals(ActionType.USERNAME, action1.getType());
        assertEquals("#example_login_userid", action1.getSelector().get());
        assertEquals("{{ .LOGIN_USER }}", action1.getValue().get());

        Action action2 = actionsFirstPage.get(1);
        assertEquals(ActionType.PASSWORD, action2.getType());
        assertEquals("#example_login_pwd", action2.getSelector().get());
        assertEquals("{{ .LOGIN_PWD }}", action2.getValue().get());

        Action action3 = actionsFirstPage.get(2);
        assertEquals(ActionType.CLICK, action3.getType());
        assertEquals("#next", action3.getSelector().get());
        assertEquals("Click to go to next page", action3.getDescription().get());
    }

    private void assertValidFormScriptSecondPage(Page secondPage) {
        List<Action> actionsSecondPage = secondPage.getActions().get();
        assertEquals(3, actionsSecondPage.size());

        Action action1 = actionsSecondPage.get(0);
        assertEquals(ActionType.INPUT, action1.getType());
        assertEquals("#example_other_inputfield", action1.getSelector().get());
        assertEquals("{{ .OTHER_VALUE }}", action1.getValue().get());

        Action action2 = actionsSecondPage.get(1);
        assertEquals(ActionType.WAIT, action2.getType());
        assertEquals("1", action2.getValue().get());
        assertEquals(SecHubTimeUnit.SECOND, action2.getUnit().get());

        Action action3 = actionsSecondPage.get(2);
        assertEquals(ActionType.CLICK, action3.getType());
        assertEquals("#doLogin", action3.getSelector().get());
    }

    private void assertOpenApiPart(SecHubScanConfiguration config, SecHubSourceDataConfiguration source) {
        assertEquals("open-api-file-reference", source.getUniqueName());

        List<String> folders = source.getFileSystem().get().getFolders();
        assertTrue(folders.isEmpty());
        List<String> files = source.getFileSystem().get().getFiles();
        assertEquals(1, files.size());
        assertEquals("gamechanger-webapp/src/main/resources/openapi3.json", files.get(0));

        SecHubWebScanConfiguration webScanConfig = config.getWebScan().get();
        assertEquals("https://productfailure.demo.example.org", webScanConfig.getUrl().toString());

        SecHubWebScanApiConfiguration apiConfiguration = webScanConfig.getApi().get();
        assertEquals(SecHubWebScanApiType.OPEN_API, apiConfiguration.getType());
        assertEquals("https://productfailure.demo.example.org/api/v1/swagger/?format=openapi", apiConfiguration.getApiDefinitionUrl().toString());

        Set<String> uses = apiConfiguration.getNamesOfUsedDataConfigurationObjects();
        assertEquals(1, uses.size());
        assertTrue(uses.contains("open-api-file-reference"));
    }

    private void assertClientCertificatePart(SecHubScanConfiguration config, SecHubSourceDataConfiguration source) {
        assertEquals("client-certificate-file-reference", source.getUniqueName());

        List<String> folders = source.getFileSystem().get().getFolders();
        assertTrue(folders.isEmpty());
        List<String> files = source.getFileSystem().get().getFiles();
        assertEquals(1, files.size());
        assertEquals("path/to/backend-cert.p12", files.get(0));

        SecHubWebScanConfiguration webScanConfig = config.getWebScan().get();
        assertEquals("https://productfailure.demo.example.org", webScanConfig.getUrl().toString());

        ClientCertificateConfiguration clientCertificateConfiguration = webScanConfig.getClientCertificate().get();
        String pwd = new String(clientCertificateConfiguration.getPassword());
        assertEquals("{{ .CERT_PASSWORD }}", pwd);

        Set<String> uses = clientCertificateConfiguration.getNamesOfUsedDataConfigurationObjects();
        assertEquals(1, uses.size());
        assertTrue(uses.contains("client-certificate-file-reference"));
    }

}
