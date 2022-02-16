// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.analyzer.cli;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.FileNotFoundException;

import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Analyzer CLI Integration/System Tests
 */
public class SecHubAnalyzerApplicationTest {
    final String path = "src/test/resources/";

    private SecHubAnalyzerApplication applicationToTest;

    @Rule
    @SuppressWarnings("deprecation")
    // Reason for using old style:
    // new way (Assert.assertThrow) is very uncomfortable to use and we use old way
    // currently in other SecHub parts - but there is an older junit version used
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void initialize() {
        applicationToTest = new SecHubAnalyzerApplication();
    }

    @Test
    public void test_start__single_folder() throws Exception {
        /* prepare */
        String folderPath = path + "test/";

        String[] commandLineArguments = { folderPath };

        String start = "{\"noSecHubMarkers\":{";
        String testPair = "test/test_pair.txt\":[{\"end\":{\"column\":3,";
        String testPair2 = "test/test_pair2.txt\":[{\"end\":{\"column\":3,";

        /* execute */
        String output = applicationToTest.start(commandLineArguments);

        /* test */
        assertThat(output, startsWith(start));
        assertThat(output, StringContains.containsString(testPair));
        assertThat(output, StringContains.containsString(testPair2));
    }

    @Test
    public void test_start__single_file_pretty_print() throws Exception {
        /* prepare */
        String filePath = path + "test_pair.txt";

        String[] commandLineArguments = { "-p", filePath };

        String start = "{";
        String findings = "  \"noSecHubMarkers\" : {";

        /* execute */
        String output = applicationToTest.start(commandLineArguments);

        /* test */
        assertThat(output, startsWith(start));
        assertThat(output, StringContains.containsString(findings));
    }

    @Test
    public void test_start__folder_does_not_exist() throws Exception {
        /* test */
        expected.expect(FileNotFoundException.class);

        /* prepare */
        String folderPath = path + "test/does_not_exist/";

        String[] commandLineArguments = { folderPath };

        /* execute */
        applicationToTest.start(commandLineArguments);

    }

    @Test
    public void test_start__no_arguments() throws Exception {
        /* prepare */
        String[] commandLineArguments = {};

        /* execute */
        String output = applicationToTest.start(commandLineArguments);

        /* test */
        assertThat(output, is(nullValue()));
    }

    @Test
    public void test_start__help() throws Exception {
        /* prepare */
        String[] commandLineArguments = { "-h" };

        /* execute */
        String output = applicationToTest.start(commandLineArguments);

        /* test */
        assertThat(output, is(nullValue()));
    }
}
