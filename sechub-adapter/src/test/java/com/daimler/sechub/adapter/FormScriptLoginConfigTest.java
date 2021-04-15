// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.LinkedList;

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
        LoginScriptPage page1 = mock(LoginScriptPage.class);
        when(page1.getActions()).thenReturn(new LinkedList<>());

        LoginScriptAction action1 = mock(LoginScriptAction.class);
        when(action1.isUserName()).thenReturn(true);
        when(action1.getValue()).thenReturn("username1");

        LoginScriptAction action2 = mock(LoginScriptAction.class);
        when(action2.isPassword()).thenReturn(true);
        when(action2.getValue()).thenReturn("pwd1");

        configToTest.getPages().add(page1);
        configToTest.getPages().get(0).getActions().add(action1);
        configToTest.getPages().get(0).getActions().add(action2);

        /* test */
        assertEquals(1, configToTest.getPages().size());
        assertEquals("username1", configToTest.getUserName());
        assertEquals("pwd1", configToTest.getPassword());
    }

    @Test
    public void test_no_password_or_user_steps_return_values_by_getter_only_defaults() {
        /* prepare */
        LoginScriptPage page1 = mock(LoginScriptPage.class);
        when(page1.getActions()).thenReturn(new LinkedList<>());

        LoginScriptAction action1 = mock(LoginScriptAction.class);
        when(action1.isUserName()).thenReturn(false);
        when(action1.getValue()).thenReturn("username1");

        LoginScriptAction action2 = mock(LoginScriptAction.class);
        when(action2.isPassword()).thenReturn(false);
        when(action2.getValue()).thenReturn("pwd1");

        configToTest.getPages().add(page1);
        configToTest.getPages().get(0).getActions().add(action1);
        configToTest.getPages().get(0).getActions().add(action2);

        /* test */
        assertEquals(1, configToTest.getPages().size());
        assertEquals("<unknown-user>", configToTest.getUserName());
        assertEquals("<unknown-pwd>", configToTest.getPassword());
    }

    @Test
    public void test_multiple_pages_password_and_user_steps_return_values_by_getter() {
        /* prepare */
        LoginScriptPage page1 = mock(LoginScriptPage.class);
        when(page1.getActions()).thenReturn(new LinkedList<>());

        LoginScriptAction action1 = mock(LoginScriptAction.class);
        when(action1.isUserName()).thenReturn(true);
        when(action1.getValue()).thenReturn("username1");

        LoginScriptAction action2 = mock(LoginScriptAction.class);
        when(action2.isClick()).thenReturn(true);
        when(action2.getSelector()).thenReturn("#next");

        configToTest.getPages().add(page1);
        configToTest.getPages().get(0).getActions().add(action1);
        configToTest.getPages().get(0).getActions().add(action2);

        LoginScriptPage page2 = mock(LoginScriptPage.class);
        when(page2.getActions()).thenReturn(new LinkedList<>());

        LoginScriptAction action3 = mock(LoginScriptAction.class);
        when(action3.isPassword()).thenReturn(true);
        when(action3.getValue()).thenReturn("pwd1");

        LoginScriptAction action4 = mock(LoginScriptAction.class);
        when(action2.isClick()).thenReturn(true);
        when(action2.getSelector()).thenReturn("#login");

        configToTest.getPages().add(page2);
        configToTest.getPages().get(1).getActions().add(action3);
        configToTest.getPages().get(1).getActions().add(action4);

        /* test */
        assertEquals(2, configToTest.getPages().size());
        assertEquals("username1", configToTest.getUserName());
        assertEquals("pwd1", configToTest.getPassword());
    }
}
