// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PDSWorkspacePreparationResultCalculatorTest {

    private PDSWorkspacePreparationResultCalculator calculatorToTest;
    private PDSWorkspacePreparationContext context;

    @BeforeEach
    void beforeEach() {
        calculatorToTest = new PDSWorkspacePreparationResultCalculator();
        context = mock(PDSWorkspacePreparationContext.class);
    }

    @Test
    void context_returning_false_for_every_thing_results_in_non_executable_launcher_script() {
        /* execute */
        PDSWorkspacePreparationResult result = calculatorToTest.calculateResult(context);
        when(context.isNoneAccepted()).thenReturn(false);

        when(context.isBinaryAccepted()).thenReturn(false);
        when(context.isSourceAccepted()).thenReturn(false);
        when(context.isExtractedBinaryAvailable()).thenReturn(false);
        when(context.isExtractedSourceAvailable()).thenReturn(false);

        /* test */
        assertFalse(result.isLauncherScriptExecutable());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void none__accepted_results_in_executable_launcher_script_no_matter_what_other_getters_return(boolean other) {
        /* prepare */
        when(context.isNoneAccepted()).thenReturn(true);

        when(context.isBinaryAccepted()).thenReturn(other);
        when(context.isSourceAccepted()).thenReturn(other);
        when(context.isExtractedBinaryAvailable()).thenReturn(other);
        when(context.isExtractedSourceAvailable()).thenReturn(other);

        /* execute */
        PDSWorkspacePreparationResult result = calculatorToTest.calculateResult(context);

        /* test */
        assertTrue(result.isLauncherScriptExecutable());
    }

    @Test
    void binary_accepted_binaries_extracted_script_launchable_yes() {

        /* prepare */
        when(context.isNoneAccepted()).thenReturn(false);

        when(context.isBinaryAccepted()).thenReturn(true);
        when(context.isExtractedBinaryAvailable()).thenReturn(true);

        when(context.isSourceAccepted()).thenReturn(false);
        when(context.isExtractedSourceAvailable()).thenReturn(false);

        /* execute */
        PDSWorkspacePreparationResult result = calculatorToTest.calculateResult(context);

        /* test */
        assertTrue(result.isLauncherScriptExecutable());
    }

    @Test
    void binary_accepted_binaries_NOT_extracted_script_launchable_no() {
        /* prepare */
        when(context.isNoneAccepted()).thenReturn(false);

        when(context.isBinaryAccepted()).thenReturn(true);
        when(context.isExtractedBinaryAvailable()).thenReturn(false);

        when(context.isSourceAccepted()).thenReturn(false);
        when(context.isExtractedSourceAvailable()).thenReturn(false);

        /* execute */
        PDSWorkspacePreparationResult result = calculatorToTest.calculateResult(context);

        /* test */
        assertFalse(result.isLauncherScriptExecutable());
    }

    @Test
    void binary_NOT_accepted_binaries_extracted_script_launchable_no() {
        /* prepare */
        when(context.isNoneAccepted()).thenReturn(false);

        when(context.isBinaryAccepted()).thenReturn(false);
        when(context.isExtractedBinaryAvailable()).thenReturn(true);

        when(context.isSourceAccepted()).thenReturn(false);
        when(context.isExtractedSourceAvailable()).thenReturn(false);

        /* execute */
        PDSWorkspacePreparationResult result = calculatorToTest.calculateResult(context);

        /* test */
        assertFalse(result.isLauncherScriptExecutable());
    }

    @Test
    void source_accepted_sources_extracted_script_launchable_yes() {

        /* prepare */
        when(context.isNoneAccepted()).thenReturn(false);

        when(context.isBinaryAccepted()).thenReturn(false);
        when(context.isExtractedBinaryAvailable()).thenReturn(false);

        when(context.isSourceAccepted()).thenReturn(true);
        when(context.isExtractedSourceAvailable()).thenReturn(true);

        /* execute */
        PDSWorkspacePreparationResult result = calculatorToTest.calculateResult(context);

        /* test */
        assertTrue(result.isLauncherScriptExecutable());
    }

    @Test
    void source_accepted_sources_NOT_extracted_script_launchable_no() {
        /* prepare */
        when(context.isNoneAccepted()).thenReturn(false);

        when(context.isBinaryAccepted()).thenReturn(false);
        when(context.isExtractedBinaryAvailable()).thenReturn(false);

        when(context.isSourceAccepted()).thenReturn(true);
        when(context.isExtractedSourceAvailable()).thenReturn(false);

        /* execute */
        PDSWorkspacePreparationResult result = calculatorToTest.calculateResult(context);

        /* test */
        assertFalse(result.isLauncherScriptExecutable());
    }

    @Test
    void source_NOT_accepted_sources_extracted_script_launchable_no() {
        /* prepare */
        when(context.isNoneAccepted()).thenReturn(false);

        when(context.isBinaryAccepted()).thenReturn(false);
        when(context.isExtractedBinaryAvailable()).thenReturn(false);

        when(context.isSourceAccepted()).thenReturn(false);
        when(context.isExtractedSourceAvailable()).thenReturn(true);

        /* execute */
        PDSWorkspacePreparationResult result = calculatorToTest.calculateResult(context);

        /* test */
        assertFalse(result.isLauncherScriptExecutable());
    }

    @Test
    void source_and_binaries_accepted_sources_extracted_script_launchable_yes() {

        /* prepare */
        when(context.isNoneAccepted()).thenReturn(false);

        when(context.isBinaryAccepted()).thenReturn(true);
        when(context.isExtractedBinaryAvailable()).thenReturn(false);

        when(context.isSourceAccepted()).thenReturn(true);
        when(context.isExtractedSourceAvailable()).thenReturn(true);

        /* execute */
        PDSWorkspacePreparationResult result = calculatorToTest.calculateResult(context);

        /* test */
        assertTrue(result.isLauncherScriptExecutable());
    }

    @Test
    void source_and_binaries_accepted_binaries_extracted_script_launchable_yes() {

        /* prepare */
        when(context.isNoneAccepted()).thenReturn(false);

        when(context.isBinaryAccepted()).thenReturn(true);
        when(context.isExtractedBinaryAvailable()).thenReturn(true);

        when(context.isSourceAccepted()).thenReturn(true);
        when(context.isExtractedSourceAvailable()).thenReturn(false);

        /* execute */
        PDSWorkspacePreparationResult result = calculatorToTest.calculateResult(context);

        /* test */
        assertTrue(result.isLauncherScriptExecutable());
    }

    @Test
    void source_and_binaries_accepted_both_extracted_script_launchable_yes() {

        /* prepare */
        when(context.isNoneAccepted()).thenReturn(false);

        when(context.isBinaryAccepted()).thenReturn(true);
        when(context.isExtractedBinaryAvailable()).thenReturn(true);

        when(context.isSourceAccepted()).thenReturn(true);
        when(context.isExtractedSourceAvailable()).thenReturn(true);

        /* execute */
        PDSWorkspacePreparationResult result = calculatorToTest.calculateResult(context);

        /* test */
        assertTrue(result.isLauncherScriptExecutable());
    }

}
