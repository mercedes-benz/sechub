// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.testengine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.api.JobStatus;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.SecHubClientException;
import com.mercedesbenz.sechub.api.SecHubReport;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.systemtest.config.AssertSechubResultDefinition;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestAssertDefinition;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.LocationSupport;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRunResult;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeContext;
import com.mercedesbenz.sechub.systemtest.runtime.TestRuntimeAccess;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ExecutionSupport;

class SystemTestRuntimeTestEngineTest {

    private static final TrafficLight EXPECTED_TRAFFIC_LIGHT_YELLOW = TrafficLight.YELLOW;
    private SystemTestRuntimeTestEngine engineToTest;
    private ExecutionSupport execSupport;
    private CurrentTestVariableCalculatorFactory currentTestVariableCalculatorFactory;
    private CurrentTestVariableCalculator calculator;
    private SystemTestRuntimeContext runtimeContext;
    private SecHubClient secHubClient;
    private LocationSupport locationSupport;
    private SystemTestRunResult currentTestResult;
    private TestAssertDefinition assertDefinition;

    @BeforeEach
    void beforeEach() {
        execSupport = mock(ExecutionSupport.class);
        calculator = mock(CurrentTestVariableCalculator.class);
        runtimeContext = mock(SystemTestRuntimeContext.class);
        currentTestVariableCalculatorFactory = mock(CurrentTestVariableCalculatorFactory.class);
        secHubClient = mock(SecHubClient.class);
        locationSupport = mock(LocationSupport.class);
        currentTestResult = TestRuntimeAccess.createDummyTestRunResult();
        assertDefinition = mock(TestAssertDefinition.class);

        engineToTest = new SystemTestRuntimeTestEngine(execSupport);
        engineToTest.currentTestVariableCalculatorFactory = currentTestVariableCalculatorFactory;

        when(locationSupport.ensureTestWorkingDirectoryRealPath(any())).thenReturn(Paths.get("./build/tmp/workingdirectory"));
    }

    @Test
    void sechub_client_without_errors_assertion_done__variant_valid() throws Exception {
        /* prepare */
        JobStatus status = new JobStatus();
        status.setResult(ExecutionResult.OK);
        when(secHubClient.fetchJobStatus(any(), any())).thenReturn(status);
        SecHubReport report = new SecHubReport();
        report.setTrafficLight(EXPECTED_TRAFFIC_LIGHT_YELLOW);

        when(secHubClient.downloadSecHubReportAsJson(any(), any())).thenReturn(report);

        TestDefinition test = configureSecHubLocalRunAndReturnTestDefinition();

        /* execute */
        engineToTest.runTest(test, runtimeContext);

        /* test */
        assertFalse(currentTestResult.hasFailed());
        assertNull(currentTestResult.getFailure());

        // check assert definition interactions where done
        verify(assertDefinition).getSechubResult();

    }

    @Test
    void sechub_client_without_errors_assertion_done__variant_assertion_fails_for_trafficlight() throws Exception {
        /* prepare */
        JobStatus status = new JobStatus();
        status.setResult(ExecutionResult.OK);
        when(secHubClient.fetchJobStatus(any(), any())).thenReturn(status);
        SecHubReport report = new SecHubReport();
        report.setTrafficLight(TrafficLight.RED);

        when(secHubClient.downloadSecHubReportAsJson(any(), any())).thenReturn(report);

        TestDefinition test = configureSecHubLocalRunAndReturnTestDefinition();

        /* execute */
        engineToTest.runTest(test, runtimeContext);

        /* test */
        assertTrue(currentTestResult.hasFailed());
        assertEquals("SecHub report not as wanted. Expected was traffic light: YELLOW, but result was: RED", currentTestResult.getFailure().getMessage());

        // check assert definition interactions where done
        verify(assertDefinition).getSechubResult();

    }

    @Test
    void sechub_job_creation_fails() throws Exception {
        /* prepare */
        when(secHubClient.createJob(any())).thenThrow(new SecHubClientException("unable to create job"));

        TestDefinition test = configureSecHubLocalRunAndReturnTestDefinition();

        /* execute */
        engineToTest.runTest(test, runtimeContext);

        /* test */
        assertEquals("Was not able to launch SecHub job. Reason: unable to create job", currentTestResult.getFailure().getMessage());
        assertTrue(currentTestResult.getFailure().getDetails().contains("unable to create job"));
        assertTrue(currentTestResult.hasFailed());
        verifyNoInteractions(assertDefinition);
    }

    @Test
    void sechub_download_fails() throws Exception {
        /* prepare */
        JobStatus status = new JobStatus();
        status.setResult(ExecutionResult.OK);
        when(secHubClient.fetchJobStatus(any(), any())).thenReturn(status);
        when(secHubClient.downloadSecHubReportAsJson(any(), any())).thenThrow(new NullPointerException());
        TestDefinition test = configureSecHubLocalRunAndReturnTestDefinition();

        /* execute */
        engineToTest.runTest(test, runtimeContext);

        /* test */
        assertEquals("Was not able to launch SecHub job. Reason: NullPointerException", currentTestResult.getFailure().getMessage());
        assertTrue(currentTestResult.getFailure().getDetails().contains("java.lang.NullPointerException"));
        assertTrue(currentTestResult.hasFailed());
    }

    @Test
    void sechub_status_fails() throws Exception {
        /* prepare */
        when(secHubClient.fetchJobStatus(any(),any())).thenThrow(new SecHubClientException("no status readable"));

        TestDefinition test = configureSecHubLocalRunAndReturnTestDefinition();

        /* execute */
        engineToTest.runTest(test, runtimeContext);

        /* test */
        assertEquals("Was not able to launch SecHub job. Reason: no status readable", currentTestResult.getFailure().getMessage());
        assertTrue(currentTestResult.getFailure().getDetails().contains("no status readable"));
        assertTrue(currentTestResult.hasFailed());
    }

    private TestDefinition configureSecHubLocalRunAndReturnTestDefinition() {
        when(runtimeContext.isLocalRun()).thenReturn(true);
        when(runtimeContext.getLocalAdminSecHubClient()).thenReturn(secHubClient);
        when(currentTestVariableCalculatorFactory.create(any(), any())).thenReturn(calculator);
        when(runtimeContext.getLocationSupport()).thenReturn(locationSupport);
        when(runtimeContext.getCurrentResult()).thenReturn(currentTestResult);

        SecHubConfigurationModel dummyModel = new SecHubConfigurationModel();
        when(calculator.replace(any())).thenReturn(JSONConverter.get().toJSON(dummyModel));


        AssertSechubResultDefinition sechubResult = new AssertSechubResultDefinition();
        sechubResult.setHasTrafficLight(Optional.of(EXPECTED_TRAFFIC_LIGHT_YELLOW));
        when(assertDefinition.getSechubResult()).thenReturn(Optional.of(sechubResult));

        TestDefinition test = new TestDefinition();
        RunSecHubJobDefinition sechubJobDefinition = new RunSecHubJobDefinition();
        test.getExecute().setRunSecHubJob(Optional.of(sechubJobDefinition));
        test.getAssert().add(assertDefinition);


        return test;
    }
}
