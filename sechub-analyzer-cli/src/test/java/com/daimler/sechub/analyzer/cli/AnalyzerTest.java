package com.daimler.sechub.analyzer.cli;

import org.junit.Before;
import org.junit.Test;

import org.hamcrest.core.StringContains;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Analyzer CLI Integration/System Tests
 */
public class AnalyzerTest {
    final String path = "src/test/resources/";
    
    private Analyzer analyzer;
    
    @Before
    public void initialize() {
        analyzer = new Analyzer("analyzer");
    }
    
    @Test
    public void test_start__single_folder() {
        /* prepare */
        String folderPath = path + "test/";
        
        String[] commandLineArguments = {folderPath};
        
        String start = "{\"noSecHubMarkers\":{";
        String testPair = "test/test_pair.txt\":[{\"end\":{\"column\":3,";
        String testPair2 = "test/test_pair2.txt\":[{\"end\":{\"column\":3,";
        
        /* execute */
        String output = analyzer.start(commandLineArguments);
        
        /* test */
        assertThat(output, startsWith(start));
        assertThat(output, StringContains.containsString(testPair));
        assertThat(output, StringContains.containsString(testPair2));
    }
    
    @Test
    public void test_start__single_file_pretty_print() {
        /* prepare */
        String filePath = path + "test_pair.txt";
        
        String[] commandLineArguments = {"-p", filePath};
        
        String start = "{";
        String findings = "  \"noSecHubMarkers\" : {";
        
        /* execute */
        String output = analyzer.start(commandLineArguments);
        
        /* test */
        assertThat(output, startsWith(start));
        assertThat(output, StringContains.containsString(findings));
    }
    
    @Test
    public void test_start__folder_does_not_exist() {
        /* prepare */
        String folderPath = path + "test/does_not_exist/";
        
        String[] commandLineArguments = {folderPath};
        
        /* execute */
        String output = analyzer.start(commandLineArguments);
        
        /* test */
        assertThat(output, is(nullValue()));
    }
    
    @Test
    public void test_start__no_arguments() {     
        /* prepare */
        String[] commandLineArguments = {};
        
        /* execute */
        String output = analyzer.start(commandLineArguments);
        
        /* test */
        assertThat(output, is(nullValue()));
    }
    
    @Test
    public void test_start__help() {
        /* prepare */
        String[] commandLineArguments = {"-h"};
        
        /* execute */
        String output = analyzer.start(commandLineArguments);
        
        /* test */
        assertThat(output, is(nullValue()));
    }
}
