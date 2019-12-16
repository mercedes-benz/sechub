// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.LoginScriptStep;



public class NetsparkerLoginScriptGeneratorTest {

	private NetsparkerLoginScriptGenerator genoToTest;

	@Before
	public void before() {
		genoToTest = new NetsparkerLoginScriptGenerator();
	}


	@Test
	public void null_value_results_in_empty_string() {
		/* execute */
		String result = genoToTest.generate(null);

		/* test */
		assertEquals("",result);
	}

	@Test
	public void empty_list_results_in_empty_string() {
		/* execute */
		String result = genoToTest.generate(new ArrayList<>());

		/* test */
		assertEquals("",result);
	}


	@Test
	public void one_list_entry_but_null_results_in_empty_string() {
		/* prepare */
		List<LoginScriptStep> steps = new ArrayList<LoginScriptStep>();
		steps.add(null);

		/* execute */
		String result = genoToTest.generate(steps);

		/* test */
		assertEquals("",result);
	}

	@Test
	public void input_usernamefield_input_passwordfield_click_buttonfield() {

		/* prepare */
		List<LoginScriptStep> steps = new ArrayList<LoginScriptStep>();
		LoginScriptStep step1 = mock(LoginScriptStep.class);
		LoginScriptStep step2 = mock(LoginScriptStep.class);
		LoginScriptStep step3 = mock(LoginScriptStep.class);

		when(step1.getSelector()).thenReturn("#usernamefield");
		when(step1.getValue()).thenReturn("username1");
		when(step1.isInput()).thenReturn(true);

		when(step2.getSelector()).thenReturn("#passwordfield");
		when(step2.getValue()).thenReturn("password1");
		when(step2.isInput()).thenReturn(true);


		when(step3.getSelector()).thenReturn("#buttonfield");
		when(step3.isClick()).thenReturn(true);


		steps.add(step1);
		steps.add(step2);
		steps.add(step3);

		/* execute */
		String result = genoToTest.generate(steps);


		/* test */
		String expected = "ns.auth.setValueByQuery('#usernamefield','username1');\n";
		expected+="ns.auth.setValueByQuery('#passwordfield','password1');\n";
		expected+="ns.auth.clickByQuery('#buttonfield',2000);\n";

		assertEquals(expected,result);

	}

	@Test
	public void username_usernamefield_password_passwordfield_click_buttonfield() {

		/* prepare */
		List<LoginScriptStep> steps = new ArrayList<LoginScriptStep>();
		LoginScriptStep step1 = mock(LoginScriptStep.class);
		LoginScriptStep step2 = mock(LoginScriptStep.class);
		LoginScriptStep step3 = mock(LoginScriptStep.class);

		when(step1.getSelector()).thenReturn("#usernamefield");
		when(step1.getValue()).thenReturn("username1");
		when(step1.isUserName()).thenReturn(true);

		when(step2.getSelector()).thenReturn("#passwordfield");
		when(step2.getValue()).thenReturn("password1");
		when(step2.isPassword()).thenReturn(true);


		when(step3.getType()).thenReturn("click");
		when(step3.getSelector()).thenReturn("#buttonfield");
		when(step3.isClick()).thenReturn(true);


		steps.add(step1);
		steps.add(step2);
		steps.add(step3);

		/* execute */
		String result = genoToTest.generate(steps);


		/* test */
		String expected = "ns.auth.setValueByQuery('#usernamefield',username);\n";
		expected+="ns.auth.setValueByQuery('#passwordfield',password);\n";
		expected+="ns.auth.clickByQuery('#buttonfield',2000);\n";

		assertEquals(expected,result);

	}

}
