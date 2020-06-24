package com.daimler.sechub.analyzer.core;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.daimler.analyzer.model.Marker;
import com.daimler.analyzer.model.MarkerPair;
import com.daimler.analyzer.model.MarkerType;

public class FileAnalyzerTest {

    final String path = "src/test/resources/";

    @Test
    public void test_processFile__pair() throws IOException {
        Marker start = new Marker(MarkerType.START, 3, 3);
        Marker end = new Marker(MarkerType.END, 9, 3);
        MarkerPair pair = new MarkerPair();
        pair.setEnd(end);
        pair.setStart(start);
        List<MarkerPair> expectedPairs = new LinkedList<>();
        expectedPairs.add(pair);
        
        File file = new File(path + "test_pair.txt");
        
        List<MarkerPair> actualPairs = FileAnalyzer.getInstance().processFile(file); //SUT
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void test_processFile__multiple() throws IOException {
        List<MarkerPair> expectedPairs = new LinkedList<>();
        
        Marker start = new Marker(MarkerType.START, 4, 4);
        Marker end = new Marker(MarkerType.END, 7, 5);
        MarkerPair pair = new MarkerPair();
        pair.setEnd(end);
        pair.setStart(start);
        
        Marker start2 = new Marker(MarkerType.START, 10, 9);
        Marker end2 = new Marker(MarkerType.END, 12, 3);
        MarkerPair pair2 = new MarkerPair();
        pair2.setEnd(end2);
        pair2.setStart(start2);
        
        Marker start3 = new Marker(MarkerType.START, 15, 3);
        Marker end3 = new Marker(MarkerType.END, 18, 3);
        MarkerPair pair3 = new MarkerPair();
        pair3.setEnd(end3);
        pair3.setStart(start3);
        
        expectedPairs.add(pair);
        expectedPairs.add(pair2);
        expectedPairs.add(pair3);
        
        File file = new File(path + "test_multiple.txt");
        
        List<MarkerPair> actualPairs = FileAnalyzer.getInstance().processFile(file); //SUT
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void test_processFile__start_only() throws IOException {
        List<MarkerPair> expectedPairs = new LinkedList<>();
        
        File file = new File(path + "test_only_start.txt");
        
        List<MarkerPair> actualPairs = FileAnalyzer.getInstance().processFile(file);
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void test_processFile__end_only() throws IOException {
        List<MarkerPair> expectedPairs = new LinkedList<>();
        
        File file = new File(path + "test_only_end.txt");
        
        List<MarkerPair> actualPairs = FileAnalyzer.getInstance().processFile(file); //SUT
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void test_processFile__two_ends() throws IOException {
        Marker start = new Marker(MarkerType.START, 3, 3);
        Marker end = new Marker(MarkerType.END, 9, 3);
        MarkerPair pair = new MarkerPair();
        pair.setEnd(end);
        pair.setStart(start);
        List<MarkerPair> expectedPairs = new LinkedList<>();
        expectedPairs.add(pair);
        
        File file = new File(path + "test_two_ends.txt");
        
        List<MarkerPair> actualPairs = FileAnalyzer.getInstance().processFile(file); //SUT
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void test_processFile__two_starts() throws IOException {
        Marker start = new Marker(MarkerType.START, 3, 3);
        Marker end = new Marker(MarkerType.END, 15, 2);
        MarkerPair pair = new MarkerPair();
        pair.setEnd(end);
        pair.setStart(start);
        List<MarkerPair> expectedPairs = new LinkedList<>();
        expectedPairs.add(pair);
        
        File file = new File(path + "test_two_starts.txt");
        
        List<MarkerPair> actualPairs = FileAnalyzer.getInstance().processFile(file); //SUT
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void test_processFile__no_markers() throws IOException {
        File file = new File(path + "test_no_markers.txt");
        
        List<MarkerPair> actualPairs = FileAnalyzer.getInstance().processFile(file); //SUT
        assertThat(actualPairs.isEmpty(), is(true));
    }
    
    @Test
    public void test_processFile__same_line() throws IOException {
        File file = new File(path + "test_same_line.txt");
        
        List<MarkerPair> actualPairs = FileAnalyzer.getInstance().processFile(file);
        assertThat(actualPairs.isEmpty(), is(true));
    }
    
    @Test
    public void test_processFile__file_not_found() {
        File file = new File(path + "not_found.txt");
        
        try {
            FileAnalyzer.getInstance().processFile(file); //SUT
            fail("The file does not exist. An exception was expected.");
        } catch (FileNotFoundException e) {
            // do nothing
        } catch (IOException e) {
            fail("Unexpected exception.");
        }
    }
}
