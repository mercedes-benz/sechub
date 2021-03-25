// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.UUID;

import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;

public class AssertExecutionResult {

    private ExecutionResult result;

    public static AssertExecutionResult assertResult(ExecutionResult result) {
        if (result == null) {
            fail("result is null!");
        }
        return new AssertExecutionResult(result);
    }

    public UUID getSechubJobUUD() {
        return getResult().getSechubJobUUID();
    }

    private AssertExecutionResult(ExecutionResult result) {
        this.result = result;
    }

    public AssertExecutionResult isTrafficLight(TrafficLight trafficLight) {
        AssertSecHubReport.assertSecHubReport(getResult()).hasTrafficLight(trafficLight);
        return this;
    }

    public AssertExecutionResult isGreen() {
        return isTrafficLight(TrafficLight.GREEN);
    }

    public AssertExecutionResult isRed() {
        return isTrafficLight(TrafficLight.RED);
    }

    public AssertExecutionResult isYellow() {
        return isTrafficLight(TrafficLight.YELLOW);
    }

    public AssertExecutionResult hasExitCode(int exitCode) {
        assertEquals("Exit code not as expected!", exitCode, result.getExitCode());
        return this;
    }

    public ExecutionResult getResult() {
        return result;
    }

}