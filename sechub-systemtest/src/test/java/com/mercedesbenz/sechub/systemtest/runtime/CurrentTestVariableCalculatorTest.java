// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.systemtest.config.CalculatedVariables;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.testengine.CurrentTestVariableCalculator;
import com.mercedesbenz.sechub.test.TestUtil;

class CurrentTestVariableCalculatorTest {

    private SystemTestRuntimeContext context;
    private TestDefinition test;
    private CurrentTestVariableCalculator calculatorToTest;
    private LocationSupport locationSupport;
    private Path testFolder;

    @BeforeEach
    void beforeEach() throws Exception {

        testFolder = TestUtil.createTempDirectoryInBuildFolder("testFolder").toRealPath();

        context = mock(SystemTestRuntimeContext.class);
        test = mock(TestDefinition.class);
        locationSupport = mock(LocationSupport.class);

        when(locationSupport.ensureTestWorkingDirectoryRealPath(test)).thenReturn(testFolder);

        when(context.getLocationSupport()).thenReturn(locationSupport);

        calculatorToTest = new CurrentTestVariableCalculator(test, context);
    }

    @Test
    void replace_works_for_calculated_testworkingdirectory() throws Exception {
        /* prepare */

        String testWorkingDirectory = testFolder.toString();

        String origin = """
                {
                    "someJson" : "${calculated.testWorkingDirectory}/other"
                }
                """;

        String expectedReplacement = """
                {
                    "someJson" : "%s/other"
                }
                """.formatted(testWorkingDirectory);

        /* execute */
        String replaced = calculatorToTest.replace(origin);

        /* test */
        assertEquals(expectedReplacement, replaced);
    }

    @ParameterizedTest
    @ValueSource(strings={"${calculated.unknown}","other"})
    @EmptySource
    @NullSource
    void calculateValue_other_unchanged(String content) throws Exception {
        /* prepare */
        when(locationSupport.ensureTestWorkingDirectoryRealPath(test)).thenReturn(testFolder);

        /* execute */
        String calculated  = calculatorToTest.calculateValue(content);

        /* test */
        assertEquals(content, calculated);

    }

    @Test
    void calculateValue_testworkingdirectory() throws Exception {
        /* prepare */
        when(locationSupport.ensureTestWorkingDirectoryRealPath(test)).thenReturn(testFolder);

        /* execute */
        String pathToTestWorkingDirectory = calculatorToTest.calculateValue(CalculatedVariables.TEST_WORKING_DIRECTORY.asExpression());

        /* test */
        assertEquals(testFolder.toString(), pathToTestWorkingDirectory);

    }

    @Test
    void calculateValue_testworkingdirectory_with_additions() throws Exception {
        /* prepare */
        when(locationSupport.ensureTestWorkingDirectoryRealPath(test)).thenReturn(testFolder);

        /* execute */
        String pathToTestFolder = calculatorToTest.calculateValue("before/"+CalculatedVariables.TEST_WORKING_DIRECTORY.asExpression()+"/somewhere");

        /* test */
        assertEquals("before/"+testFolder.toString()+"/somewhere", pathToTestFolder);

    }

}
