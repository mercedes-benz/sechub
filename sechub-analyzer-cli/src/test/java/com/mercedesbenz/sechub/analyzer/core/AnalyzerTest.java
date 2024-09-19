// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.analyzer.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.analyzer.model.AnalyzerResult;
import com.mercedesbenz.sechub.analyzer.model.Marker;
import com.mercedesbenz.sechub.analyzer.model.MarkerPair;
import com.mercedesbenz.sechub.analyzer.model.MarkerType;

/**
 * Integration Tests
 *
 * Tests the Processor and FileAnalyzer classes
 */
public class AnalyzerTest {

    final String path = "src/test/resources/";
    private Analyzer processor;

    @Before
    public void setUp() {
        processor = new Analyzer();
    }

    @Test
    public void test_analyzeFiles__same_folders() throws FileNotFoundException {
        /* prepare */
        String rootPath = path + "test/";
        File rootFolder = new File(rootPath);
        File rootFolder2 = new File(rootPath);
        List<File> roots = new LinkedList<>();
        roots.add(rootFolder);
        roots.add(rootFolder2);

        List<MarkerPair> pairs = getMarkers();
        List<MarkerPair> pairs2 = getMarkers();

        String markedFile = rootPath + "test_pair.txt";
        String markedFile2 = rootPath + "test_pair2.txt";

        Map<String, List<MarkerPair>> expectedResult = new HashMap<>();
        expectedResult.put(markedFile, pairs);
        expectedResult.put(markedFile2, pairs2);

        /* execute */
        Map<String, List<MarkerPair>> actualResult = processor.analyzeFiles(roots);

        /* test */
        assertThat(actualResult, is(expectedResult));
    }

    @Test
    public void test_analyzeFiles__same_files() throws FileNotFoundException {
        /* prepare */
        File filePath = new File(path + "test_pair.txt");
        File filePath2 = new File(path + "test_pair.txt");
        List<File> filePaths = new LinkedList<>();
        filePaths.add(filePath);
        filePaths.add(filePath2);

        List<MarkerPair> pairs = getMarkers();
        Map<String, List<MarkerPair>> expectedResult = new HashMap<>();
        expectedResult.put(filePath.getPath(), pairs);

        /* execute */
        Map<String, List<MarkerPair>> actualResult = processor.analyzeFiles(filePaths);

        /* test */
        assertThat(actualResult, is(expectedResult));
    }

    @Test
    public void test_analyzeFiles__file_folder() throws FileNotFoundException {
        /* prepare */
        String rootPath = path + "test/";

        File folderPath = new File(rootPath);
        File filePath = new File(path + "test_pair.txt");
        List<File> roots = new LinkedList<>();
        roots.add(folderPath);
        roots.add(filePath);

        List<MarkerPair> pairs = getMarkers();
        List<MarkerPair> pairs2 = getMarkers();
        List<MarkerPair> pairs3 = getMarkers();

        String markedFile = rootPath + "test_pair.txt";
        String markedFile2 = rootPath + "test_pair2.txt";
        String markedFile3 = filePath.getPath();

        Map<String, List<MarkerPair>> expectedResult = new HashMap<>();
        expectedResult.put(markedFile, pairs);
        expectedResult.put(markedFile2, pairs2);
        expectedResult.put(markedFile3, pairs3);

        /* execute */
        Map<String, List<MarkerPair>> actualResult = processor.analyzeFiles(roots);

        /* test */
        assertThat(actualResult, is(expectedResult));
    }

    @Test
    public void test_analyzeFiles__single_file() throws FileNotFoundException {
        /* prepare */
        String filePath = path + "test_pair.txt";

        File file = new File(filePath);
        List<File> roots = new LinkedList<>();
        roots.add(file);

        List<MarkerPair> pairs = getMarkers();
        Map<String, List<MarkerPair>> expectedResult = new HashMap<>();
        expectedResult.put(filePath, pairs);

        /* execute */
        Map<String, List<MarkerPair>> actualResult = processor.analyzeFiles(roots);

        /* test */
        assertThat(actualResult, is(expectedResult));
    }

    @Test
    public void test_analyzeFiles__one_folder() throws FileNotFoundException {
        /* prepare */
        String rootPath = path + "test/";

        File rootFolder = new File(rootPath);
        List<File> roots = new LinkedList<>();
        roots.add(rootFolder);

        List<MarkerPair> pairs = getMarkers();
        List<MarkerPair> pairs2 = getMarkers();

        String markedFile = rootPath + "test_pair.txt";
        String markedFile2 = rootPath + "test_pair2.txt";

        Map<String, List<MarkerPair>> expectedResult = new HashMap<>();
        expectedResult.put(markedFile, pairs);
        expectedResult.put(markedFile2, pairs2);

        /* execute */
        Map<String, List<MarkerPair>> actualResult = processor.analyzeFiles(roots);

        /* test */
        assertThat(actualResult, is(expectedResult));
    }

    @Test
    public void test_analyzeFiles__nested_folders() throws FileNotFoundException {
        /* prepare */
        String rootPath = path + "test_nested/";

        File rootFolder = new File(rootPath);
        List<File> roots = new LinkedList<>();
        roots.add(rootFolder);

        List<MarkerPair> pairs = getMarkers();
        List<MarkerPair> pairs2 = getMarkers();

        String markedFile = rootPath + "test_pair.txt";
        String markedFile2 = rootPath + "other/test_pair.txt";

        Map<String, List<MarkerPair>> expectedResult = new HashMap<>();
        expectedResult.put(markedFile, pairs);
        expectedResult.put(markedFile2, pairs2);

        /* execute */
        Map<String, List<MarkerPair>> actualResult = processor.analyzeFiles(roots);

        /* test */
        assertThat(actualResult, is(expectedResult));
    }

    @Test
    public void test_analyzeFiles__single_file_no_markers() throws FileNotFoundException {
        /* prepare */
        String filePath = path + "test_no_markers.txt";

        File file = new File(filePath);
        List<File> roots = new LinkedList<>();
        roots.add(file);

        Map<String, List<MarkerPair>> expectedResult = new HashMap<>();

        /* execute */
        Map<String, List<MarkerPair>> actualResult = processor.analyzeFiles(roots);

        /* test */
        assertThat(actualResult, is(expectedResult));
    }

    @Test
    public void test_processFiles__notFound() {
        /* prepare */
        String rootPath = path + "notFound/";
        List<String> rootPaths = new LinkedList<>();
        rootPaths.add(rootPath);

        try {
            /* execute */
            processor.analyze(rootPaths);
            fail("Should throw an exception!");
        } catch (FileNotFoundException e) {
            /* test */
            assertThat("File not found: " + rootPath, is(e.getMessage()));
        }
    }

    @Test
    public void test_processFiles__single_file() throws FileNotFoundException {
        /* prepare */
        String filePath = path + "test_pair.txt";
        List<String> roots = new LinkedList<>();
        roots.add(filePath);

        List<MarkerPair> pairs = getMarkers();
        Map<String, List<MarkerPair>> result = new HashMap<>();
        result.put(filePath, pairs);
        AnalyzerResult expectedAnalyzerResult = new AnalyzerResult(result);

        /* execute */
        AnalyzerResult actualAnalyzerResult = processor.analyze(roots);

        /* test */
        assertThat(actualAnalyzerResult, is(expectedAnalyzerResult));
        assertThat(actualAnalyzerResult.hasResults(), is(true));
    }

    @Test
    public void test_getFiles__directory() {
        /* prepare */
        File root = new File(path + "test/");

        File file = new File(path + "test/test_pair.txt");
        File file2 = new File(path + "test/test_pair2.txt");

        Set<File> expectedFiles = new HashSet<>();
        expectedFiles.add(file);
        expectedFiles.add(file2);

        Set<File> files = new HashSet<>();

        /* execute */
        Set<File> actualFiles = processor.getFiles(root, files);

        /* test */
        assertThat(actualFiles, is(expectedFiles));
    }

    @Test
    public void test_getFiles__nested_directory() {
        /* prepare */
        File root = new File(path + "test_nested/");

        File file = new File(path + "test_nested/test_pair.txt");
        File file2 = new File(path + "test_nested/other/test_pair.txt");

        Set<File> expectedFiles = new HashSet<>();
        expectedFiles.add(file);
        expectedFiles.add(file2);

        Set<File> files = new HashSet<>();

        /* execute */
        Set<File> actualFiles = processor.getFiles(root, files);

        /* test */
        assertThat(actualFiles, is(expectedFiles));
    }

    @Test
    public void test_getFiles__file_not_found() {
        /* prepare */
        String rootPath = path + "notFound/";
        File root = new File(rootPath);

        Set<File> files = new HashSet<>();

        /* execute */
        Set<File> actualFiles = processor.getFiles(root, files);

        /* test */
        assertThat(actualFiles.isEmpty(), is(true));
    }

    /**
     * Helper method to create markers
     *
     * @return a marker pair
     */
    private static List<MarkerPair> getMarkers() {
        Marker start = new Marker(MarkerType.START, 5, 3);
        Marker end = new Marker(MarkerType.END, 11, 3);
        MarkerPair pair = new MarkerPair();
        pair.setEnd(end);
        pair.setStart(start);

        List<MarkerPair> pairs = new LinkedList<>();
        pairs.add(pair);

        return pairs;
    }
}
