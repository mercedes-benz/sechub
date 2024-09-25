// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.test.TestFileReader;

class SecHubReportModelTest {

    @ParameterizedTest
    @ValueSource(strings = { "versionControl", "revision" })
    void empty_model_does_not_contain(String notContained) {
        /* prepare */
        SecHubReportModel model = new SecHubReportModel();

        /* execute */
        String json = model.toFormattedJSON();

        /* test */
        assertFalse(json.contains(notContained));
    }

    @Test
    void sechub_example_result_7_can_be_loaded_and_transformed_from_json() {
        /* prepare */
        String json = TestFileReader.readTextFromFile("src/test/resources/report/sechub-testreport7-secret-scan-with-revision-data-and-metadata.json");

        /* execute 1 - read */
        SecHubReportModel model = SecHubReportModel.fromJSONString(json);

        /* test 1 - read */
        List<SecHubFinding> findings = model.getResult().getFindings();
        assertEquals(2, findings.size());
        Iterator<SecHubFinding> iterator = findings.iterator();
        SecHubFinding finding1 = iterator.next();
        SecHubFinding finding2 = iterator.next();

        // finding 1 has revision information
        Optional<SecHubRevisionData> revisionOpt = finding1.getRevision();
        assertTrue(revisionOpt.isPresent());
        SecHubRevisionData revision = revisionOpt.get();

        assertEquals("66adf786209eaf01d9f26beb0a9e9fffdcf5f04b", revision.getId());

        // finding 2 has no revision in the example
        assertFalse(finding2.getRevision().isPresent());

    }

    @Test
    void sechub_example_result_6_can_be_loaded_and_transformed_from_json() {
        /* prepare */
        String json = TestFileReader.readTextFromFile("src/test/resources/report/sechub-testreport6-secret-scan-with-revision-data.json");

        /* execute */
        SecHubReportModel model = SecHubReportModel.fromJSONString(json);

        /* test */
        List<SecHubFinding> findings = model.getResult().getFindings();
        assertEquals(1, findings.size());
        SecHubFinding finding1 = findings.iterator().next();
        Optional<SecHubRevisionData> revisionOpt = finding1.getRevision();
        assertTrue(revisionOpt.isPresent());
        SecHubRevisionData revision = revisionOpt.get();

        assertEquals("66adf786209eaf01d9f26beb0a9e9fffdcf5f04b", revision.getId());

    }

    @Test
    void sechub_example_result_5_can_be_loaded_and_transformed_from_json() {
        /* prepare */
        String json = TestFileReader.readTextFromFile("src/test/resources/report/sechub-testreport5-version-control-data.json");

        /* execute */
        SecHubReportModel result = SecHubReportModel.fromJSONString(json);

        /* test */
        SecHubReportMetaData metaData = result.getMetaData().get();
        SecHubVersionControlData versionControl = metaData.getVersionControl().get();
        assertEquals("git", versionControl.getType());
        assertEquals("git@example.org:testuser/testrepo.git", versionControl.getLocation());
        assertEquals("57adf786209eaf01d9f26beb0a9e9fffdcf5f04b", versionControl.getRevision().get().getId());
    }

    @Test
    void sechub_example_result_4_can_be_loaded_and_transformed_from_json() {
        /* prepare */
        String json = TestFileReader.readTextFromFile("src/test/resources/report/sechub-testreport4-multiple-web-findings.json");

        /* execute */
        SecHubReportModel result = SecHubReportModel.fromJSONString(json);

        /* test */
        assertEquals(14, result.getResult().getFindings().size());
    }

    @Test
    void a_report_without_status_and_messages_can_be_read_and_when_traffic_light_is_red_status_is_failed() {
        /* prepare */
        String jsonNoStatusOrMessages = TestFileReader.readTextFromFile(new File("./src/test/resources/report/sechub-testreport3.json"), "\n");

        /* execute */
        SecHubReportModel report = SecHubReportModel.fromJSONString(jsonNoStatusOrMessages);

        /* test */
        assertNotNull(report);
        assertEquals(TrafficLight.RED, report.getTrafficLight());
        assertEquals(null, report.getStatus());

        Set<SecHubMessage> messages = report.getMessages();
        assertNotNull(messages);
        assertTrue(messages.isEmpty());
    }

    @Test
    void a_report_without_status_and_messages_can_be_read_and_when_traffic_light_is_green_status_is_ok() {
        /* prepare */
        String jsonNoStatusOrMessages = TestFileReader.readTextFromFile(new File("./src/test/resources/report/sechub-testreport1.json"), "\n");

        /* execute */
        SecHubReportModel report = SecHubReportModel.fromJSONString(jsonNoStatusOrMessages);

        /* test */
        assertNotNull(report);
        assertEquals(TrafficLight.GREEN, report.getTrafficLight());
        assertEquals(null, report.getStatus());

        Set<SecHubMessage> messages = report.getMessages();
        assertNotNull(messages);
        assertTrue(messages.isEmpty());
    }

    @Test
    void a_report_with_status_failed_and_messages_can_be_read() {
        /* prepare */
        String jsonWithStatusAndMessages = TestFileReader.readTextFromFile(new File("./src/test/resources/report/sechub-testreport2.json"), "\n");

        /* execute */
        SecHubReportModel report = SecHubReportModel.fromJSONString(jsonWithStatusAndMessages);

        /* test */
        assertNotNull(report);
        assertEquals(TrafficLight.GREEN, report.getTrafficLight());
        assertEquals(SecHubStatus.FAILED, report.getStatus());

        Set<SecHubMessage> messages = report.getMessages();
        assertNotNull(messages);
        assertFalse(messages.isEmpty());
        assertEquals(1, messages.size());
        SecHubMessage message = messages.iterator().next();

        assertEquals(SecHubMessageType.ERROR, message.getType());
        assertEquals("The code scan execution did fail!", message.getText());

    }

}
