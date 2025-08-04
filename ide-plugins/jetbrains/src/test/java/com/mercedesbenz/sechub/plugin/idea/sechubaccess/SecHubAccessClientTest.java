package com.mercedesbenz.sechub.plugin.idea.sechubaccess;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositiveEntry;
import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositiveJobData;
import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositiveProjectConfiguration;
import com.mercedesbenz.sechub.api.internal.gen.model.ProjectData;
import org.junit.*;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.*;

public class SecHubAccessClientTest {

    private static WireMockServer wireMockServer;
    private static SecHubAccessClient clientToTest;
    private static final SecHubAccessClient failingClient = new SecHubAccessClient(
            "http://does-not-exist.localhost",
            "user",
            "token",
            true
    );

    @BeforeClass
    public static void setUpClass() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        /* Mock /api/server/alive endpoint */
        WireMock.stubFor(WireMock.head(UrlPattern.fromOneOf("/api/anonymous/check/alive", null, null, null, null))
                .willReturn(WireMock.aResponse().withStatus(200)));

        /* Mock /api/projects endpoint */
        WireMock.stubFor(WireMock.get("/api/projects")
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/ajson")
                        .withBody("""
                                {
                                  "projectId": "example-project",
                                  "owner": {
                                    "userId": "owner-id"
                                  },
                                  "isOwned": true,
                                  "assignedUsers": [],
                                  "enabledProfileIds": []
                                }
                                """)));

        /* Mock /api/project/{projectId}/false-positives endpoint */
        WireMock.stubFor(WireMock.get("/api/project/example-project/false-positives")
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/ajson")
                        .withBody("""
                                {
                                  "falsePositives": [
                                    {
                                      "jobData": {
                                        "jobUUID": "117bbc23-309d-4ff3-b805-515b73c823fd",
                                        "findingId": 1,
                                        "comment": "A fix has already been started - "
                                      },
                                      "author": "owner-id",
                                      "metaData": {
                                        "scanType": "codeScan",
                                        "name": "Absolute Path Traversal",
                                        "severity": "MEDIUM",
                                        "code": {
                                          "start": {
                                            "sourceCode": "public static void main(String[] args) throws Exception {",
                                            "relevantPart": "args",
                                            "location": "java/com/example/example-project/docgen/AsciidocGenerator.java"
                                          },
                                          "end": {
                                            "sourceCode": "File documentsGenFolder = new File(path);",
                                            "relevantPart": "File",
                                            "location": "java/com/example/example-project/docgen/AsciidocGenerator.java"
                                          }
                                        },
                                        "web": null,
                                        "cweId": 36,
                                        "cveId": null,
                                        "owasp": null
                                      },
                                      "projectData": null,
                                      "created": "2025-08-04 09:03:36"
                                    }
                                  ]
                                }
                                """)));

        clientToTest = new SecHubAccessClient(
                "http://localhost:" + wireMockServer.port(),
                "user",
                "token",
                true
        );
    }

    @AfterClass
    public static void tearDownClass() {
        wireMockServer.stop();
    }

    @Test
    public void initSecHubClient_with_null_server_url_throws_null_pointer_exception() {
        try {
            new SecHubAccessClient(null, "user", "token", true);
            fail("Expected NullPointerException not thrown");
        } catch (NullPointerException e) {
            assertEquals("Parameter 'secHubServerUrl' must not be null", e.getMessage());
        }
    }

    @Test
    public void initSecHubClient_with_null_user_id_throws_null_pointer_exception() {
        try {
            new SecHubAccessClient("http://localhost", null, "token", true);
            fail("Expected NullPointerException not thrown");
        } catch (NullPointerException e) {
            assertEquals("Parameter 'userId' must not be null", e.getMessage());
        }
    }

    @Test
    public void initSecHubClient_with_null_api_token_throws_null_pointer_exception() {
        try {
            new SecHubAccessClient("http://localhost", "user", null, true);
            fail("Expected NullPointerException not thrown");
        } catch (NullPointerException e) {
            assertEquals("Parameter 'apiToken' must not be null", e.getMessage());
        }
    }

    @Test
    public void initSecHubClient_with_invalid_server_url_throws_illegal_state_exception() {
        try {
            new SecHubAccessClient("invalid-url", "user", "token", true);
            fail("Expected IllegalStateException not thrown");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Invalid parameter 'secHubServerUrl': invalid-url"));
        }
    }

    @Test
    public void isSecHubServerAlive_returns_true_when_server_responds_200() {
        /* execute + test */
        assertTrue(clientToTest.isSecHubServerAlive());
    }

    @Test
    public void isSecHubServerAlive_returns_false_on_exception() {
        /* execute + test */
        assertFalse(failingClient.isSecHubServerAlive());
    }

    @Test
    public void getSecHubProjects_returns_project_data() {
        /* execute */
        List<ProjectData> projects = clientToTest.getSecHubProjects();

        /* test */
        assertNotNull(projects);
        assertEquals(1, projects.size());
        ProjectData project = projects.get(0);
        assertEquals("example-project", project.getProjectId());
        assertEquals("owner-id", project.getOwner().getUserId());
        assertTrue(project.getAssignedUsers().isEmpty());
        assertTrue(project.getEnabledProfileIds().isEmpty());
    }

    @Test
    public void getSecHubProjects_throws_runtime_exception_on_exception() {
        try {
            failingClient.getSecHubProjects();
            fail("Expected RuntimeException not thrown");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Failed to retrieve SecHub reports"));
        }
    }

    @Test
    public void isProjectIdDeprecated_returns_false_for_non_deprecated_project() {
        /* execute */
        boolean isDeprecated = clientToTest.isProjectIdDeprecated("example-project");

        /* test */
        assertFalse(isDeprecated);
    }

    @Test
    public void isProjectIdDeprecated_returns_true_for_deprecated_project() {
        /* execute */
        boolean isDeprecated = clientToTest.isProjectIdDeprecated("this-project-does-not-exist");

        /* test */
        assertTrue(isDeprecated);
    }

    /**
     * As of now this test is not executable due to a mismatch in jackson library dependencies between wiremock and intellij
     * plugin CE version 2023.1.1.
     */
    @Test
    @Ignore
    public void getSecHubJobPage_returns_job_page() {
        // do nothing
    }

    @Test
    public void getSecHubJobPage_throws_illegal_argument_exception_on_null_project_id() {
        try {
            clientToTest.getSecHubJobPage(null, 10, 0);
            fail("Expected IllegalArgumentException not thrown");
        } catch (NullPointerException e) {
            assertEquals("Parameter 'projectId' must not be null", e.getMessage());
        }
    }

    @Test
    public void getSecHubJobPage_throws_runtime_exception_on_exception() {
        try {
            failingClient.getSecHubJobPage("example-project", 10, 0);
            fail("Expected RuntimeException not thrown");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Failed to retrieve SecHub jobs for project: example-project"));
        }
    }

    /**
     * As of now this test is not executable due to a mismatch in jackson library dependencies between wiremock and intellij
     * plugin CE version 2023.1.1.
     */
    @Test
    public void getSecHubReport_returns_report() {
        // do nothing
    }

    @Test
    public void getSecHubReport_throws_null_pointer_exception_on_null_project_id() {
        try {
            clientToTest.getSecHubReport(null, null);
            fail("Expected NullPointerException not thrown");
        } catch (NullPointerException e) {
            assertEquals("Parameter 'projectId' must not be null", e.getMessage());
        }
    }

    @Test
    public void getSecHubReport_throws_null_pointer_exception_on_null_job_uuid() {
        try {
            clientToTest.getSecHubReport("example-project", null);
            fail("Expected NullPointerException not thrown");
        } catch (NullPointerException e) {
            assertEquals("Parameter 'jobUUID' must not be null", e.getMessage());
        }
    }

    @Test
    public void getFalsePositiveProjectConfiguration_returns_false_positive_configuration() {
        /* execute */
        FalsePositiveProjectConfiguration config = clientToTest.getFalsePositiveProjectConfiguration("example-project");

        /* test */
        assertNotNull(config);
        List<FalsePositiveEntry> falsePositives = config.getFalsePositives();
        assertNotNull(falsePositives);
        assertEquals(1, falsePositives.size());
        FalsePositiveJobData jobData = falsePositives.get(0).getJobData();
        assertNotNull(jobData);
        assertEquals(Integer.valueOf(1), jobData.getFindingId());
        assertEquals(UUID.fromString("117bbc23-309d-4ff3-b805-515b73c823fd"), jobData.getJobUUID());
        assertEquals("A fix has already been started - ", jobData.getComment());
    }

    @Test
    public void getFalsePositiveProjectConfiguration_throws_null_pointer_exception_on_null_project_id() {
        try {
            clientToTest.getFalsePositiveProjectConfiguration(null);
            fail("Expected NullPointerException not thrown");
        } catch (NullPointerException e) {
            assertEquals("Parameter 'projectId' must not be null", e.getMessage());
        }
    }
}