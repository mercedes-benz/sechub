// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static org.junit.Assert.*;

import org.junit.Test;

public class LoginScriptStepTest {

	@Test
	public void step_input_getter_check() {
		/* prepare */
		LoginScriptStepAction action = LoginScriptStepAction.INPUT;

		/* test */
		assertTrue(step(action).isInput());

		assertFalse(step(action).isClick());
		assertFalse(step(action).isPassword());
		assertFalse(step(action).isUserName());
		assertFalse(step(action).isWait());
	}

	@Test
	public void step_username_getter_check() {
		/* prepare */
		LoginScriptStepAction action = LoginScriptStepAction.USERNAME;

		/* test */
		assertTrue(step(action).isUserName());

		assertFalse(step(action).isClick());
		assertFalse(step(action).isPassword());
		assertFalse(step(action).isInput());
	}

	@Test
	public void step_password_getter_check() {
		/* prepare */
		LoginScriptStepAction action = LoginScriptStepAction.PASSWORD;

		/* test */
		assertTrue(step(action).isPassword());

		assertFalse(step(action).isClick());
		assertFalse(step(action).isInput());
		assertFalse(step(action).isUserName());
	}

	@Test
	public void step_click_getter_check() {
		/* prepare */
		LoginScriptStepAction action = LoginScriptStepAction.CLICK;

		/* test */
		assertTrue(step(action).isClick());

		assertFalse(step(action).isPassword());
		assertFalse(step(action).isInput());
		assertFalse(step(action).isUserName());
	}
	
	@Test
	public void description_not_set() {
	    /* prepare */
	    LoginScriptStep step = new LoginScriptStep();
	    
	    /* test + execute */
	    assertNull(step.getDescription());
	}

    @Test
    public void description_set() {
        /* prepare */
        LoginScriptStep step = new LoginScriptStep();
        String description = "I am a description";
        
        /* execute */
        step.description = description;
        
        /* test */
        assertEquals(description, step.getDescription());
    }

	private LoginScriptStep step(LoginScriptStepAction action) {
		LoginScriptStep step = new LoginScriptStep();
		step.action=action;
		return step;
	}
}
