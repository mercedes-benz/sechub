// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static org.junit.Assert.*;

import org.junit.Test;

public class LoginScriptActionTest {

	@Test
	public void step_input_getter_check() {
		/* prepare */
		ActionType action = ActionType.INPUT;

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
		ActionType action = ActionType.USERNAME;

		/* test */
		assertTrue(step(action).isUserName());

		assertFalse(step(action).isClick());
		assertFalse(step(action).isPassword());
		assertFalse(step(action).isInput());
	}

	@Test
	public void step_password_getter_check() {
		/* prepare */
		ActionType action = ActionType.PASSWORD;

		/* test */
		assertTrue(step(action).isPassword());

		assertFalse(step(action).isClick());
		assertFalse(step(action).isInput());
		assertFalse(step(action).isUserName());
	}

	@Test
	public void step_click_getter_check() {
		/* prepare */
		ActionType action = ActionType.CLICK;

		/* test */
		assertTrue(step(action).isClick());

		assertFalse(step(action).isPassword());
		assertFalse(step(action).isInput());
		assertFalse(step(action).isUserName());
	}
	
	@Test
	public void description_not_set() {
	    /* prepare */
	    LoginScriptAction step = new LoginScriptAction();
	    
	    /* test + execute */
	    assertNull(step.getDescription());
	}

    @Test
    public void description_set() {
        /* prepare */
        LoginScriptAction step = new LoginScriptAction();
        String description = "I am a description";
        
        /* execute */
        step.description = description;
        
        /* test */
        assertEquals(description, step.getDescription());
    }

	private LoginScriptAction step(ActionType action) {
		LoginScriptAction step = new LoginScriptAction();
		step.action=action;
		return step;
	}
}
