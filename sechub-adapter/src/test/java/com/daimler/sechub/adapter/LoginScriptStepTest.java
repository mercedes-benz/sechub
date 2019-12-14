// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static org.junit.Assert.*;

import org.junit.Test;

public class LoginScriptStepTest {

	@Test
	public void step_input_getter_check() {
		/* prepare */
		String type = "input";

		/* test */
		assertTrue(step(type).isInput());

		assertFalse(step(type).isClick());
		assertFalse(step(type).isPassword());
		assertFalse(step(type).isUserName());
	}

	@Test
	public void step_username_getter_check() {
		/* prepare */
		String type = "username";

		/* test */
		assertTrue(step(type).isUserName());

		assertFalse(step(type).isClick());
		assertFalse(step(type).isPassword());
		assertFalse(step(type).isInput());
	}

	@Test
	public void step_password_getter_check() {
		/* prepare */
		String type = "password";

		/* test */
		assertTrue(step(type).isPassword());

		assertFalse(step(type).isClick());
		assertFalse(step(type).isInput());
		assertFalse(step(type).isUserName());
	}

	@Test
	public void step_click_getter_check() {
		/* prepare */
		String type = "click";

		/* test */
		assertTrue(step(type).isClick());

		assertFalse(step(type).isPassword());
		assertFalse(step(type).isInput());
		assertFalse(step(type).isUserName());
	}


	private LoginScriptStep step(String type) {
		LoginScriptStep step = new LoginScriptStep();
		step.type=type;
		return step;
	}
}
