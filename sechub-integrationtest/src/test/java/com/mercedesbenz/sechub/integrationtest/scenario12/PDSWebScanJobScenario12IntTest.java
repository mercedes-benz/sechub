// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario12;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario12.Scenario12.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.commons.model.*;
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

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario12.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test
    public void pds_web_scan_has_expected_info_finding_with_given_target_url_and_product2_level_information_and_sechub_web_config_parts() {
        /* @formatter:off */

        /* prepare */
        String configurationAsJson = IntegrationTestFileSupport.getTestfileSupport().loadTestFile("sechub-integrationtest-webscanconfig-all-options.json");
        SecHubScanConfiguration configuration = SecHubScanConfiguration.createFromJSON(configurationAsJson);
        configuration.setProjectId("myTestProject");

        TestProject project = PROJECT_1;
        String targetURL = configuration.getWebScan().get().getUrl().toString();
        as(SUPER_ADMIN).updateWhiteListForProject(project, Arrays.asList(targetURL));

        /* execute */
        UUID jobUUID = as(USER_1).withSecHubClient().startAsynchronScanFor(project, configuration).getJobUUID();
        waitForJobDone(project, jobUUID, 30, true);

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

        // config must contain the expected headers
        assertExpectedHeaders(webConfiguration);

        // config must contain the expected client certificate
        assertExpectedClientCertificate(webConfiguration);

        // config must contain the expected openApi definition
        assertExpectedOpenApiDefinition(webConfiguration);

        /* additional testing : messages*/

        assertJobStatus(project, jobUUID).
            enablePDSAutoDumpOnErrorsForSecHubJob().
            hasMessage(SecHubMessageType.INFO,"info from webscan by PDS for sechub job uuid: "+jobUUID).
            hasMessage(SecHubMessageType.WARNING,"warning from webscan by PDS for sechub job uuid: "+jobUUID).
            hasMessage(SecHubMessageType.ERROR,"error from webscan by PDS for sechub job uuid: "+jobUUID).
            hasMessage(SecHubMessageType.INFO, "another-token.txtbearer-token.txtcertificate.p12openapi.json");

        /* @formatter:on */
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
