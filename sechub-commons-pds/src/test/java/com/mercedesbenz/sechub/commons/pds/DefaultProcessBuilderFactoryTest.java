package com.mercedesbenz.sechub.commons.pds;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultProcessBuilderFactoryTest {

    private DefaultProcessBuilderFactory factoryToTest;

    @BeforeEach
    void beforeEach() {
        factoryToTest = new DefaultProcessBuilderFactory();
    }

    @Test
    void builderforCommands_creates_processbuilder() {
        /* execute */
        ProcessBuilder result = factoryToTest.createForCommands("cmd1", "cmd2");

        /* test */
        assertNotNull(result);
        List<String> commandList = result.command();
        assertTrue(commandList.contains("cmd1"));
        assertTrue(commandList.contains("cmd2"));
        assertEquals(2, commandList.size());

    }

    @Test
    void builderforCommandList_creates_processbuilder() {
        /* execute */
        ProcessBuilder result = factoryToTest.createForCommandList(Arrays.asList("cmd3", "cmd4", "cmd5"));

        /* test */
        assertNotNull(result);
        List<String> commandList = result.command();
        assertTrue(commandList.contains("cmd3"));
        assertTrue(commandList.contains("cmd4"));
        assertTrue(commandList.contains("cmd5"));
        assertEquals(3, commandList.size());

    }

}
