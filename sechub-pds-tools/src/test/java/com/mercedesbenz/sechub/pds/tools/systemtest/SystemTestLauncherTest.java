// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools.systemtest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.pds.tools.SystemTestCommand;
import com.mercedesbenz.sechub.systemtest.SystemTestAPI;
import com.mercedesbenz.sechub.systemtest.SystemTestParameters;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;

class SystemTestLauncherTest {

    private SystemTestLauncher launcherToTest;
    private SystemTestCommand command;
    private SystemTestAPI systemTestApi;

    @BeforeEach
    void beforeEach() {
        launcherToTest = new SystemTestLauncher();

        systemTestApi = mock(SystemTestAPI.class);
        command = mock(SystemTestCommand.class);

        // connect mocks
        launcherToTest.systemTestApi = systemTestApi;
    }

    @Test
    void new_launcher_has_all_parts_defined() throws Exception {
        /* execute */
        launcherToTest = new SystemTestLauncher();

        /* test */
        assertNotNull(launcherToTest.systemTestApi);
    }

    @Test
    void no_config_file_defined_throws_illegal_argument_without_interactions() throws Exception {

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> launcherToTest.launch(command));

        /* test */
        assertTrue(exception.getMessage().contains("config file not defined"));
        verifyNoInteractions(systemTestApi);
    }

    @Test
    void result_is_returned_from_sytemtestapi() throws Exception {
        /* prepare */
        String path = "./src/test/resources/systemtest/systemtest_example1.json";
        SystemTestResult resultFromApiCall = new SystemTestResult();
        when(systemTestApi.runSystemTests(any())).thenReturn(resultFromApiCall);
        when(command.getPathToConfigFile()).thenReturn(path);

        /* execute */
        SystemTestResult result = launcherToTest.launch(command);

        /* test */
        assertSame(resultFromApiCall, result);

    }

    @Test
    void system_test_parameters_are_build_from_command_as_expected__tests_to_run_set() throws Exception {

        /* prepare */
        String testAdditionaResourcesPath = "test-additional-resources-path";
        String testPathToPDSSolution = "test-pds-solution-path";
        String testPathToSecHub = "test-sechub-path";
        String testPathToWorkspace = "test-workspace-path";

        String path = "./src/test/resources/systemtest/systemtest_example1.json";
        String expectedPrettyJson = loadJsonAsPrettyPrinted(path);
        List<String> testsToRun = Arrays.asList(new String[] { "testA", "testB" });

        when(command.getPathToConfigFile()).thenReturn(path);
        when(command.getAdditionalResourcesFolder()).thenReturn(testAdditionaResourcesPath);
        when(command.getPdsSolutionsRootFolder()).thenReturn(testPathToPDSSolution);
        when(command.getSecHubSolutionRootFolder()).thenReturn(testPathToSecHub);
        when(command.getWorkspaceFolder()).thenReturn(testPathToWorkspace);
        when(command.getTestsToRun()).thenReturn(testsToRun);

        /* execute */
        launcherToTest.launch(command);

        /* test */
        ArgumentCaptor<SystemTestParameters> paramCaptor = ArgumentCaptor.forClass(SystemTestParameters.class);
        verify(systemTestApi).runSystemTests(paramCaptor.capture());

        SystemTestParameters parameters = paramCaptor.getValue();
        assertTrue(parameters.getTestsToRun().contains("testA"));
        assertTrue(parameters.getTestsToRun().contains("testB"));

        // test configuration is loaded and available
        SystemTestConfiguration parametersConfiguration = parameters.getConfiguration();
        String prettyJson = JSONConverter.get().toJSON(parametersConfiguration, true);
        assertEquals(expectedPrettyJson, prettyJson);

        assertEquals(testAdditionaResourcesPath, parameters.getPathToAdditionalResources());
        assertEquals(testPathToPDSSolution, parameters.getPathToPdsSolutionsRootFolder());
        assertEquals(testPathToSecHub, parameters.getPathToSechubSolutionRootFolder());
        assertEquals(testPathToWorkspace, parameters.getPathToWorkspace());

    }

    @Test
    void system_test_parameters_are_build_from_command_as_expected__no_tests_to_run_set() throws Exception {

        /* prepare */
        String testAdditionaResourcesPath = "test-additional-resources-path";
        String testPathToPDSSolution = "test-pds-solution-path";
        String testPathToSecHub = "test-sechub-path";
        String testPathToWorkspace = "test-workspace-path";

        String path = "./src/test/resources/systemtest/systemtest_example1.json";
        String expectedPrettyJson = loadJsonAsPrettyPrinted(path);

        when(command.getPathToConfigFile()).thenReturn(path);
        when(command.getAdditionalResourcesFolder()).thenReturn(testAdditionaResourcesPath);
        when(command.getPdsSolutionsRootFolder()).thenReturn(testPathToPDSSolution);
        when(command.getSecHubSolutionRootFolder()).thenReturn(testPathToSecHub);
        when(command.getWorkspaceFolder()).thenReturn(testPathToWorkspace);

        /* execute */
        launcherToTest.launch(command);

        /* test */
        ArgumentCaptor<SystemTestParameters> paramCaptor = ArgumentCaptor.forClass(SystemTestParameters.class);
        verify(systemTestApi).runSystemTests(paramCaptor.capture());
        SystemTestParameters parameters = paramCaptor.getValue();
        assertTrue(parameters.getTestsToRun().isEmpty());

        // test configuration is loaded and available
        SystemTestConfiguration parametersConfiguration = parameters.getConfiguration();
        String prettyJson = JSONConverter.get().toJSON(parametersConfiguration, true);
        assertEquals(expectedPrettyJson, prettyJson);

        assertEquals(testAdditionaResourcesPath, parameters.getPathToAdditionalResources());
        assertEquals(testPathToPDSSolution, parameters.getPathToPdsSolutionsRootFolder());
        assertEquals(testPathToSecHub, parameters.getPathToSechubSolutionRootFolder());
        assertEquals(testPathToWorkspace, parameters.getPathToWorkspace());

    }

    private String loadJsonAsPrettyPrinted(String path) throws IOException {
        TextFileReader reader = new TextFileReader();
        String json = reader.loadTextFile(new File(path));
        SystemTestConfiguration expectedConfiguration = JSONConverter.get().fromJSON(SystemTestConfiguration.class, json);
        String expectedPrettyJson = JSONConverter.get().toJSON(expectedConfiguration, true);
        return expectedPrettyJson;
    }

}
