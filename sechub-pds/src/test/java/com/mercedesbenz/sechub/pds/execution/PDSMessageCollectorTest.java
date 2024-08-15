// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.test.TestFileWriter;

class PDSMessageCollectorTest {

    private PDSMessageCollector collectorToTest;

    @BeforeEach
    void beforeEach() {
        collectorToTest = new PDSMessageCollector();
    }

    @Test
    void when_folder_null_illegal_arg_is_thrown() {
        assertThrows(IllegalArgumentException.class, () -> collectorToTest.collect(null));
    }

    @Test
    void when_folder_is_empty_results_are_empty() throws Exception {
        /* prepare */
        File emptyTempDir = Files.createTempDirectory("pds_message_collector_empty").toFile();
        emptyTempDir.deleteOnExit();

        /* execute */
        List<SecHubMessage> result = collectorToTest.collect(emptyTempDir);

        /* test */
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /* @formatter:off */
    @ParameterizedTest
    @CsvSource({
        "INFO1.txt",
        "msg.txt",
        "something.adoc",
        "WARNING-no-underscore.txt",
        "ERROR-no-underscore.txt",
        "warning-no-underscore.txt",
        "error-no-underscore.txt",
        })
    /* @formatter:on */
    void when_folder_has_one_file_with_not_ERROR_or_WARNING_at_the_beginning_an_info_message_is_resolve(String fileName) throws Exception {
        /* prepare */
        File tempDir = Files.createTempDirectory("pds_message_collector_info_1").toFile();
        tempDir.deleteOnExit();

        String messageText = "I am a message!";
        TestFileWriter writer = new TestFileWriter();
        writer.writeTextToFile(new File(tempDir, fileName), messageText, false);

        /* execute */
        List<SecHubMessage> result = collectorToTest.collect(tempDir);

        /* test */
        assertNotNull(result);
        assertEquals(1, result.size());

        SecHubMessage secHubMessage1 = result.get(0);
        assertEquals(messageText, secHubMessage1.getText());
        assertEquals(SecHubMessageType.INFO, secHubMessage1.getType());
    }

    @ParameterizedTest
    @CsvSource({ "WARNING_1.txt", "WARNING_2_special.adoc", "WARNING_.txt", "warning_small_works_as_well.txt" })
    void when_folder_has_one_file_with_WARNING_at_the_beginning_a_warning_message_is_resolve(String fileName) throws Exception {
        /* prepare */
        File tempDir = Files.createTempDirectory("pds_message_collector_warning_1").toFile();
        tempDir.deleteOnExit();

        String messageText = "I am a warn message!";
        TestFileWriter writer = new TestFileWriter();
        writer.writeTextToFile(new File(tempDir, fileName), messageText, false);

        /* execute */
        List<SecHubMessage> result = collectorToTest.collect(tempDir);

        /* test */
        assertNotNull(result);
        assertEquals(1, result.size());

        SecHubMessage secHubMessage1 = result.get(0);
        assertEquals(messageText, secHubMessage1.getText());
        assertEquals(SecHubMessageType.WARNING, secHubMessage1.getType());
    }

    @ParameterizedTest
    @CsvSource({ "ERROR_1.txt", "ERROR_2_special.adoc", "ERROR_.txt", "error_small_works_as_well.txt" })
    void when_folder_has_one_file_with_ERROR_at_the_beginning_a_error_message_is_resolve(String fileName) throws Exception {
        /* prepare */
        File tempDir = Files.createTempDirectory("pds_message_collector_error_1").toFile();
        tempDir.deleteOnExit();

        String messageText = "I am an error message!";
        TestFileWriter writer = new TestFileWriter();
        writer.writeTextToFile(new File(tempDir, fileName), messageText, false);

        /* execute */
        List<SecHubMessage> result = collectorToTest.collect(tempDir);

        /* test */
        assertNotNull(result);
        assertEquals(1, result.size());

        SecHubMessage secHubMessage1 = result.get(0);
        assertEquals(messageText, secHubMessage1.getText());
        assertEquals(SecHubMessageType.ERROR, secHubMessage1.getType());
    }

}
