// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario9;

import static com.mercedesbenz.sechub.commons.model.TrafficLight.*;
import static com.mercedesbenz.sechub.integrationtest.api.IntegrationTestMockMode.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario9.Scenario9.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.api.internal.gen.model.CodeExample;
import com.mercedesbenz.sechub.api.internal.gen.model.Reference;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubExplanationResponse;
import com.mercedesbenz.sechub.api.internal.gen.model.TextBlock;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubStatus;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;

/**
 * Integration test doing code scans by integration test servers (SecHub server,
 * PDS server).
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSCodeScanSarifJobScenario9IntTest {

    public static final String PATH = "pds/codescan/upload/zipfile_contains_inttest_codescan_with_critical_sarif.zip";

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario9.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    TestProject project = PROJECT_1;

    /**
     * Important: This test is only working when we have same storage for SECHUB and
     * for PDS defined!
     */
    @Test
    public void pds_reuses_sechub_data__user_starts_webscan_marks_report_finding_1_as_fp_and_scans_again_without_this_finding() {
        /* @formatter:off */

        /* ---------------------------------------------*/
        /* Phase 1: WebScan without false positive data */
        /* ---------------------------------------------*/

        /* prepare 1*/
        TestProject project = PROJECT_1;

        UUID jobUUID = as(USER_1).createWebScan(project,NOT_MOCKED);// scenario9 uses real integration test pds server!

        /* execute 1*/
        as(USER_1).
            approveJob(project, jobUUID);

        waitForJobDone(project, jobUUID,30,true);

        /* test 1*/
        String report = as(USER_1).getJobReport(project, jobUUID);
        assertReport(report).
            hasStatus(SecHubStatus.SUCCESS).
            hasTrafficLight(RED).
            hasFindings(14).
               finding(0).
                   hasCweId(79).
                   hasId(1).
                   hasSeverity(Severity.HIGH).
                   hasScanType(ScanType.WEB_SCAN).
                   hasName("Cross Site Scripting (Reflected)").
                   hasDescriptionContaining("There are three types").
                   hasDescriptionContaining("DOM-based").
               finding(1).
                   hasCweId(693).
                   hasId(2).
                   hasName("CSP: Wildcard Directive").
                   hasScanType(ScanType.WEB_SCAN).
                   hasSeverity(Severity.MEDIUM).
                   hasDescriptionContaining("either allow wildcard sources");

        /* --------------------------------------------------------*/
        /* Phase 2: WebScan with false positive data definition set*/
        /*          Next web scan for same project does not contain*/
        /*          the formerly marked false positive             */
        /* --------------------------------------------------------*/

        /* prepare 2 */
        as(USER_1).startFalsePositiveDefinition(PROJECT_1).add(1, jobUUID).markAsFalsePositive();


        UUID jobUUID2 = as(USER_1).createWebScan(project,NOT_MOCKED);// scenario9 uses real integration test pds server!

        /* execute 2 */
        as(USER_1).
            approveJob(project, jobUUID2);

        waitForJobDone(project, jobUUID2,30,true);

        /* test 2 */
        String report2 = as(USER_1).getJobReport(project, jobUUID2);
        assertReport(report2).
            hasStatus(SecHubStatus.SUCCESS).
            hasTrafficLight(YELLOW).// high finding no longer in report but only medium ones...
            hasFindings(13).// report has one finding less
               finding(0).
                   hasCweId(693).
                   hasId(2). // finding 1 is still there, but a false positive... so first finding inside this report is still having id 2
                   hasName("CSP: Wildcard Directive").
                   hasScanType(ScanType.WEB_SCAN).
                   hasSeverity(Severity.MEDIUM).
                   hasDescriptionContaining("either allow wildcard sources");
        /* @formatter:on */
    }

    /**
     * Important: This test is only working when we have same storage for SECHUB and
     * for PDS defined!
     */
    @Test
    public void pds_reuses_sechub_data__a_user_can_start_a_pds_codescan_with_sarif_output_and_get_result_and_can_get_an_explanation_afterwards() {
        /* @formatter:off */

        /* prepare */
        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createCodeScan(project,NOT_MOCKED);// scenario9 uses real integration test pds server!


        /* execute */
        as(USER_1).
            uploadSourcecode(project, jobUUID, PATH).
            approveJob(project, jobUUID);

        waitForJobDone(project, jobUUID,30,true);

        /* test */
        // test storage is a SecHub storage and no PDS storage
        String storagePath = fetchStoragePathHistoryEntryoForSecHubJobUUID(jobUUID); // this is a SecHub job UUID!
        assertNotNull("Storage path not found for SecHub job UUID:"+jobUUID+" - wrong storage used!",storagePath); // storage path must be found for sechub job uuid,
        if (!storagePath.contains("jobstorage/"+project.getProjectId())){
            fail("unexpected jobstorage path found:"+storagePath);
        }

        // test content as expected


        String report = as(USER_1).getJobReport(project, jobUUID);
        assertReport(report).
            hasStatus(SecHubStatus.SUCCESS).
            hasTrafficLight(RED).
               finding(0).
                   hasSeverity(Severity.HIGH).
                   hasScanType(ScanType.CODE_SCAN).
                   hasName("BRAKE0102").
                   hasDescription("Rails 5.0.0 `content_tag` does not escape double quotes in attribute values (CVE-2016-6316). Upgrade to Rails 5.0.0.1.").
                   codeCall(0).
                      hasLocation("Gemfile.lock").
                      hasLine(115).
               andFinding(28). // 28 because it is sorted
                   hasName("BRAKE0116").
                   hasScanType(ScanType.CODE_SCAN).
                   hasSeverity(Severity.MEDIUM).
                   hasDescription("Rails 5.0.0 has a vulnerability that may allow CSRF token forgery. Upgrade to Rails 5.2.4.3 or patch.");


        // check script trust all is defined here with "false". Because PROFILE_3_PDS_CODESCAN_SARIF
        // uses PDS_V1_CODE_SCAN_D which has defined the parameter as false
        assertPDSJob(assertAndFetchPDSJobUUIDForSecHubJob(jobUUID)).
            containsVariableTestOutput("PDS_DEBUG_ENABLED", ""). // this executor config has not debugging enabled, so variable is empty
            containsVariableTestOutput("PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED", "false");
        /* @formatter:on */

        SecHubExplanationResponse expectedResponse = createExpectedSecHubExplanationResponse();
        SecHubExplanationResponse explanationResponse = as(USER_1).explainFinding(project.getProjectId(), jobUUID, 0);

        assertEquals(expectedResponse, explanationResponse);

        String explanationResponseAsJson = JSONConverter.get().toJSON(explanationResponse);
        String expectedResponseAsJson = JSONConverter.get().toJSON(expectedResponse);
        assertEquals(expectedResponseAsJson, explanationResponseAsJson);
    }

    private SecHubExplanationResponse createExpectedSecHubExplanationResponse() {
        SecHubExplanationResponse response = new SecHubExplanationResponse();

        // Create and set FindingExplanation
        TextBlock findingExplanation = new TextBlock();
        findingExplanation.setTitle("Absolute Path Traversal Vulnerability");
        findingExplanation.setContent(
                "This finding indicates an 'Absolute Path Traversal' vulnerability in the `AsciidocGenerator.java` file. The application constructs a file path using user-supplied input (`args[0]`) without proper validation. An attacker could provide an absolute path (e.g., `/etc/passwd` on Linux or `C:\\Windows\\System32\\drivers\\etc\\hosts` on Windows) as input, allowing them to access arbitrary files on the system, potentially bypassing intended security restrictions [3, 7].");
        response.setFindingExplanation(findingExplanation);

        // Create and set PotentialImpact
        TextBlock potentialImpact = new TextBlock();
        potentialImpact.setTitle("Potential Impact");
        potentialImpact.setContent(
                "If exploited, this vulnerability could allow an attacker to read sensitive files on the server, including configuration files, source code, or even password files. This could lead to information disclosure, privilege escalation, or other malicious activities [1, 5].");
        response.setPotentialImpact(potentialImpact);

        // Create and set Recommendations
        List<TextBlock> recommendations = new ArrayList<>();

        TextBlock recommendation1 = new TextBlock();
        recommendation1.setTitle("Validate and Sanitize User Input");
        recommendation1.setContent(
                "Always validate and sanitize user-supplied input before using it to construct file paths. In this case, ensure that the `path` variable does not contain an absolute path. You can check if the path starts with a drive letter (e.g., `C:\\`) on Windows or a forward slash (`/`) on Unix-like systems [1].");
        recommendations.add(recommendation1);

        TextBlock recommendation2 = new TextBlock();
        recommendation2.setTitle("Use Relative Paths and a Base Directory");
        recommendation2.setContent(
                "Instead of allowing absolute paths, restrict user input to relative paths within a designated base directory. Construct the full file path by combining the base directory with the user-provided relative path. This limits the attacker's ability to access files outside the intended directory [1].");
        recommendations.add(recommendation2);

        TextBlock recommendation3 = new TextBlock();
        recommendation3.setTitle("Normalize the Path");
        recommendation3.setContent(
                "Normalize the constructed file path to remove any directory traversal sequences (e.g., `../`). This can be achieved using the `java.nio.file.Path.normalize()` method. After normalization, verify that the path still resides within the allowed base directory [1, 6].");
        recommendations.add(recommendation3);

        response.setRecommendations(recommendations);

        // Create and set CodeExample
        CodeExample codeExample = new CodeExample();
        codeExample.setVulnerableExample(
                "public static void main(String[] args) throws Exception {\n  String path = args[0];\n  File documentsGenFolder = new File(path);\n  //Potentially dangerous operation with documentsGenFolder\n}");
        codeExample.setSecureExample(
                "public static void main(String[] args) throws Exception {\n  String basePath = \"/safe/base/directory\";\n  String userPath = args[0];\n\n  // Validate that userPath is not an absolute path\n  if (new File(userPath).isAbsolute()) {\n    System.err.println(\"Error: Absolute paths are not allowed.\");\n    return;\n  }\n\n  Path combinedPath = Paths.get(basePath, userPath).normalize();\n\n  // Ensure the combined path is still within the base directory\n  if (!combinedPath.startsWith(basePath)) {\n    System.err.println(\"Error: Path traversal detected.\");\n    return;\n  }\n\n  File documentsGenFolder = combinedPath.toFile();\n  //Safe operation with documentsGenFolder\n}");

        TextBlock explanation = new TextBlock();
        explanation.setTitle("Code Example Explanation");
        explanation.setContent(
                "The vulnerable example directly uses user-provided input to create a `File` object, allowing an attacker to specify an arbitrary file path. The secure example first defines a base directory and combines it with the user-provided path using `Paths.get()`. It then normalizes the path and verifies that it remains within the base directory before creating the `File` object. This prevents path traversal attacks by ensuring that the application only accesses files within the intended directory [2, 6].");
        codeExample.setExplanation(explanation);
        response.setCodeExample(codeExample);

        // Create and set References
        List<Reference> references = new ArrayList<>();

        Reference reference1 = new Reference();
        reference1.setTitle("OWASP Path Traversal");
        reference1.setUrl("https://owasp.org/www-community/attacks/Path_Traversal");
        references.add(reference1);

        Reference reference2 = new Reference();
        reference2.setTitle("CWE-22: Improper Limitation of a Pathname to a Restricted Directory ('Path Traversal')");
        reference2.setUrl("https://cwe.mitre.org/data/definitions/22.html");
        references.add(reference2);

        Reference reference3 = new Reference();
        reference3.setTitle("Snyk Path Traversal");
        reference3.setUrl("https://snyk.io/learn/path-traversal/");
        references.add(reference3);

        response.setReferences(references);

        return response;
    }
}
