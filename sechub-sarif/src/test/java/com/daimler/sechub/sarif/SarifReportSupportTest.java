// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.integrationtest.TextFileReader;
import com.daimler.sechub.sarif.model.CodeFlow;
import com.daimler.sechub.sarif.model.Level;
import com.daimler.sechub.sarif.model.PropertyBag;
import com.daimler.sechub.sarif.model.Report;
import com.daimler.sechub.sarif.model.ReportingConfiguration;
import com.daimler.sechub.sarif.model.Result;
import com.daimler.sechub.sarif.model.Rule;
import com.daimler.sechub.sarif.model.Run;

class SarifReportSupportTest {

    private static final Logger LOG = LoggerFactory.getLogger(SarifReportSupportTest.class);

    private static FilenameFilter sarifFileEndingFilter;
    private static TextFileReader reader;

    private static File sarifTutorialSamplesFolder;
    private static File sarifSpecificationSnippetsFolder;
    private static File sarifBrakemanFolder;

    private SarifReportSupport supportToTest;

    private Object openSourceData;

    @BeforeAll
    static void init() {
        sarifFileEndingFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".sarif") || name.endsWith(".sarif.json");
            }
        };
        reader = new TextFileReader();

        sarifTutorialSamplesFolder = new File("./src/test/resources/examples/microsoft/sarif-tutorials/samples");
        sarifSpecificationSnippetsFolder = new File("./src/test/resources/examples/specification");
        sarifBrakemanFolder = new File("./src/test/resources/examples/brakeman");
    }

    @BeforeEach
    void beforeEach() {
        supportToTest = new SarifReportSupport();
    }

    @Test
    void brakeman_sarif_example_with_tags_can_be_loaded() throws IOException {
        /* prepare */
        File folder = sarifBrakemanFolder;

        /* execute +test */
        testReports(folder, 1, "2.1.0");
    }

    @Test
    void specification_examples_can_all_be_loaded() throws IOException {
        /* prepare */
        File folder = sarifSpecificationSnippetsFolder;

        /* execute +test */
        testReports(folder, 1, "2.1.0");

    }

    @Test
    void specification_properties_snippet_properties_contains_tags() throws IOException {
        /* prepare */
        File folder = sarifSpecificationSnippetsFolder;

        /* execute */
        Report report = supportToTest.loadReport(new File(folder, "specification-properties-snippet.sarif.json"));

        /* test */
        List<Result> results = report.getRuns().iterator().next().getResults();
        Result result = results.iterator().next();
        PropertyBag properties = result.getProperties();
        assertNotNull(properties);
        Object tags = properties.get("tags");
        assertEquals(Collections.singleton("openSource"), tags);

    }

    @Test
    void specification_properties_snippet_properties_contains_opensource_key_and_map_value() throws IOException {
        /* prepare */
        File folder = sarifSpecificationSnippetsFolder;

        /* execute */
        Report report = supportToTest.loadReport(new File(folder, "specification-properties-snippet.sarif.json"));

        /* test */
        List<Result> results = report.getRuns().iterator().next().getResults();
        Result result = results.iterator().next();
        PropertyBag properties = result.getProperties();
        assertNotNull(properties);
        openSourceData = properties.get("openSource");
        if (openSourceData instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) openSourceData;
            String informationUri = (String) map.get("informationUri");
            assertEquals("http://www.example.com/procedures/usingOpenSource.html", informationUri);
        } else {
            fail("expected map but found:" + openSourceData);
        }

    }

    @Test
    void specification_properties_snippet_properties_contains_opensource_key_and_map_value_and_can_be_written() throws IOException {
        /* prepare */
        File folder = sarifSpecificationSnippetsFolder;

        /* execute */
        Report report = supportToTest.loadReport(new File(folder, "specification-properties-snippet.sarif.json"));

        /* test */
        List<Result> results = report.getRuns().iterator().next().getResults();
        Result result = results.iterator().next();
        PropertyBag properties = result.getProperties();
        assertNotNull(properties);
        openSourceData = properties.get("openSource");
        if (openSourceData instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) openSourceData;
            String informationUri = (String) map.get("informationUri");
            assertEquals("http://www.example.com/procedures/usingOpenSource.html", informationUri);
        } else {
            fail("expected map but found:" + openSourceData);
        }

    }

    @Test
    void microsoft_sarif_tutorial_samples_can_all_be_loaded() throws IOException {
        /* prepare */
        File folder = sarifTutorialSamplesFolder;

        /* execute +test */
        testReports(folder, 14, "2.1.0");

    }

    @Test
    void microsoft_sarif_tutorial_samples_1_introduction_can_all_be_loaded() throws IOException {

        /* prepare */
        File folder = new File(sarifTutorialSamplesFolder, "1-Introduction");

        /* execute +test */
        testReports(folder, 1, "2.1.0");

    }

    @Test
    void microsoft_sarif_tutorial_samples_2_basics_can_all_be_loaded() throws IOException {

        /* prepare */
        File folder = new File(sarifTutorialSamplesFolder, "2-Basics");

        /* execute +test */
        testReports(folder, 1, "2.1.0");

    }

    @Test
    void microsoft_sarif_tutorial_samples_3_beyond_basics_can_all_be_loaded() throws IOException {

        /* prepare */
        File folder = new File(sarifTutorialSamplesFolder, "3-Beyond-basics");

        /* execute +test */
        testReports(folder, 8, "2.1.0");

    }

    private void testReports(File folder, int expectedCount, String expectedSarifVersion) throws IOException {
        int count = 0;
        for (File file : folder.listFiles(sarifFileEndingFilter)) {
            /* prepare */
            LOG.info("Reading sarif report:{}", file);
            count++;

            String sarifJson = reader.loadTextFile(file);
            assertNotNull(sarifJson);

            /* execute */
            Report report = supportToTest.loadReport(sarifJson);

            /* test */
            assertNotNull(report);
            assertEquals(expectedSarifVersion, report.getVersion());

        }
        /* sanity check */
        assertEquals(expectedCount, count, "Not amount of expected files were read as sarif report!");
    }

    @Test
    void brakeman_sarif_example_with_tags__tags_can_be_fetched() throws IOException {
        /* prepare */
        File codeFlowReportFile = new File(sarifBrakemanFolder, "sarif_2_1_0__brakeman_testfile_with_tags.sarif.json");

        /* execute */
        Report report = supportToTest.loadReport(codeFlowReportFile);

        /* test */
        List<Run> runs = report.getRuns();
        assertEquals(1, runs.size(), "there must be ONE run!");
        Run run = runs.iterator().next();
        List<Result> results = run.getResults();
        assertEquals(32, results.size(), "there must be 32 results!");
        Result result = results.iterator().next();

        Rule rule = supportToTest.fetchRuleForResult(result, run);
        Set<String> tags = rule.getProperties().fetchTags();
        assertNotNull(tags);

        Set<String> expected = new LinkedHashSet<>();
        expected.add("ContentTag");
        expected.add("Tag2");
        expected.add("Tag3");
        assertEquals(expected, tags);
    }

    @Test
    void microsoft_sarif_tutorial_codeflow_example() throws IOException {
        /* prepare */
        File codeFlowReportFile = new File(sarifTutorialSamplesFolder, "CodeFlows.sarif");

        /* execute */
        Report report = supportToTest.loadReport(codeFlowReportFile);

        /* test */
        List<Run> runs = report.getRuns();
        assertEquals(1, runs.size(), "there must be ONE run!");
        Run run = runs.iterator().next();
        List<Result> results = run.getResults();
        assertEquals(1, results.size(), "there must be ONE result!");
        Result result = results.iterator().next();
        assertEquals("TUT1001", result.getRuleId());
        assertEquals("Use of uninitialized variable.", result.getMessage().getText());

        List<CodeFlow> codeFlows = result.getCodeFlows();
        assertEquals(2, codeFlows.size());
    }

    @Test
    void microsoft_sarif_tutorial_taxonomies_example__result_messages() throws IOException {
        /* prepare */
        File codeFlowReportFile = new File(sarifTutorialSamplesFolder, "Taxonomies.sarif");

        /* execute */
        Report report = supportToTest.loadReport(codeFlowReportFile);

        /* test */
        List<Run> runs = report.getRuns();
        assertEquals(1, runs.size(), "there must be ONE run!");
        Run run = runs.iterator().next();

        List<Result> results = run.getResults();
        assertEquals(2, results.size(), "there must be two result!");
        Iterator<Result> iterator = results.iterator();

        // sort results by tree map, so we can fetch wanted ones
        Map<String, Result> sortedMap = new TreeMap<>();
        Result result = iterator.next();
        sortedMap.put(result.getRuleId(), result);
        result = iterator.next();
        sortedMap.put(result.getRuleId(), result);

        Result result1 = sortedMap.get("TUT1001");
        assertNotNull(result1);
        assertEquals("TUT1001", result1.getRuleId());
        assertEquals("This result violates a rule that is classified as 'Required'.", result1.getMessage().getText());

        Result result2 = sortedMap.get("TUT1002");
        assertNotNull(result2);
        assertEquals("TUT1002", result2.getRuleId());
        assertEquals("This result violates a rule that is classified as 'Recommended'.", result2.getMessage().getText());
    }

    @Test
    void microsoft_sarif_tutorial_taxonomies_example__result_defaultocnfiguraiton_level() throws IOException {
        /* prepare */
        File codeFlowReportFile = new File(sarifTutorialSamplesFolder, "Taxonomies.sarif");

        /* execute */
        Report report = supportToTest.loadReport(codeFlowReportFile);

        /* test */
        List<Run> runs = report.getRuns();
        assertEquals(1, runs.size(), "there must be ONE run!");
        Run run = runs.iterator().next();

        List<Rule> rules = run.getTool().getDriver().getRules();
        Map<String, Rule> sortedMap = new TreeMap<>();
        for (Rule rule : rules) {
            sortedMap.put(rule.getId(), rule);
        }
        Rule rule1 = sortedMap.get("TUT0001");
        assertNotNull(rule1);
        ReportingConfiguration defaultConfig1 = rule1.getDefaultConfiguration();
        assertNotNull(defaultConfig1);
        assertEquals(Level.ERROR, defaultConfig1.getLevel());

        Rule rule2 = sortedMap.get("TUT0002");
        assertNotNull(rule2);
        ReportingConfiguration defaultConfig2 = rule2.getDefaultConfiguration();
        assertNotNull(defaultConfig2);
        assertEquals(Level.WARNING, defaultConfig2.getLevel());
    }

}
