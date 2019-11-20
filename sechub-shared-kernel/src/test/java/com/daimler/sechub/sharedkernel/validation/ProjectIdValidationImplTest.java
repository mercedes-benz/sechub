// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import static org.junit.Assert.*;

import org.junit.Test;

public class ProjectIdValidationImplTest {

    private ProjectIdValidationImpl validationToTest = new ProjectIdValidationImpl();

    @Test
    public void lengthTest(){
        ValidationResult projectIdValidationResult = validationToTest.validate("ab");
        assertTrue("Project id not valid", projectIdValidationResult.isValid());
    }

    @Test
    public void lengthTooShortTest(){
        ValidationResult projectIdValidationResult = validationToTest.validate("a");
        assertFalse("Project id is not too short.", projectIdValidationResult.isValid());
    }
    @Test
    public void ABCDIsNOTValidBecauseUppercaseCharactersNotAllowed(){
        ValidationResult projectIdValidationResult = validationToTest.validate("ABCD");
        assertFalse("Project id is not okay, but should?!?", projectIdValidationResult.isValid());
    }

    @Test
    public void abcdIsValid(){
        ValidationResult projectIdValidationResult = validationToTest.validate("abcd");
        assertTrue("Project id is not okay, but should?!?", projectIdValidationResult.isValid());
    }

    @Test
    public void containsDotIsNotValid(){
        ValidationResult projectIdValidationResult = validationToTest.validate("ab.d");
        assertFalse("Project id dot forbidden, but accepted?!?", projectIdValidationResult.isValid());
    }

    @Test
    public void containsSlashIsNotValid(){
        ValidationResult projectIdValidationResult = validationToTest.validate("ab/d");
        assertFalse("Project id slash forbidden, but accepted?!?", projectIdValidationResult.isValid());
    }
    @Test
    public void containsBackSlashIsNotValid(){
        ValidationResult projectIdValidationResult = validationToTest.validate("ab\\d");
        assertFalse("Project id backslash forbidden, but accepted?!?", projectIdValidationResult.isValid());
    }
    @Test
    public void containsDollorIsNotValid(){
        ValidationResult projectIdValidationResult = validationToTest.validate("ab$d");
        assertFalse("Project id backslash forbidden, but accepted?!?", projectIdValidationResult.isValid());
    }
    @Test
    public void containsPercentageIsNotValid(){
        ValidationResult projectIdValidationResult = validationToTest.validate("ab$d");
        assertFalse("Project id backslash forbidden, but accepted?!?", projectIdValidationResult.isValid());
    }
    @Test
    public void containsQuestionMarkIsNotValid(){
        ValidationResult projectIdValidationResult = validationToTest.validate("ab?d");
        assertFalse("Project id backslash forbidden, but accepted?!?", projectIdValidationResult.isValid());
    }
    @Test
    public void containsExlamationMarkIsNotValid(){
        ValidationResult projectIdValidationResult = validationToTest.validate("ab!d");
        assertFalse("Project id backslash forbidden, but accepted?!?", projectIdValidationResult.isValid());
    }
    @Test
    public void containsColonIsNotValid(){
        ValidationResult projectIdValidationResult = validationToTest.validate("AB:D".toLowerCase());
        assertFalse("Project id backslash forbidden, but accepted?!?", projectIdValidationResult.isValid());
    }
}
