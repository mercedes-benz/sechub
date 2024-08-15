// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.testengine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.internal.gen.SecHubExecutionApi;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.api.internal.gen.model.*;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
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
    private CurrentTestVariableCalculatorFactory currentTestVariableCalculatorFactory;
    private CurrentTestVariableCalculator calculator;
    private SystemTestRuntimeContext runtimeContext;
    private SecHubClient secHubClient;
    private SecHubExecutionApi secHubExecutionApi;
    private LocationSupport locationSupport;
    private SystemTestRunResult currentTestResult;
    private TestAssertDefinition assertDefinition;

    @BeforeEach
    void beforeEach() throws ApiException {
        ExecutionSupport execSupport = mock(ExecutionSupport.class);

        calculator = mock(CurrentTestVariableCalculator.class);
        runtimeContext = mock(SystemTestRuntimeContext.class);
        currentTestVariableCalculatorFactory = mock(CurrentTestVariableCalculatorFactory.class);
        secHubClient = mock(SecHubClient.class);
        secHubExecutionApi = mock(SecHubExecutionApi.class);
        locationSupport = mock(LocationSupport.class);
        currentTestResult = TestRuntimeAccess.createDummyTestRunResult();
        assertDefinition = mock(TestAssertDefinition.class);

        engineToTest = new SystemTestRuntimeTestEngine(execSupport);
        engineToTest.currentTestVariableCalculatorFactory = currentTestVariableCalculatorFactory;

        when(locationSupport.ensureTestWorkingDirectoryRealPath(any())).thenReturn(Paths.get("./build/tmp/workingdirectory"));
        when(secHubExecutionApi.userCreateNewJob(any(), any())).thenReturn(mock(SchedulerResult.class));
        when(secHubClient.atSecHubExecutionApi()).thenReturn(secHubExecutionApi);
    }

    @Test
    void sechub_client_without_errors_assertion_done__variant_valid() throws Exception {
        /* prepare */
        ScheduleJobStatus status = new ScheduleJobStatus();
        status.setResult(ScheduleJobStatusResult.OK);
        when(secHubExecutionApi.userCheckJobStatus(any(), any())).thenReturn(status);
        ScanSecHubReport report = new ScanSecHubReport();
        report.setTrafficLight(EXPECTED_TRAFFIC_LIGHT_YELLOW);

        when(secHubExecutionApi.userDownloadJobReport(any(), any())).thenReturn(report);

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
        ScheduleJobStatus status = new ScheduleJobStatus();
        status.setResult(ScheduleJobStatusResult.OK);
        when(secHubExecutionApi.userCheckJobStatus(any(), any())).thenReturn(status);
        ScanSecHubReport report = new ScanSecHubReport();
        report.setTrafficLight(TrafficLight.RED);

        when(secHubExecutionApi.userDownloadJobReport(any(), any())).thenReturn(report);

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
        when(secHubExecutionApi.userCreateNewJob(any(), any())).thenThrow(new ApiException("unable to create job"));

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
        ScheduleJobStatus status = new ScheduleJobStatus();
        status.setResult(ScheduleJobStatusResult.OK);
        when(secHubExecutionApi.userCheckJobStatus(any(), any())).thenReturn(status);
        when(secHubExecutionApi.userDownloadJobReport(any(), any())).thenThrow(new NullPointerException());
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
        when(secHubExecutionApi.userCheckJobStatus(any(),any())).thenThrow(new ApiException("no status readable"));

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

        SecHubConfiguration dummySecHubConfiguration = new SecHubConfiguration();
        when(calculator.replace(any())).thenReturn(JSONConverter.get().toJSON(dummySecHubConfiguration));


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
