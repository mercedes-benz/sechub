// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest;

import static com.mercedesbenz.sechub.systemtest.SystemTestAPI.*;
import static com.mercedesbenz.sechub.test.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.LocalSetupDefinition;
import com.mercedesbenz.sechub.systemtest.config.ProjectDefinition;
import com.mercedesbenz.sechub.systemtest.config.RuntimeVariable;
import com.mercedesbenz.sechub.systemtest.config.SecHubConfigurationDefinition;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestResult;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRunResult;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeException;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ProcessContainerFailedException;
import com.mercedesbenz.sechub.test.TestFileReader;

/**
 * A test if the system test API and the involved runtime + configuration
 * builder can work together and execute real (but simple fake) bash scripts.
 *
 * @author Albert Tregnaghi
 *
 */
class SystemTestDryRunTest {
    private static final String FAKED_PDS_SOLUTIONS_PATH = "./src/test/resources/fake-root/sechub-pds-solutions";
    private static final String ADDITIONAL_RESOURCES_PATH = "./src/test/resources/additional-resources";
    private static final Logger LOG = LoggerFactory.getLogger(SystemTestDryRunTest.class);
    private static final String PREPARE_TEST1_OUPTUT_FILE_NAME = "output-prepare-test1.txt";

    private SystemTestAPI systemTestApi;

    @BeforeEach
    void beforeEach(TestInfo info) {
        systemTestApi = new SystemTestAPI();

        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
        LOG.info("Systemtest: {}", info.getDisplayName());
        LOG.info("--------------------------------------------------------------------------------------------------------------------------------");
    }

    @Test
    void faked_xray_can_be_executed_without_errors() throws IOException {
        String path = "./src/test/resources/systemtest_xray_licensescan_example.json";
        String json = TestFileReader.loadTextFile(path);

        SystemTestConfiguration configuration = JSONConverter.get().fromJSON(SystemTestConfiguration.class, json);

        /* @formatter:off */

        /* execute */
        SystemTestResult result = systemTestApi.
                    runSystemTests(params().
                                        localRun().
                                        dryRun().
                                        testConfiguration(configuration).
                                        additionalResourcesPath(ADDITIONAL_RESOURCES_PATH).
                                        pdsSolutionPath(FAKED_PDS_SOLUTIONS_PATH).
                                    build());
        /* @formatter:on */

        /* test */
        if (result.hasFailedTests()) {
            fail("The execution failed:" + result.toString());
        }
    }

    @Test
    void faked_webscan_can_be_executed_without_errors_and_contains_expected_data_in_configuration() throws IOException {

        /* @formatter:off */

        /* prepare*/
        SystemTestConfiguration configuration = configure().
                localSetup().
                    secHub().
                        project().
                            addURItoWhiteList("https://example.com/app-to-test").
                        endProject().
                    endSecHub().
                endLocalSetup().
                test("at-least-one-testentry").
                    runSecHubJob().
                        secretScan().endScan().
                        uploads().addBinaryUploadWithDefaultRef("/path").
                        endUploads().
                    endRunSecHub().
                endTest().
                build();

        /* execute */
        SystemTestResult result = systemTestApi.
                    runSystemTests(params().
                                        localRun().
                                        dryRun().
                                        testConfiguration(configuration).
                                        additionalResourcesPath(ADDITIONAL_RESOURCES_PATH).
                                        pdsSolutionPath(FAKED_PDS_SOLUTIONS_PATH).
                                    build());
        /* @formatter:on */

        /* test */
        if (result.hasFailedTests()) {
            fail("The execution failed:" + result.toString());
        }

        Optional<LocalSetupDefinition> localSetup = configuration.getSetup().getLocal();
        SecHubConfigurationDefinition configure = localSetup.get().getSecHub().getConfigure();
        Optional<List<ProjectDefinition>> projectsOpt = configure.getProjects();

        List<ProjectDefinition> projects = projectsOpt.get();
        assertEquals(1, projects.size());
        ProjectDefinition project = projects.iterator().next();
        assertTrue(project.getWhitelistedURIs().contains("https://example.com/app-to-test"));

    }

    @Test
    void faked_gosec_can_be_executed_without_errors() throws IOException {
        /* @formatter:off */

        Path secHubStartOutputFile = createTempFileInBuildFolder("faked_gosec_sechub_start_output_file.txt");
        Path goSecStartOutputFile = createTempFileInBuildFolder("faked_gosec_pds_start_output_file.txt");
        Path goSecStopOutputFile = createTempFileInBuildFolder("faked_gosec_pds_stop_output_file.txt");
        Path secHubStopOutputFile = createTempFileInBuildFolder("faked_gosec_sechub_stop_output_file.txt");

        String var_Text = "nano_"+System.nanoTime();

        /* prepare */
        SystemTestConfiguration configuration = configure().

                addVariable("var_text",var_Text).
                addVariable("var_number","2").
                addVariable("test_var_number","${variables.var_number}_should_be_2").
                addVariable("test_env_path","${env.PATH}").
                addVariable("a-secret-example","${secretEnv.PATH}").

                localSetup().
                    secHub().
                        addStartStep().
                            script().
                                envVariable("TEST_NUMBER_LIST", "${variables.test_var_number}").
                                path("./01-start-single-docker-compose.sh").
                                arguments(secHubStartOutputFile.toString()).
                                process().
                                    markStageWaits(). // we use this to ensure output can be done by script at setup stage
                                endProcess().
                            endScript().
                        endStep().

                        addStopStep().
                            script().
                                envVariable("Y_TEST", "testy").
                                path("./01-stop-single-docker-compose.sh").
                                arguments(secHubStopOutputFile.toString(),"second","third-as:${variables.var_text}").
                                process().
                                    markStageWaits(). // we use this to ensure output can be done by script at setup stage
                                endProcess().
                            endScript().
                        endStep().

                        configure().
                            addExecutor().
                                pdsProductId("PDS_GOSEC").
                            endExecutor().
                        endConfigure().

                    endSecHub().

                    addSolution("faked-gosec").
                        addStartStep().
                            script().
                                process().
                                    markStageWaits(). // we use this to ensure output can be done by script at setup stage
                                    withTimeOut(3, TimeUnit.SECONDS).
                                endProcess().

                                envVariable("A_TEST1", "value1").
                                envVariable("B_TEST2", "value2").
                                envVariable("C_test_var_number_added", "${variables.test_var_number}").
                                envVariable("D_RESOLVED_SECRET","${variables.a-secret-example}").
                                path("./05-start-single-sechub-network-docker-compose.sh").
                                arguments(goSecStartOutputFile.toString(),"secondCallIsForPDS","third-as:${secretEnv.PATH}_may_not_be_resolved_because_only_script_env_can_contain_this").

                            endScript().
                        endStep().
                        addStopStep().
                            script().
                                process().
                                    markStageWaits(). // we use this to ensure output can be done by script at setup stage
                                    withTimeOut(3, TimeUnit.SECONDS).
                                endProcess().
                                envVariable("X_TEST", "testx").
                                path("./05-stop-single-sechub-network-docker-compose.sh").
                                arguments(goSecStopOutputFile.toString(),"second","third-as:${variables.var_text}").
                                workingDir("./").
                            endScript().
                        endStep().

                    endSolution().

                endLocalSetup().

                test("test1").
                    prepareStep().
                        script().
                            workingDir(RuntimeVariable.ADDITIONAL_RESOURCES_FOLDER.asExpression()).
                            path("./preparation/prepare-test1.sh").
                            arguments("${runtime."+RuntimeVariable.CURRENT_TEST_FOLDER.getVariableName()+"}/"+PREPARE_TEST1_OUPTUT_FILE_NAME).
                            process().
                                markStageWaits().
                                withTimeOut(20, TimeUnit.SECONDS).
                            endProcess().
                        endScript().
                    endStep().
                    runSecHubJob().
                        webScan().
                            url(URI.create("https://example.com")).
                        endScan().
                        uploads().

                        endUploads().
                    endRunSecHub().
                endTest().

                build();


        /* execute */
        Path tempWorkspaceFolder = createTempDirectoryInBuildFolder("systemtest_inttest/faked_gosec_can_be_executed_without_errors");
        SystemTestResult result = systemTestApi.runSystemTests(
                params().
                    localRun().
                    dryRun().
                    workspacePath(tempWorkspaceFolder.toString()).
                    testConfiguration(configuration).
                    additionalResourcesPath(ADDITIONAL_RESOURCES_PATH).
                    pdsSolutionPath(FAKED_PDS_SOLUTIONS_PATH).
                build()
         );

        /* test */
        if (result.hasFailedTests()) {
            fail("The execution failed:"+result.toString());
        }
        // we now check that all test output was written by our test scripts to files
        String sechubStartOutputData = TestFileReader.loadTextFile(secHubStartOutputFile);
        assertEquals("sechub-started and TEST_NUMBER_LIST=2_should_be_2", sechubStartOutputData);

        // special case: inside this script we wait some time before the output is done
        // means: we can test if waitForStage information is correct handled by framework
        String gosecStartOutputData = TestFileReader.loadTextFile(goSecStartOutputFile);
        assertEquals("gosec-started with param2=secondCallIsForPDS and C_test_var_number_added=2_should_be_2, B_TEST2=value2, D_RESOLVED_SECRET is like path=true, parameter3 is still a secret=true", gosecStartOutputData);

        String sechubStopOutputData = TestFileReader.loadTextFile(secHubStopOutputFile);
        assertEquals("sechub-stopped with param2=second and parm3=third-as:"+var_Text+" and Y_TEST=testy", sechubStopOutputData);

        String gosecStopOutputData = TestFileReader.loadTextFile(goSecStopOutputFile);
        assertEquals("gosec-stopped with param2=second and parm3=third-as:"+var_Text+" and X_TEST=testx", gosecStopOutputData);

        // Here we check if the test1 output file preparation was written to test folder
        Path preparationOutputFile = tempWorkspaceFolder.resolve("tests/test1/"+PREPARE_TEST1_OUPTUT_FILE_NAME);
        String preparationOutputFileContent = TestFileReader.loadTextFile(preparationOutputFile);

        assertEquals("Output from prepare-test1.sh", preparationOutputFileContent);

        /* @formatter:on */
    }

    @Test
    void faked_test_can_be_executed_when_testsToRun_not_defined() throws IOException {
        /* @formatter:off */

        /* prepare */
        SystemTestConfiguration configuration = configure().

                localSetup().
                endLocalSetup().

                test("correct-testname").
                    runSecHubJob().
                        webScan().
                            url(URI.create("https://example.com")).
                        endScan().
                        uploads().

                        endUploads().
                    endRunSecHub().
                endTest().

                build();


        /* execute */
        SystemTestResult result = systemTestApi.runSystemTests(
                params().
                    localRun().
                    dryRun().
                    testConfiguration(configuration).
                build()
         );

        /* test */
        if (result.hasFailedTests()) {
            fail("The execution failed:"+result.toString());
        }

        /* @formatter:on */
    }

    @Test
    void faked_test_can_be_executed_when_testsToRun_contains_correct_testname() throws IOException {
        /* @formatter:off */

        /* prepare */
        SystemTestConfiguration configuration = configure().

                localSetup().
                endLocalSetup().

                test("correct-testname").
                    runSecHubJob().
                        webScan().
                            url(URI.create("https://example.com")).
                        endScan().
                        uploads().

                        endUploads().
                    endRunSecHub().
                endTest().

                build();


        /* execute */
        SystemTestResult result = systemTestApi.runSystemTests(
                params().
                localRun().
                dryRun().
                testsToRun("correct-testname").
                testConfiguration(configuration).
                build()
                );

        /* test */
        if (result.hasFailedTests()) {
            fail("The execution failed:"+result.toString());
        }

        /* @formatter:on */
    }

    @Test
    void faked_test_cannot_be_executed_when_wrong_test_name_used_for_runtests() throws IOException {
        /* @formatter:off */

        /* prepare */
        SystemTestConfiguration configuration = configure().

                localSetup().
                endLocalSetup().

                test("correct-testname").
                    runSecHubJob().
                        webScan().
                        url(URI.create("https://example.com")).
                        endScan().
                        uploads().

                        endUploads().
                    endRunSecHub().
                endTest().

                build();


        /* execute */
        SystemTestResult result = systemTestApi.runSystemTests(
                params().
                localRun().
                dryRun().
                testsToRun("wrong-testname").
                testConfiguration(configuration).
                build()
                );

        /* test */
        if (!result.hasFailedTests()) {
            fail("The execution has not failed:"+result.toString());
        }
        Set<SystemTestRunResult> runs = result.getRuns();
        assertEquals(1, runs.size());
        Iterator<SystemTestRunResult> iterator = runs.iterator();

        // check test result
        SystemTestRunResult run1 = iterator.next();
        assertEquals("Test 'wrong-testname' is not defined!", run1.getFailure().getMessage());

        // check problem
        assertTrue(result.hasProblems());
        Set<String> problems = result.getProblems();
        assertEquals(1, problems.size());
        String problem1 = problems.iterator().next();
        assertEquals("No tests were executed (0/1)", problem1);

        /* @formatter:on */
    }

    @Test
    void faked_test_cannot_be_executed_when_one_correct_and_one_wrong_test_name_used_for_runtests() throws IOException {
        /* @formatter:off */

        /* prepare */
        SystemTestConfiguration configuration = configure().

                localSetup().
                endLocalSetup().

                test("correct-testname").
                runSecHubJob().
                webScan().
                url(URI.create("https://example.com")).
                endScan().
                uploads().

                endUploads().
                endRunSecHub().
                endTest().

                build();


        /* execute */
        SystemTestResult result = systemTestApi.runSystemTests(
                params().
                localRun().
                dryRun().
                testsToRun("correct-testname", "wrong-testname").
                testConfiguration(configuration).
                build()
                );

        /* test */
        if (!result.hasFailedTests()) {
            fail("The execution has not failed:"+result.toString());
        }
        Set<SystemTestRunResult> runs = result.getRuns();
        assertEquals(2, runs.size());
        Iterator<SystemTestRunResult> iterator = runs.iterator();

        SystemTestRunResult run1 = iterator.next();
        assertFalse(run1.hasFailed());

        SystemTestRunResult run2 = iterator.next();
        assertTrue(run2.hasFailed());
        assertEquals("Test 'wrong-testname' is not defined!", run2.getFailure().getMessage());

        /* @formatter:on */
    }

    @Test
    void fail_because_unknown_runtime_variable() {
        /* @formatter:off */

        /* prepare */
        SystemTestConfiguration configuration = configure().
                localSetup().
                    addSolution("faked-fail_on_start").
                        addStartStep().script().path("./05-start-single-sechub-network-docker-compose.sh").arguments("${runtime.unknown_must_fail}").endScript().endStep().
                        addStopStep().script().path("./05-stop-single-sechub-network-docker-compose.sh").endScript().endStep().
                    endSolution().
                endLocalSetup().
                build();

        LOG.info("loaded config=\n{}", JSONConverter.get().toJSON(configuration,true));

        /* execute */
        SystemTestRuntimeException exception = assertThrows(SystemTestRuntimeException.class, ()->systemTestApi.runSystemTests(
            params().
                localRun().
                workspacePath(createTempDirectoryInBuildFolder("systemtest_inttest/fail_because_unknown_runtime_variable").toString()).
                testConfiguration(configuration).
                pdsSolutionPath(FAKED_PDS_SOLUTIONS_PATH).
            build()));

        /* test */
        String message = exception.getMessage();
        assertTrue(message.contains("'runtime.unknown_must_fail' is not defined!"));
        // test proposals are inside error message:
        assertTrue(message.contains("Allowed variables for type RUNTIME_VARIABLES are:"));
        assertTrue(message.contains("- runtime."+RuntimeVariable.CURRENT_TEST_FOLDER.getVariableName()));


        /* @formatter:on */
    }

    @Test
    void fail_on_start() {
        /* @formatter:off */

        /* prepare */
        SystemTestConfiguration configuration = configure().
                addVariable("a-env-variable","WILL_BE_REPLACED:${env.PATH}").
                addVariable("a-secret-variable","WILL_NOT_BE_REPLACED:${secretEnv.PATH}").
                localSetup().
                    addSolution("faked-fail_on_start").
                        addStartStep().script().path("./05-start-single-sechub-network-docker-compose.sh").endScript().endStep().
                        addStopStep().script().path("./05-stop-single-sechub-network-docker-compose.sh").endScript().endStep().
                    endSolution().
                endLocalSetup().
                build();

        LOG.info("loaded config=\n{}", JSONConverter.get().toJSON(configuration,true));

        /* execute */
        ProcessContainerFailedException exception = assertThrows(ProcessContainerFailedException.class,()->systemTestApi.runSystemTests(
            params().
                localRun().
                dryRun().
                workspacePath(createTempDirectoryInBuildFolder("systemtest_inttest/fail_on_start").toString()).
                testConfiguration(configuration).
                pdsSolutionPath(FAKED_PDS_SOLUTIONS_PATH).
            build()));

        /* test */
        String message = exception.getMessage();

        if(!message.contains("Script: ./05-start-single-sechub-network-docker-compose.sh\n"
                + "Exit code: 33\n"
                + "Error message: This shall be the last fail message")) {
            exception.printStackTrace();
            fail("Unexpected message:"+message);
        }

        /* @formatter:on */
    }

    @Test
    void fail_because_no_pds_config() {
        /* @formatter:off */

        /* prepare */
        SystemTestConfiguration configuration = configure().
                localSetup().
                    addSolution("faked-fail_because_no_pds_server_config_file").
                        addStartStep().script().path("./05-start-single-sechub-network-docker-compose.sh").endScript().endStep().
                        addStopStep().script().path("./05-stop-single-sechub-network-docker-compose.sh").endScript().endStep().
                    endSolution().
                endLocalSetup().
                build();

        LOG.debug("loaded config=\n{}", JSONConverter.get().toJSON(configuration,true));

        /* execute */
        SystemTestRuntimeException exception = assertThrows(SystemTestRuntimeException.class, ()->systemTestApi.runSystemTests(
           params().
                localRun().
                workspacePath(createTempDirectoryInBuildFolder("systemtest_inttest/fail_because_no_pds_config").toString()).
                testConfiguration(configuration).
                pdsSolutionPath(FAKED_PDS_SOLUTIONS_PATH).
            build()));

        String message = exception.getMessage();
        assertTrue(message.contains("PDS server config file does not exist"));


        /* @formatter:on */
    }

    @Test
    void fail_because_pds_config_file_does_not_exist() {
        /* @formatter:off */

        /* prepare */
        SystemTestConfiguration configuration = configure().
                localSetup().
                    addSolution("faked-fail_because_no_pds_server_config_file").
                        addStartStep().script().path("./05-start-single-sechub-network-docker-compose.sh").endScript().endStep().
                        addStopStep().script().path("./05-stop-single-sechub-network-docker-compose.sh").endScript().endStep().
                    endSolution().
                endLocalSetup().
                build();

        LOG.debug("loaded config=\n{}", JSONConverter.get().toJSON(configuration,true));

        /* execute */
        SystemTestRuntimeException exception = assertThrows(SystemTestRuntimeException.class, ()->systemTestApi.runSystemTests(
           params().
                localRun().
                workspacePath(createTempDirectoryInBuildFolder("systemtest_inttest/fail_because_no_pds_config").toString()).
                testConfiguration(configuration).
                pdsSolutionPath(FAKED_PDS_SOLUTIONS_PATH).
            build()));

        String message = exception.getMessage();
        assertTrue(message.contains("PDS server config file does not exist"));


        /* @formatter:on */
    }

}
