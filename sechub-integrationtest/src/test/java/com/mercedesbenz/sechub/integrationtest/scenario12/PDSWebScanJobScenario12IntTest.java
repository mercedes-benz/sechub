// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario12;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.as;
import static com.mercedesbenz.sechub.integrationtest.scenario12.Scenario12.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.ClientCertificateConfiguration;
import com.mercedesbenz.sechub.commons.model.HTTPHeaderConfiguration;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanApiConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanApiType;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestFileSupport;

/**
 * Integration test doing web scans by integration test servers (sechub server,
 * pds server)
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSWebScanJobScenario12IntTest {

    private static final String TEMPLATE_VARIABLE_PASSWORD = "test-password";

    private static final String TEMPLATE_VARIABLE_USERNAME = "test-username";

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario12.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    /* @formatter:off
     *
     * This is test is a web scan integration test which
     * tests multiple features.
     *
     * The test prepares
     * - web scan in general with dedicated setup
     * - uses a SecHub configuration with template data inside
     * - creates an asset, creates a template which uses the asset, assigns template
     *
     * The tests checks following:
     *
     * - PDS web scan has expected info finding, with
     *    - given target URL
     *    - product level information
     *    - SecHub web configuration parts
     *
     *  - PDS parameter for template meta data configuration is correct and transmitted to PDS
     *    The parameter "pds.config.template.metadata.list" is normally not available inside
     *    the scripts, but for testing we added the parameter inside server configuration so it
     *    will be added to script level and can be checked by TestAPI
     *
     *  - PDS will download and extract the uploaded asset file automatically and the
     *    extracted content is available inside the test bash script (executed by PDS)
     *
     *
     * @formatter:on
     */
    @Test
    public void pds_web_scan_can_be_executed_and_works() throws Exception {
        /* @formatter:off */

        /* prepare */
        String assetId="asset-s12-pds-inttest-webscan";
        File productZipFile = IntegrationTestFileSupport.getTestfileSupport().createFileFromResourcePath("/asset/scenario12/PDS_INTTEST_PRODUCT_WEBSCAN.zip");

        String configurationAsJson = IntegrationTestFileSupport.getTestfileSupport().loadTestFile("sechub-integrationtest-webscanconfig-all-options.json");
        SecHubScanConfiguration configuration = SecHubScanConfiguration.createFromJSON(configurationAsJson);
        configuration.setProjectId("myTestProject");

        TestProject project = PROJECT_1;
        String targetURL = configuration.getWebScan().get().getUrl().toString();

        TemplateVariable userNameVariable = new TemplateVariable();
        userNameVariable.setName(TEMPLATE_VARIABLE_USERNAME);

        TemplateVariable passwordVariable = new TemplateVariable();
        passwordVariable.setName(TEMPLATE_VARIABLE_PASSWORD);

        TemplateDefinition templateDefinition = new TemplateDefinition();
        templateDefinition.setAssetId(assetId);
        templateDefinition.setType(TemplateType.WEBSCAN_LOGIN);
        templateDefinition.getVariables().add(userNameVariable);
        templateDefinition.getVariables().add(passwordVariable);

        String templateId = "template-scenario12-1";

        // we must add the mandatory variables from template definition also to template data section of configuration (or job could not be created)
        var templateDataVariables = configuration.getWebScan().get().getLogin().get().getTemplateData().getVariables();
        templateDataVariables.put(TEMPLATE_VARIABLE_USERNAME, "test-user");
        templateDataVariables.put(TEMPLATE_VARIABLE_PASSWORD, "test-fake-password");


        as(SUPER_ADMIN).
            updateWhiteListForProject(project, Arrays.asList(targetURL)).
            uploadAssetFile(assetId, productZipFile).
            createOrUpdateTemplate(templateId, templateDefinition).
            assignTemplateToProject(templateId, project)
        ;

        /* prepare 2 */
        // this is just for testing that even with illegal values, the configuration is accepted without errors
        String maxScanConfiguration = """
                {
                    "apiVersion": "1.0",
                    "webScan": {
                        "url": "%s",
                        "maxScanDuration": {
                			"duration" : 1,
                			"unit" : "invalid_value"
                		}
                    }
                }
                """.formatted(targetURL);

        /* execute */
        UUID jobUUID = as(USER_1).withSecHubClient().startAsynchronScanFor(project, configuration).getJobUUID();
        waitForJobDone(project, jobUUID, 30, true);

        /* execute 2 */
        UUID secondJobUUID = as(USER_1).createJobFromStringAndReturnJobUUID(project, maxScanConfiguration);

        /* test */
        String sechubReport = as(USER_1).getJobReport(project, jobUUID);

        // IMPORTANT: The 'integrationtest-webscan.sh' returns the configuration file as part of the resulting report.
        //            It is necessary to start a PDS and SecHub in integration mode. The web scan will be created on the
        //            SecHub server and SecHub calls the PDS. The PDS in return calls the 'integrationtest-webscan.sh',
        //            which produces the report.
        //
        // Workflow:
        //   This test -- sends webscan config to -> SecHub -- calls -> PDS -- calls -> 'integrationtest-webscan.sh' -- returns -> Report
        //
        // look at 'integrationtest-webscan.sh' for implementation details
        // finding 1: contains target url and more
        // finding 2: contains sechub configuration (only web parts)
        String descriptionFinding2WithDataInside = assertReport(sechubReport).
            enablePDSAutoDumpOnErrorsForSecHubJob(jobUUID).
            hasMessages(4).
            finding(0).
                hasSeverity(Severity.INFO).
                hasDescriptionContaining("PRODUCT2_LEVEL=4711").// this comes from custom mandatory parameter from PDS config
                hasDescriptionContaining("PDS_SCAN_TARGET_URL=" + targetURL). // this is a default generated parameter which will always be sent by SecHub without being defined in PDS config!
            finding(1).
                hasDescriptionContaining("PDS_SCAN_CONFIGURATION={").
                getDescription();

        String returndPdsScanConfigurationJSON =
                 descriptionFinding2WithDataInside.substring("PDS_SCAN_CONFIGURATION=".length());

        // the returned JSON must be a valid sechub scan configuration
        SecHubScanConfiguration returnedConfiguration = SecHubScanConfiguration.createFromJSON(returndPdsScanConfigurationJSON);
        assertEquals("ProjectId not as expected", project.getProjectId(), returnedConfiguration.getProjectId());
        assertFalse(targetURL, returnedConfiguration.getCodeScan().isPresent());
        assertFalse(targetURL, returnedConfiguration.getInfraScan().isPresent());
        assertTrue(targetURL, returnedConfiguration.getWebScan().isPresent());

        SecHubWebScanConfiguration webConfiguration = returnedConfiguration.getWebScan().get();
        assertNotNull(webConfiguration.getUrl());
        assertEquals(JSONConverter.get().toJSON(configuration, true), JSONConverter.get().toJSON(returnedConfiguration, true));

        // config must contain the includes/excludes with wildcards
        assertTrue("The web scan config hould contain includes!", webConfiguration.getIncludes().isPresent());
        assertTrue("The web scan config hould contain excludes!", webConfiguration.getExcludes().isPresent());

        List<String> includes = webConfiguration.getIncludes().get();
        List<String> excludes = webConfiguration.getExcludes().get();

        assertTrue(includes.contains("/customer/<*>"));
        assertTrue(excludes.contains("<*>/admin/<*>"));

        // web configuration must contain the expected headers
        assertExpectedHeaders(webConfiguration);

        // web configuration must contain the expected client certificate
        assertExpectedClientCertificate(webConfiguration);

        // web configuration must contain the expected openApi definition
        assertExpectedOpenApiDefinition(webConfiguration);

        /* additional testing : messages*/

        assertJobStatus(project, jobUUID).
            enablePDSAutoDumpOnErrorsForSecHubJob().
            hasMessage(SecHubMessageType.INFO,"info from webscan by PDS for sechub job uuid: "+jobUUID).
            hasMessage(SecHubMessageType.WARNING,"warning from webscan by PDS for sechub job uuid: "+jobUUID).
            hasMessage(SecHubMessageType.ERROR,"error from webscan by PDS for sechub job uuid: "+jobUUID).
            hasMessage(SecHubMessageType.INFO, "another-token.txtbearer-token.txtcertificate.p12openapi.json");

        UUID pdsJobUUID = waitForFirstPDSJobOfSecHubJobAndReturnPDSJobUUID(jobUUID);
        Map<String, String> variables = fetchPDSVariableTestOutputMap(pdsJobUUID);

        String expectedMetaDataListJson = """
                [{"templateId":"template-scenario12-1","templateType":"WEBSCAN_LOGIN","assetData":{"assetId":"asset-s12-pds-inttest-webscan","fileName":"PDS_INTTEST_PRODUCT_WEBSCAN.zip","checksum":"ff06430bfc2d8c698ab8effa41b914525b8cca1c1eecefa76d248b25cc598fba"}}]
                """.trim();
        assertThat(variables.get("PDS_CONFIG_TEMPLATE_METADATA_LIST")).isEqualTo(expectedMetaDataListJson);
        assertThat(variables.get("TEST_CONTENT_FROM_ASSETFILE")).isEqualTo("i am \"testfile1.txt\" for scenario12 integration tests");
        /* @formatter:on */

        /* test 2 */
        assertNotNull(secondJobUUID);
    }

    private void assertExpectedHeaders(SecHubWebScanConfiguration webConfiguration) {
        assertTrue(webConfiguration.getHeaders().isPresent());

        List<HTTPHeaderConfiguration> headers = webConfiguration.getHeaders().get();
        assertEquals(3, headers.size());

        Iterator<HTTPHeaderConfiguration> iterator = headers.iterator();
        assertTrue(iterator.hasNext());

        HTTPHeaderConfiguration firstHeader = iterator.next();
        assertEquals("Authorization", firstHeader.getName());
        Set<String> firstHeaderReferences = firstHeader.getNamesOfUsedDataConfigurationObjects();
        assertTrue(firstHeaderReferences.contains("header-file-ref-for-big-token"));
        assertTrue(firstHeader.getOnlyForUrls().isEmpty());
        assertTrue(firstHeader.isSensitive());

        HTTPHeaderConfiguration secondHeader = iterator.next();
        assertEquals("x-file-size", secondHeader.getName());
        assertEquals("123456", secondHeader.getValue());
        assertFalse(secondHeader.getOnlyForUrls().isEmpty());
        assertEquals(3, secondHeader.getOnlyForUrls().get().size());
        assertFalse(secondHeader.isSensitive());

        HTTPHeaderConfiguration thirdHeader = iterator.next();
        assertEquals("Key", thirdHeader.getName());
        Set<String> thirdHeaderReferences = thirdHeader.getNamesOfUsedDataConfigurationObjects();
        assertTrue(thirdHeaderReferences.contains("another-header-file-ref-for-big-token"));
        assertTrue(thirdHeader.getOnlyForUrls().isEmpty());
        assertTrue(thirdHeader.isSensitive());
    }

    private void assertExpectedClientCertificate(SecHubWebScanConfiguration webConfiguration) {
        assertTrue(webConfiguration.getClientCertificate().isPresent());

        ClientCertificateConfiguration clientCertificateConfiguration = webConfiguration.getClientCertificate().get();

        assertEquals("secret-password", new String(clientCertificateConfiguration.getPassword()));

        Set<String> namesOfUsedDataConfigurationObjects = clientCertificateConfiguration.getNamesOfUsedDataConfigurationObjects();
        assertTrue(namesOfUsedDataConfigurationObjects.contains("client-cert-api-file-reference"));
    }

    private void assertExpectedOpenApiDefinition(SecHubWebScanConfiguration webConfiguration) {
        assertTrue(webConfiguration.getApi().isPresent());

        SecHubWebScanApiConfiguration secHubWebScanApiConfiguration = webConfiguration.getApi().get();

        assertEquals(SecHubWebScanApiType.OPEN_API, secHubWebScanApiConfiguration.getType());

        Set<String> namesOfUsedDataConfigurationObjects = secHubWebScanApiConfiguration.getNamesOfUsedDataConfigurationObjects();
        assertTrue(namesOfUsedDataConfigurationObjects.contains("open-api-file-reference"));

    }

}
