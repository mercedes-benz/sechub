// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.UUID;

import com.daimler.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;

public class AssertExecutionResult{

	private ExecutionResult result;

	public static AssertExecutionResult assertResult(ExecutionResult result) {
		if (result==null) {
			fail("result is null!");
		}
		return new AssertExecutionResult(result);
	}
	
	public UUID getSechubJobUUD() {
	    return getResult().getSechubJobUUD();
	}
	
	private AssertExecutionResult(ExecutionResult result) {
		this.result=result;
	}
	
	public AssertExecutionResult isGreen() {
		return isTrafficLight("GREEN");
	}
	
	public AssertExecutionResult isRed() {
		return isTrafficLight("RED");
	}
	
	public AssertExecutionResult isYellow() {
		return isTrafficLight("YELLOW");
	}
	
	public AssertExecutionResult hasExitCode(int exitCode) {
		assertEquals("Exit code not as expected!", exitCode,result.getExitCode());
		return this;
	}
	
	public ExecutionResult getResult() {
		return result;
	}
	
	protected AssertExecutionResult isTrafficLight(String color) {
		if (color==null) {
			throw new IllegalArgumentException("color may not be null - testcase corrupt!");
		}
		String lastOutputLine = result.getLastOutputLine();
		if (lastOutputLine==null) {
			fail("No output line available, so cannot be color:"+color);
		}
		String found = lastOutputLine.trim();
		String wanted = color;
		if (!  found.startsWith(wanted)) {
			fail("Expected: '"+color+"'\nbut got\n'"+lastOutputLine+"'");
		}
		return this;
	}
}