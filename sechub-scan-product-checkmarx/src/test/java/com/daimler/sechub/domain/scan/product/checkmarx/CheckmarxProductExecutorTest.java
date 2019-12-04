package com.daimler.sechub.domain.scan.product.checkmarx;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CheckmarxProductExecutorTest {

	private CheckmarxProductExecutor executorToTest;

	@Before
	public void before() {
		executorToTest= new CheckmarxProductExecutor();
	}

	@Test
	public void action_executor_is_not_null() {
		assertNotNull(executorToTest.resilientActionExecutor);
	}

	@Test
	public void action_executor_contains_checkmarx_bad_request_consultant() {
		assertTrue(executorToTest.resilientActionExecutor.containsConsultant(CheckmarxBadRequestConsultant.class));
	}

}
