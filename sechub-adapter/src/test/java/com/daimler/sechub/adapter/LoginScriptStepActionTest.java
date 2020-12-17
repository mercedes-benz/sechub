package com.daimler.sechub.adapter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LoginScriptStepActionTest {
    @Test
    public void values_of_ignore_case__lower_case() {
               
        /* execute */
        LoginScriptStepAction action = LoginScriptStepAction.valueOfIgnoreCase("input");
        
        /* test */
        assertEquals(LoginScriptStepAction.INPUT, action);
    }
    
    @Test
    public void values_of_igoner_case__upper_case() {
        
        /* execute */
        LoginScriptStepAction action = LoginScriptStepAction.valueOfIgnoreCase("INPUT");
        
        /* test */
        assertEquals(LoginScriptStepAction.INPUT, action);
    }
    
    @Test
    public void values_of_ignore_case__mixed_case() {
        
        /* execute */
        LoginScriptStepAction action = LoginScriptStepAction.valueOfIgnoreCase("Input");
        
        /* test */
        assertEquals(LoginScriptStepAction.INPUT, action);
    }
}
