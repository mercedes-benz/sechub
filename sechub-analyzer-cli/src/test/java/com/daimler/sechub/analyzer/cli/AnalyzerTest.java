package com.daimler.sechub.analyzer.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertThat;

import org.hamcrest.core.StringContains;

import static org.hamcrest.Matchers.*;

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
        String folderPath = path + "test/";
        
        String[] commandLineArguments = {folderPath};
        
        String output = analyzer.start(commandLineArguments); //SUT
        
        String start = "{\"findings\":{";
        String testPair = "test/test_pair.txt\":[{\"end\":{\"column\":3,";
        String testPair2 = "test/test_pair2.txt\":[{\"end\":{\"column\":3,";

        assertThat(output, startsWith(start));
        assertThat(output, StringContains.containsString(testPair));
        assertThat(output, StringContains.containsString(testPair2));
    }
    
    @Test
    public void test_start__single_file_pretty_print() {
        String filePath = path + "test_pair.txt";
        
        String[] commandLineArguments = {"-p", filePath};
        
        String output = analyzer.start(commandLineArguments); //SUT
        
        String start = "{";
        String findings = "  \"findings\" : {";

        assertThat(output, startsWith(start));
        assertThat(output, StringContains.containsString(findings));
    }
    
    @Test
    public void test_start__folder_does_not_exist() {
        String folderPath = path + "test/does_not_exist/";
        
        String[] commandLineArguments = {folderPath};
        
        String output = analyzer.start(commandLineArguments); //SUT
        
        assertThat(output, is(nullValue()));
    }
    
    @Test
    public void test_start__no_arguments() {       
        String[] commandLineArguments = {};
        
        String output = analyzer.start(commandLineArguments); //SUT
        
        assertThat(output, is(nullValue()));
    }
    
    @Test
    public void test_start__help() {       
        String[] commandLineArguments = {"-h"};
        
        String output = analyzer.start(commandLineArguments); //SUT
        
        assertThat(output, is(nullValue()));
    }
}
