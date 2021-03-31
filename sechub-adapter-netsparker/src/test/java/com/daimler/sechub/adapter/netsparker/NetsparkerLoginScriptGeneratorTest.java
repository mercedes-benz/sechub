// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.LoginScriptAction;
import com.daimler.sechub.adapter.SecHubTimeUnit;

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
        assertEquals("", result);
    }

    @Test
    public void empty_list_results_in_empty_string() {
        /* execute */
        String result = genoToTest.generate(new ArrayList<>());

        /* test */
        assertEquals("", result);
    }

    @Test
    public void one_list_entry_but_null_results_in_empty_string() {
        /* prepare */
        List<LoginScriptAction> steps = new ArrayList<LoginScriptAction>();
        steps.add(null);

        /* execute */
        String result = genoToTest.generate(steps);

        /* test */
        assertEquals("", result);
    }

    @Test
    public void one_step() {
        /* prepare */
        List<LoginScriptAction> steps = new ArrayList<LoginScriptAction>();
        LoginScriptAction step1 = mock(LoginScriptAction.class);

        when(step1.isClick()).thenReturn(true);
        when(step1.getSelector()).thenReturn("#buttonfield");

        steps.add(step1);

        /* execute */
        String result = genoToTest.generate(steps);

        /* test */
        String expected = "netsparker.auth.clickByQuery('#buttonfield');";

        assertEquals(expected, result);

    }

    @Test
    public void two_steps() {
        /* prepare */
        List<LoginScriptAction> steps = new ArrayList<LoginScriptAction>();
        LoginScriptAction step1 = mock(LoginScriptAction.class);
        LoginScriptAction step2 = mock(LoginScriptAction.class);

        when(step1.isInput()).thenReturn(true);
        when(step1.getSelector()).thenReturn("#usernamefield");
        when(step1.getValue()).thenReturn("username1");

        when(step2.isClick()).thenReturn(true);
        when(step2.getSelector()).thenReturn("#buttonfield");

        steps.add(step1);
        steps.add(step2);

        /* execute */
        String result = genoToTest.generate(steps);

        /* test */
        String expected = "netsparker.auth.setValueByQuery('#usernamefield','username1');";
        expected += "netsparker.auth.clickByQuery('#buttonfield');";

        assertEquals(expected, result);

    }

    @Test
    public void input_usernamefield_input_passwordfield_click_buttonfield() {

        /* prepare */
        List<LoginScriptAction> steps = new ArrayList<LoginScriptAction>();
        LoginScriptAction step1 = mock(LoginScriptAction.class);
        LoginScriptAction step2 = mock(LoginScriptAction.class);
        LoginScriptAction step3 = mock(LoginScriptAction.class);

        when(step1.isInput()).thenReturn(true);
        when(step1.getSelector()).thenReturn("#usernamefield");
        when(step1.getValue()).thenReturn("username1");
        
        when(step2.isInput()).thenReturn(true);
        when(step2.getSelector()).thenReturn("#passwordfield");
        when(step2.getValue()).thenReturn("password1");
        
        when(step3.isClick()).thenReturn(true);
        when(step3.getSelector()).thenReturn("#buttonfield");
        
        steps.add(step1);
        steps.add(step2);
        steps.add(step3);

        /* execute */
        String result = genoToTest.generate(steps);

        /* test */
        String expected = "netsparker.auth.setValueByQuery('#usernamefield','username1');";
        expected += "netsparker.auth.setValueByQuery('#passwordfield','password1');";
        expected += "netsparker.auth.clickByQuery('#buttonfield');";

        assertEquals(expected, result);

    }

    @Test
    public void username_usernamefield_password_passwordfield_click_buttonfield() {

        /* prepare */
        List<LoginScriptAction> steps = new ArrayList<LoginScriptAction>();
        LoginScriptAction step1 = mock(LoginScriptAction.class);
        LoginScriptAction step2 = mock(LoginScriptAction.class);
        LoginScriptAction step3 = mock(LoginScriptAction.class);

        when(step1.isUserName()).thenReturn(true);
        when(step1.getSelector()).thenReturn("#usernamefield");
        when(step1.getValue()).thenReturn("username1");
        
        when(step2.isPassword()).thenReturn(true);
        when(step2.getSelector()).thenReturn("#passwordfield");
        when(step2.getValue()).thenReturn("password1");
        
        when(step3.isClick()).thenReturn(true);
        when(step3.getSelector()).thenReturn("#buttonfield");

        steps.add(step1);
        steps.add(step2);
        steps.add(step3);

        /* execute */
        String result = genoToTest.generate(steps);

        /* test */
        String expected = "netsparker.auth.setValueByQuery('#usernamefield',username);";
        expected += "netsparker.auth.setValueByQuery('#passwordfield',password);";
        expected += "netsparker.auth.clickByQuery('#buttonfield');";

        assertEquals(expected, result);
    }

    @Test
    public void input_username_input_password_wait_click_button() {

        /* prepare */
        List<LoginScriptAction> steps = new ArrayList<LoginScriptAction>();
        LoginScriptAction step1 = mock(LoginScriptAction.class);
        LoginScriptAction step2 = mock(LoginScriptAction.class);
        LoginScriptAction step3 = mock(LoginScriptAction.class);
        LoginScriptAction step4 = mock(LoginScriptAction.class);

        when(step1.isInput()).thenReturn(true);
        when(step1.getSelector()).thenReturn("#usernamefield");
        when(step1.getValue()).thenReturn("username1");

        when(step2.isInput()).thenReturn(true);
        when(step2.getSelector()).thenReturn("#passwordfield");
        when(step2.getValue()).thenReturn("password1");

        when(step3.isWait()).thenReturn(true);
        when(step3.getValue()).thenReturn("3020");
        when(step3.getUnit()).thenReturn(SecHubTimeUnit.MILLISECOND);

        when(step4.isClick()).thenReturn(true);
        when(step4.getSelector()).thenReturn("#buttonfield");

        steps.add(step1);
        steps.add(step2);
        steps.add(step3);
        steps.add(step4);

        /* execute */
        String result = genoToTest.generate(steps);

        /* test */
        String expected = "netsparker.auth.setValueByQuery('#usernamefield','username1');";
        expected += "netsparker.auth.setValueByQuery('#passwordfield','password1');";
        expected += "netsparker.auth.clickByQuery('#buttonfield',3020);";

        assertEquals(expected, result);
    }

    @Test
    public void input_username_input_password_click_button_wait() {

        /* prepare */
        List<LoginScriptAction> steps = new ArrayList<LoginScriptAction>();
        LoginScriptAction step1 = mock(LoginScriptAction.class);
        LoginScriptAction step2 = mock(LoginScriptAction.class);
        LoginScriptAction step3 = mock(LoginScriptAction.class);
        LoginScriptAction step4 = mock(LoginScriptAction.class);

        when(step1.isInput()).thenReturn(true);
        when(step1.getSelector()).thenReturn("#usernamefield");
        when(step1.getValue()).thenReturn("username1");

        when(step2.isInput()).thenReturn(true);
        when(step2.getSelector()).thenReturn("#passwordfield");
        when(step2.getValue()).thenReturn("password1");

        when(step3.isClick()).thenReturn(true);
        when(step3.getSelector()).thenReturn("#buttonfield");

        when(step4.isWait()).thenReturn(true);
        when(step4.getValue()).thenReturn("5");
        when(step4.getUnit()).thenReturn(SecHubTimeUnit.SECOND);

        steps.add(step1);
        steps.add(step2);
        steps.add(step3);
        steps.add(step4);

        /* execute */
        String result = genoToTest.generate(steps);

        /* test */
        String expected = "netsparker.auth.setValueByQuery('#usernamefield','username1');";
        expected += "netsparker.auth.setValueByQuery('#passwordfield','password1');";
        expected += "netsparker.auth.clickByQuery('#buttonfield');";
        expected += "setTimeout(function() {},5000);";

        assertEquals(expected, result);
    }

    @Test
    public void wait_input_username_input_password_click_button() {

        /* prepare */
        List<LoginScriptAction> steps = new ArrayList<LoginScriptAction>();
        LoginScriptAction step1 = mock(LoginScriptAction.class);
        LoginScriptAction step2 = mock(LoginScriptAction.class);
        LoginScriptAction step3 = mock(LoginScriptAction.class);
        LoginScriptAction step4 = mock(LoginScriptAction.class);

        when(step1.isWait()).thenReturn(true);
        when(step1.getValue()).thenReturn("1");
        when(step1.getUnit()).thenReturn(SecHubTimeUnit.MINUTE);

        when(step2.isInput()).thenReturn(true);
        when(step2.getSelector()).thenReturn("#usernamefield");
        when(step2.getValue()).thenReturn("username1");

        when(step3.isInput()).thenReturn(true);
        when(step3.getSelector()).thenReturn("#passwordfield");
        when(step3.getValue()).thenReturn("password1");

        when(step4.isClick()).thenReturn(true);
        when(step4.getSelector()).thenReturn("#buttonfield");

        steps.add(step1);
        steps.add(step2);
        steps.add(step3);
        steps.add(step4);

        /* execute */
        String result = genoToTest.generate(steps);

        /* test */
        String expected = "netsparker.auth.setValueByQuery('#usernamefield','username1',60000);";
        expected += "netsparker.auth.setValueByQuery('#passwordfield','password1');";
        expected += "netsparker.auth.clickByQuery('#buttonfield');";

        assertEquals(expected, result);
    }

    @Test
    public void input_username_input_email_input_password_click_button() {

        /* prepare */
        List<LoginScriptAction> steps = new ArrayList<LoginScriptAction>();
        LoginScriptAction step1 = mock(LoginScriptAction.class);
        LoginScriptAction step2 = mock(LoginScriptAction.class);
        LoginScriptAction step3 = mock(LoginScriptAction.class);
        LoginScriptAction step4 = mock(LoginScriptAction.class);

        when(step1.isUserName()).thenReturn(true);
        when(step1.getSelector()).thenReturn("#usernamefield");
        when(step1.getValue()).thenReturn("username1");

        when(step2.isInput()).thenReturn(true);
        when(step2.getSelector()).thenReturn("#emailfield");
        when(step2.getValue()).thenReturn("user@example.org");
        when(step2.getDescription()).thenReturn("The email has to be provided.");

        when(step3.isPassword()).thenReturn(true);
        when(step3.getSelector()).thenReturn("#passwordfield");
        when(step3.getValue()).thenReturn("password1");

        when(step4.isClick()).thenReturn(true);
        when(step4.getSelector()).thenReturn("#buttonfield");

        steps.add(step1);
        steps.add(step2);
        steps.add(step3);
        steps.add(step4);

        /* execute */
        String result = genoToTest.generate(steps);

        /* test */
        String expected = "netsparker.auth.setValueByQuery('#usernamefield',username);";
        expected += "/* The email has to be provided. */";
        expected += "netsparker.auth.setValueByQuery('#emailfield','user@example.org');";
        expected += "netsparker.auth.setValueByQuery('#passwordfield',password);";
        expected += "netsparker.auth.clickByQuery('#buttonfield');";

        assertEquals(expected, result);
    }

    @Test
    public void wait_wait_wait_wait() {

        /* prepare */
        List<LoginScriptAction> steps = new ArrayList<LoginScriptAction>();
        LoginScriptAction step1 = mock(LoginScriptAction.class);
        LoginScriptAction step2 = mock(LoginScriptAction.class);
        LoginScriptAction step3 = mock(LoginScriptAction.class);
        LoginScriptAction step4 = mock(LoginScriptAction.class);

        when(step1.isWait()).thenReturn(true);
        when(step1.getValue()).thenReturn("100");
        when(step1.getUnit()).thenReturn(SecHubTimeUnit.MILLISECOND);

        when(step2.isWait()).thenReturn(true);
        when(step2.getValue()).thenReturn("200");
        when(step2.getUnit()).thenReturn(SecHubTimeUnit.MILLISECOND);

        when(step3.isWait()).thenReturn(true);
        when(step3.getValue()).thenReturn("300");
        when(step3.getUnit()).thenReturn(SecHubTimeUnit.MILLISECOND);

        when(step4.isWait()).thenReturn(true);
        when(step4.getValue()).thenReturn("400");
        when(step4.getUnit()).thenReturn(SecHubTimeUnit.MILLISECOND);

        steps.add(step1);
        steps.add(step2);
        steps.add(step3);
        steps.add(step4);

        /* execute */
        String result = genoToTest.generate(steps);

        /* test */
        String expected = "setTimeout(function() {},100);";
        expected += "setTimeout(function() {},200);";
        expected += "setTimeout(function() {},300);";
        expected += "setTimeout(function() {},400);";

        assertEquals(expected, result);
    }
}
