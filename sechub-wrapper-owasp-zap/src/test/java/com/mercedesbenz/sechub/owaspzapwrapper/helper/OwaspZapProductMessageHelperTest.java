package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.rules.TemporaryFolder;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperRuntimeException;

class OwaspZapProductMessageHelperTest {

    private TemporaryFolder testFolder;
    private OwaspZapProductMessageHelper helperToTest;

    @BeforeEach
    void beforeEach() throws IOException {
        testFolder = new TemporaryFolder();
        testFolder.create();
        helperToTest = new OwaspZapProductMessageHelper(testFolder.getRoot().getAbsolutePath());
    }

    @AfterEach
    void afterEach() {
        testFolder.delete();
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
            verifyMessageContent(file, exitCode);
        }
    }

    private void verifyMessageContent(File file, ZapWrapperExitCode exitCode) throws IOException {
        String messageContent = Files.readString(file.toPath());
        String errorMessage = "Product message had unexpected content!";
        switch (exitCode) {
        case TARGET_URL_NOT_REACHABLE:
            assertEquals("Target url specified inside sechub config json was not reachable.", messageContent, errorMessage);
            break;
        case API_DEFINITION_CONFIG_INVALID:
            assertEquals("The sechub config json was invalid. Please use a single file for API definitions inside the filesystem->files part.", messageContent,
                    errorMessage);
            break;
        case TARGET_URL_INVALID:
            assertEquals("Target url specified inside sechub config json was not a valid URL.", messageContent, errorMessage);
            break;
        case PRODUCT_EXECUTION_ERROR:
            assertEquals("The DAST scanner OWASP ZAP ended because of a product error.", messageContent, errorMessage);
            break;
        default:
            fail("Unsupported ZapWrapperExitCode, this should never occur!");
            break;
        }

    }

}
