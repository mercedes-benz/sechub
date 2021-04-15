// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import static org.junit.Assert.*;

import org.junit.Test;

public class ProfileDescriptionValidationImplTest {

    ProfileDescriptionValidationImpl validationToTest = new ProfileDescriptionValidationImpl();
    
    @Test
    public void abcdefgh_is_avlid() {
        assertTrue(validationToTest.validate("abcdefgh").isValid());
    }
    
    @Test
    public void null_is_valid() {
        assertTrue(validationToTest.validate((String)null).isValid());
    }
    
    @Test
    public void a170_charlength_name_is_valid() {
        assertTrue(validationToTest.validate(create170Description()).isValid());
    }

    @Test
    public void a31_charlength_name_is_invalid() {
        assertFalse(validationToTest.validate(create170Description()+"1").isValid());
    }
    
    private String create170Description() {
        String tenLength="1234 67an\n";
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<17;i++) {
            sb.append(tenLength);
        }
        return sb.toString();
    }
}
