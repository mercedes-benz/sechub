// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario4;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario4.Scenario4.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mercedesbenz.sechub.commons.core.RunOrFail;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;

public class SecHubConfigurationFailuresScenario4IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario4.class);

    @Test
    public void a_infra_scan_job_with_wrong_uri_inside_has_expected_error_message() {
        /* prepare */
        String illegalJson = "{\"apiVersion\":\"John Doe\",\"infraScan\":{\"uris\":[\"John Doe AND 1=1\"],\"ips\":[\"John Doe\"]},\"codeScan\":{\"fileSystem\":{\"folders\":[\"John Doe\"],\"files\":[\"John Doe\"]},\"use\":[\"John Doe\"]},\"webScan\":{\"maxScanDuration\":{\"duration\":1.2,\"unit\":\"John Doe\"},\"excludes\":[\"John Doe\"],\"includes\":[\"John Doe\"],\"login\":{\"form\":{\"script\":{\"pages\":[{\"actions\":[{\"unit\":\"John Doe\",\"description\":\"John Doe\",\"selector\":\"John Doe\",\"type\":\"John Doe\",\"value\":\"John Doe\"}]}]}},\"basic\":{\"password\":\"ZAP\",\"user\":\"John Doe\"},\"url\":\"https://www.example.com\"},\"url\":\"https://www.example.com\"}}";

        /* execute */
        expectHttpFailure(() -> as(USER_1).tryToCreateJobByJson(PROJECT_1, illegalJson), (statusException) -> {

            /* test */
            assertEquals(HttpStatus.BAD_REQUEST, statusException.getStatusCode());

            String message = statusException.getMessage();
            assertEquals(
                    "400 : \"JSON data failure at line: 1, column: 47. Cannot deserialize value of type `java.net.URI` from String \"John Doe AND 1=1\": not a valid textual representation, problem: Illegal character in path at index 4: John Doe AND 1=1\"",
                    message);
        });

    }

    @Test
    public void create_project_missing_api_version_fails_with_HTTP_400_having_json_with_details() throws Exception {
        /* prepare */
        SecHubScanConfiguration config = new SecHubScanConfiguration();

        /* execute + test */
        JsonNode json = assertFailedWithJsonOutput(HttpStatus.BAD_REQUEST, () -> as(USER_1).createJobAndReturnResultAsString(PROJECT_1, config));
        assertJsonContainsDetails(json, "Field 'apiVersion' with value 'null' was rejected. Api version is missing.");

    }

    @Test
    public void create_project_api_version_set_but_nothing_else() throws Exception {
        /* prepare */
        SecHubScanConfiguration config = new SecHubScanConfiguration();
        config.setApiVersion("1.0");

        /* execute + test */
        JsonNode json = assertFailedWithJsonOutput(HttpStatus.BAD_REQUEST, () -> as(USER_1).createJobAndReturnResultAsString(PROJECT_1, config));
        assertJsonContainsDetails(json, "Configuration does not contain any scan option. Unable to start scan!");

    }

    @Test
    public void create_project_api_version_set_but_labels_wrong() throws Exception {
        /* prepare */
        SecHubScanConfiguration config = new SecHubScanConfiguration();
        config.setApiVersion("1.0");
        SecHubCodeScanConfiguration codeScan = new SecHubCodeScanConfiguration();
        config.setCodeScan(codeScan);

        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
        config.setMetaData(metaData);
        metaData.getLabels().put("E xample1.1", "value");

        /* execute + test */
        JsonNode json = assertFailedWithJsonOutput(HttpStatus.BAD_REQUEST, () -> as(USER_1).createJobAndReturnResultAsString(PROJECT_1, config));
        assertJsonContainsDetails(json,
                "Meta data label key contains illegal characters. Label key 'E xample1.1' may only contain 'a-z','0-9', '-', '_' or '.' characters");

    }

    @Test
    public void unexpected_enum_values_result_in_null_by_custom_json_wrapper() {
        /* prepare */
        TestProject project = PROJECT_1;
        /* years is not a valid value for unit */
        String configuration = """
                {
                    "apiVersion": "1.0",
                    "webScan": {
                        "url": "https://demo.example.org",
                        "maxScanDuration": {
                			"duration" : 1,
                			"unit" : "years"
                		}
                    }
                }
                """;
        as(SUPER_ADMIN).updateWhiteListForProject(project, List.of("https://demo.example.org"));

        /* execute */
        UUID jobUUID = as(USER_1).createJobFromStringAndReturnJobUUID(project, configuration);

        /* test */
        assertNotNull(jobUUID);
    }

    private void assertJsonContainsDetails(JsonNode json, String... details) {
        ArrayNode detailsNode = (ArrayNode) json.get("details");

        int index = 0;

        for (String detail : details) {

            JsonNode detailNode = detailsNode.get(index);
            String detailText = detailNode.asText();
            if (!detailText.contains(detail)) {
                // use now assertEquals to have text compare editors inside IDE on test
                // runtime...
                assertEquals(detail, detailText);
            }

            index++;
        }

    }

    private JsonNode assertFailedWithJsonOutput(HttpStatus expectedStatus, RunOrFail<Exception> failable) throws Exception {
        try {
            failable.runOrFail();
            fail("Shall be not reachable - configuration was wrong. Must have 400 failure");

        } catch (HttpClientErrorException clientException) {
            assertEquals(expectedStatus, clientException.getStatusCode());
            String response = clientException.getResponseBodyAsString();
            try {
                JsonMapper mapper = JsonMapper.builder().build();
                return mapper.reader().readTree(response);
            } catch (Exception e) {
                fail("The response is not valid json:\n" + response);
            }
        }
        return null; // unnecessary, but otherwise compile error...
    }

}
