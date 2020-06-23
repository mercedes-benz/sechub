package com.daimler.sechub.analyzer.cli;

import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

public class AnalyzerTest {
    final String path = "src/test/resources/";
    
    //TODO: More tests
    @Test
    public void test_start() {
        String folderPath = path + "test/";
        
        String[] commandLineArguments = {folderPath};
        
        String output = Analyzer.start(commandLineArguments); //SUT
        
        assertThat(output, is(not(nullValue())));
    }
}
