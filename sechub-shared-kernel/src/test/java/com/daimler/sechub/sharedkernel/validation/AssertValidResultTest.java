// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class AssertValidResultTest {

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();
    
    @Test
    public void valid_result_no_exception_thrown() {
        /* prepare*/
        ValidationResult resultOK = new ValidationResult();
        
        /* precondition check */
        assertTrue(resultOK.isValid());
        
        /* execute + test (no exception)*/
        AssertValidResult.assertValid(resultOK);
    }
    
    @Test
    public void invalid_result_exception_with_error_description_as_message_thrown() {
        /* prepare*/
        ValidationResult resultFAILED = new ValidationResult();
        resultFAILED.addError("error1");
        
        /* precondition check */
        assertFalse(resultFAILED.isValid());
        
        /* test*/
        expected.expectMessage("error1");
        
        /* execute )*/
        AssertValidResult.assertValid(resultFAILED);
    }

}
