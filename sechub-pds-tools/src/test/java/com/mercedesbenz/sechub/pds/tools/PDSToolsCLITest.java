// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.pds.tools.handler.ConsoleHandler;
import com.mercedesbenz.sechub.pds.tools.handler.ExitHandler;
import com.mercedesbenz.sechub.pds.tools.systemtest.SystemTestLauncher;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRunResult;
import com.mercedesbenz.sechub.systemtest.runtime.error.SystemTestFailure;

class PDSToolsCLITest {

    private PDSToolsCLI cliToTest;
    private SystemTestLauncher systemTestLauncher;
    private ExitHandler exitHandler;
    private ConsoleHandler consoleHandler;

    @BeforeEach
    void beforeEach() {
        cliToTest = new PDSToolsCLI();

        exitHandler = new TestExitHandler();
        consoleHandler = mock(ConsoleHandler.class);
        systemTestLauncher = mock(SystemTestLauncher.class);

        cliToTest.exitHandler = exitHandler;
        cliToTest.consoleHandler = consoleHandler;
        cliToTest.systemTestLauncher = systemTestLauncher;
    }

    @Test
    void a_normal_instanciated_cli_object_has_all_parts_inside() {
        /* execute */
        cliToTest = new PDSToolsCLI();

        /* test */
        assertNotNull(cliToTest.consoleHandler);
        assertNotNull(cliToTest.exitHandler);
        assertNotNull(cliToTest.systemTestLauncher);
    }

    @Test
    void no_argument_fails_with_exit_code_1() throws Exception {

        /* execute */
        TestExitException exception = assertThrows(TestExitException.class, () -> cliToTest.start(new String[] {}));

        /* test */
        assertEquals(1, exception.getExitCode());
        verify(systemTestLauncher, never()).launch(any());
    }

    @Test
    void unknown_command_fails_with_exit_code_2() throws Exception {

        /* execute */
        TestExitException exception = assertThrows(TestExitException.class, () -> cliToTest.start(new String[] { "unknown" }));

        /* test */
        assertEquals(2, exception.getExitCode());
        verify(systemTestLauncher, never()).launch(any());
    }

    @Test
    void help_does_exit_with_0() throws Exception {

        /* execute */
        TestExitException exception = assertThrows(TestExitException.class, () -> cliToTest.start(new String[] { "--help" }));

        /* test */
        assertEquals(0, exception.getExitCode());
        verify(systemTestLauncher, never()).launch(any());
    }

    @Test
    void generate_without_parameter_1_fails_with_exit_3() throws Exception {

        /* execute */
        TestExitException exception = assertThrows(TestExitException.class, () -> cliToTest.start(new String[] { "generate" }));

        /* test */
        assertEquals(3, exception.getExitCode());
        verify(systemTestLauncher, never()).launch(any());
    }

    @Test
    void generate_without_parameter_2_fails_with_exit_3() throws Exception {

        /* execute */
        TestExitException exception = assertThrows(TestExitException.class, () -> cliToTest.start(new String[] { "generate", "1" }));

        /* test */
        assertEquals(3, exception.getExitCode());
        verify(systemTestLauncher, never()).launch(any());
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

        /* test */
        verify(systemTestLauncher, never()).launch(any());
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

        verify(systemTestLauncher, never()).launch(any());
    }

    @Test
    void systemtest_does_call_systemtest_launcher_with_correct_paramters_1() throws Exception {

        /* prepare */
        SystemTestResult result = new SystemTestResult();
        when(cliToTest.systemTestLauncher.launch(any())).thenReturn(result);

        /* execute */
        cliToTest.start(new String[] { "systemtest", "--dry", "--file", "./testfile.json" });

        /* test */
        ArgumentCaptor<SystemTestCommand> captor = ArgumentCaptor.forClass(SystemTestCommand.class);
        verify(systemTestLauncher).launch(captor.capture());

        SystemTestCommand systemTestCommand = captor.getValue();
        assertEquals(null, systemTestCommand.getAdditionalResourcesFolder());
        assertEquals("./testfile.json", systemTestCommand.getPathToConfigFile());
        assertEquals(null, systemTestCommand.getPdsSolutionsRootFolder());
        assertEquals(null, systemTestCommand.getSecHubSolutionRootFolder());
        assertEquals(true, systemTestCommand.isDryRun());
        assertEquals(false, systemTestCommand.isRemoteRun());
        assertEquals(null, systemTestCommand.getWorkspaceFolder());
        assertTrue(systemTestCommand.getTestsToRun().isEmpty());

    }

    @Test
    void systemtest_does_call_systemtest_launcher_with_correct_paramters_2() throws Exception {
        /* prepare */
        SystemTestResult result = new SystemTestResult();
        when(cliToTest.systemTestLauncher.launch(any())).thenReturn(result);

        /* execute */
        cliToTest.start(new String[] { "systemtest", "--workspace-rootfolder", "/path/to/workspace", "--file", "/absolute/testfile.json",
                "--additional-resources-folder", "./additionalResources/path", "--pds-solutions-rootfolder", "/path/to/pds-solution",
                "--sechub-solution-rootfolder", "/path/to/sechub-solution", "--run-tests", "test1,test2" });

        /* test */
        ArgumentCaptor<SystemTestCommand> captor = ArgumentCaptor.forClass(SystemTestCommand.class);
        verify(systemTestLauncher).launch(captor.capture());

        SystemTestCommand systemTestCommand = captor.getValue();
        assertEquals("./additionalResources/path", systemTestCommand.getAdditionalResourcesFolder());
        assertEquals("/absolute/testfile.json", systemTestCommand.getPathToConfigFile());
        assertEquals("/path/to/pds-solution", systemTestCommand.getPdsSolutionsRootFolder());
        assertEquals("/path/to/sechub-solution", systemTestCommand.getSecHubSolutionRootFolder());
        assertEquals(false, systemTestCommand.isDryRun());
        assertEquals(false, systemTestCommand.isRemoteRun());
        assertEquals("/path/to/workspace", systemTestCommand.getWorkspaceFolder());
        assertEquals(2, systemTestCommand.getTestsToRun().size());
        assertTrue(systemTestCommand.getTestsToRun().contains("test1"));
        assertTrue(systemTestCommand.getTestsToRun().contains("test2"));

    }

    @Test
    void failing_system_test_results_in_exit_1_and_error_output_with_message_and_details() throws Exception {
        /* prepare */
        SystemTestResult result = new SystemTestResult();
        when(cliToTest.systemTestLauncher.launch(any())).thenReturn(result);
        SystemTestRunResult runResult1 = mock(SystemTestRunResult.class);
        when(runResult1.hasFailed()).thenReturn(true);
        SystemTestFailure failure = new SystemTestFailure();
        failure.setDetails("detail1");
        failure.setMessage("message1");
        when(runResult1.getFailure()).thenReturn(failure);
        result.getRuns().add(runResult1);

        // here we use a mocked exit handler
        exitHandler = mock(ExitHandler.class);
        cliToTest.exitHandler = exitHandler;

        /* execute */
        cliToTest.start(new String[] { "systemtest", "--workspace-rootfolder", "/path/to/workspace", "--file", "/absolute/testfile.json",
                "--additional-resources-folder", "./additionalResources/path", "--pds-solutions-rootfolder", "/path/to/pds-solution",
                "--sechub-solution-rootfolder", "/path/to/sechub-solution" });

        /* test */
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(exitHandler).exit(1);
        verify(consoleHandler).error(captor.capture());
        String errorMessage = captor.getValue();
        assertTrue(errorMessage.contains("detail1"));
        assertTrue(errorMessage.contains("message1"));

    }

    private void assertFileExists(File tmpFolder, String fileName) {
        File testFile = new File(tmpFolder, fileName);
        assertTrue(testFile.exists());
    }

}
