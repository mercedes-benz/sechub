// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PDSToolsCLITest {

    private PDSToolsCLI cliToTest;

    @BeforeEach
    void beforeEach() {
        cliToTest = new PDSToolsCLI();
        cliToTest.exitHandler = new TestExitHandler();
    }

    @Test
    void no_argument_fails_with_exit_code_1() throws Exception {

        TestExitException exception = assertThrows(TestExitException.class, () -> cliToTest.start(new String[] {}));

        assertEquals(1, exception.getExitCode());
    }

    @Test
    void unknown_command_fails_with_exit_code_2() throws Exception {

        TestExitException exception = assertThrows(TestExitException.class, () -> cliToTest.start(new String[] { "unknown" }));

        assertEquals(2, exception.getExitCode());
    }

    @Test
    void help_does_exit_with_0() throws Exception {

        TestExitException exception = assertThrows(TestExitException.class, () -> cliToTest.start(new String[] { "--help" }));

        assertEquals(0, exception.getExitCode());
    }

    @Test
    void generate_without_parameter_1_fails_with_exit_3() throws Exception {

        TestExitException exception = assertThrows(TestExitException.class, () -> cliToTest.start(new String[] { "generate" }));

        assertEquals(3, exception.getExitCode());
    }

    @Test
    void generate_without_parameter_2_fails_with_exit_3() throws Exception {

        TestExitException exception = assertThrows(TestExitException.class, () -> cliToTest.start(new String[] { "generate", "1" }));

        assertEquals(3, exception.getExitCode());
    }

    @Test
    void generate_with_config_path_and_scan_type_set_works() throws Exception {
        /* prepare */
        File testConfigFile = new File("./src/test/resources/test_codescan_example1.json");
        String scanType = "codeScan";
        File definedWorkingFolder = new File("./build/tmp/pds-tools/" + System.currentTimeMillis() + "_working_folder");
        Files.createDirectories(definedWorkingFolder.toPath());

        /* execute */
        cliToTest.start(new String[] { "generate", "-w", definedWorkingFolder.getAbsolutePath(), "-p", testConfigFile.getAbsolutePath(), "-s", scanType,
                "--createMissingFiles" });
    }

    @Test
    void generate_with_config_path_and_scan_type_and_target_folder_set_works() throws Exception {
        /* prepare */
        File testConfigFile = new File("./src/test/resources/test_codescan_example1.json");
        String scanType = "codeScan";

        long timestamp = System.currentTimeMillis();
        File definedTargetFolder = new File("./build/tmp/pds-tools/" + timestamp + "_target_folder");
        File definedWorkingFolder = new File("./build/tmp/pds-tools/" + timestamp + "_working_folder");
        Files.createDirectories(definedWorkingFolder.toPath());

        /* execute */
        cliToTest.start(new String[] { "generate", "--createMissingFiles", "-w", definedWorkingFolder.getAbsolutePath(), "-p", testConfigFile.getAbsolutePath(),
                "-s", scanType, "-t", definedTargetFolder.getAbsolutePath() });

        /* test */
        assertFileExists(definedTargetFolder, "binaries.tar");
        assertFileExists(definedTargetFolder, "sourcecode.zip");
        assertFileExists(definedTargetFolder, "pdsJobData.json");
        assertFileExists(definedTargetFolder, "original-used-sechub-configfile.json");
        assertFileExists(definedTargetFolder, "reducedSecHubJson_for_codeScan.json");
    }

    private void assertFileExists(File tmpFolder, String fileName) {
        File testFile = new File(tmpFolder, fileName);
        assertTrue(testFile.exists());
    }

}
