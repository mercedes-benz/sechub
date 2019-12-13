package com.daimler.sechub.adapter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

public class FormScriptLoginConfigTest {

	private FormScriptLoginConfig configToTest;


	@Before
	public void before() {
		configToTest = new FormScriptLoginConfig();
	}


	@Test
	public void test_no_password_nor_user_returns_defaults() {

		/* test */
		assertNotNull(configToTest.getUserName());
		assertNotNull(configToTest.getPassword());

		assertEquals("<unknown-user>", configToTest.getUserName());
		assertEquals("<unknown-pwd>", configToTest.getPassword());
	}

	@Test
	public void test_password_and_user_steps_return_values_by_getter() {
		/* prepare */
		LoginScriptStep step1 = mock(LoginScriptStep.class);
		when(step1.isUserName()).thenReturn(true);
		when(step1.getValue()).thenReturn("username1");

		LoginScriptStep step2 = mock(LoginScriptStep.class);
		when(step2.isPassword()).thenReturn(true);
		when(step2.getValue()).thenReturn("pwd1");

		configToTest.getSteps().add(step1);
		configToTest.getSteps().add(step2);

		/* test */
		assertEquals("username1", configToTest.getUserName());
		assertEquals("pwd1", configToTest.getPassword());
	}

	@Test
	public void test_no_password_or_user_steps_return_values_by_getter_only_defaults() {
		/* prepare */
		LoginScriptStep step1 = mock(LoginScriptStep.class);
		when(step1.isUserName()).thenReturn(false);
		when(step1.getValue()).thenReturn("username1");

		LoginScriptStep step2 = mock(LoginScriptStep.class);
		when(step2.isPassword()).thenReturn(false);
		when(step2.getValue()).thenReturn("pwd1");

		configToTest.getSteps().add(step1);
		configToTest.getSteps().add(step2);

		/* test */
		assertEquals("<unknown-user>", configToTest.getUserName());
		assertEquals("<unknown-pwd>", configToTest.getPassword());
	}

}
