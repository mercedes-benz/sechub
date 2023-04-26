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

import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.test.TestUtil;

class CurrentTestDynamicVariableCalculatorTest {

    private SystemTestRuntimeContext context;
    private TestDefinition test;
    private CurrentTestDynamicVariableCalculator calculatorToTest;
    private LocationSupport locationSupport;
    private Path testFolder;

    @BeforeEach
    void beforeEach() throws Exception {

        testFolder = TestUtil.createTempDirectoryInBuildFolder("testFolder");

        context = mock(SystemTestRuntimeContext.class);
        test = mock(TestDefinition.class);
        locationSupport = mock(LocationSupport.class);

        when(context.getLocationSupport()).thenReturn(locationSupport);

        calculatorToTest = new CurrentTestDynamicVariableCalculator(test, context);
    }

    @ParameterizedTest
    @ValueSource(strings={"${calculated.unknown}","other"})
    @EmptySource
    @NullSource
    void calculateValue_other_unchanged(String content) throws Exception {
        /* prepare */
        when(locationSupport.ensureTestFolder(test)).thenReturn(testFolder);

        /* execute */
        String calculated  = calculatorToTest.calculateValue(content);

        /* test */
        assertEquals(content, calculated);

    }

    @Test
    void calculateValue_currentTestFolder() throws Exception {
        /* prepare */
        when(locationSupport.ensureTestFolder(test)).thenReturn(testFolder);

        /* execute */
        String pathToTestFolder = calculatorToTest.calculateValue("${calculated.currentTestFolder}");

        /* test */
        assertEquals(testFolder.toString(), pathToTestFolder);

    }

    @Test
    void calculateValue_currentTestFolder_with_additions() throws Exception {
        /* prepare */
        when(locationSupport.ensureTestFolder(test)).thenReturn(testFolder);

        /* execute */
        String pathToTestFolder = calculatorToTest.calculateValue("before/${calculated.currentTestFolder}/somewhere");

        /* test */
        assertEquals("before/"+testFolder.toString()+"/somewhere", pathToTestFolder);

    }

}
