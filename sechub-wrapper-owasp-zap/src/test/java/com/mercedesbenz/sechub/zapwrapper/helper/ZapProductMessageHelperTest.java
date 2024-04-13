// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.rules.TemporaryFolder;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;

class ZapProductMessageHelperTest {

    private TemporaryFolder testFolder;
    private ZapProductMessageHelper helperToTest;

    @BeforeEach
    void beforeEach() throws IOException {
        testFolder = new TemporaryFolder();
        testFolder.create();
        helperToTest = new ZapProductMessageHelper(testFolder.getRoot().getAbsolutePath());
    }

    @AfterEach
    void afterEach() {
        testFolder.delete();
    }

    @Test
    void empty_list_results_in_no_messages_written() throws IOException {
        /* prepare */
        List<SecHubMessage> emptyList = new ArrayList<>();

        /* execute */
        helperToTest.writeProductMessages(emptyList);

        /* test */
        File[] files = testFolder.getRoot().listFiles();
        assertEquals(0, files.length, "No message file should be written!");
    }

    @Test
    void write_urls_as_user_messages_is_succesfull() throws IOException {
        /* prepare */
        List<SecHubMessage> list = new ArrayList<>();
        list.add(new SecHubMessage(SecHubMessageType.INFO, "https://localhost:3000/index.html"));
        list.add(new SecHubMessage(SecHubMessageType.INFO, "https://localhost:3000/login.html"));
        list.add(new SecHubMessage(SecHubMessageType.INFO, "https://localhost:3000/style.css"));
        list.add(new SecHubMessage(SecHubMessageType.INFO, "https://localhost:3000/main.js"));
        list.add(new SecHubMessage(SecHubMessageType.INFO, "https://localhost:3000/debug"));

        /* execute */
        helperToTest.writeProductMessages(list);

        /* test */
        File[] files = testFolder.getRoot().listFiles();
        assertEquals(list.size(), files.length, "Not every message has got its own file!");
    }

    @ParameterizedTest
    @EnumSource(mode = Mode.INCLUDE, value = ZapWrapperExitCode.class, names = { "UNSUPPORTED_CONFIGURATION", "PDS_CONFIGURATION_ERROR", "IO_ERROR" })
    @NullSource
    void internal_configuration_errors_do_not_write_messages(ZapWrapperExitCode exitCode) throws IOException {
        /* prepare */
        ZapWrapperRuntimeException exception = new ZapWrapperRuntimeException("empty", exitCode);

        /* execute */
        helperToTest.writeProductError(exception);

        /* test */
        File[] files = testFolder.getRoot().listFiles();
        assertEquals(0, files.length, "No files should be created!");
    }

    @ParameterizedTest
    @EnumSource(mode = Mode.INCLUDE, value = ZapWrapperExitCode.class, names = { "TARGET_URL_NOT_REACHABLE", "API_DEFINITION_CONFIG_INVALID",
            "TARGET_URL_INVALID", "PRODUCT_EXECUTION_ERROR" })
    void errors_are_written_as_product_messages(ZapWrapperExitCode exitCode) throws IOException {
        /* prepare */
        ZapWrapperRuntimeException exception = new ZapWrapperRuntimeException("empty", exitCode);

        /* execute */
        helperToTest.writeProductError(exception);

        /* test */
        File[] files = testFolder.getRoot().listFiles();
        assertEquals(1, files.length, "Only one file should be created for each exception!");

        for (File file : files) {
            verifyMessageFileContent(file, exitCode);
        }
    }

    private void verifyMessageFileContent(File file, ZapWrapperExitCode exitCode) throws IOException {
        String messageContent = Files.readString(file.toPath());
        String errorMessage = "Product message had unexpected content!";
        switch (exitCode) {
        case TARGET_URL_NOT_REACHABLE:
            assertEquals("Target URL not reachable. Please check if the target URL, specified inside SecHub configuration, is reachable.", messageContent,
                    errorMessage);
            break;
        case API_DEFINITION_CONFIG_INVALID:
            assertEquals(
                    "Only a single API file can be provided. Please use a single file for the API definition inside the filesystem->files section of the SecHub configuration.",
                    messageContent, errorMessage);
            break;
        case TARGET_URL_INVALID:
            assertEquals("Target URL invalid. The target URL, specified inside SecHub configuration, is not a valid URL.", messageContent, errorMessage);
            break;
        case PRODUCT_EXECUTION_ERROR:
            assertEquals("Product error. The DAST scanner ZAP ended with a product error.", messageContent, errorMessage);
            break;
        default:
            fail("Unsupported ZapWrapperExitCode, this should never occur!");
            break;
        }

    }

}
