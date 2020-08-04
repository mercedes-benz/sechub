// SPDX-License-Identifier: MIT
package com.daimler.sechub.analyzer.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.analyzer.model.Marker;
import com.daimler.analyzer.model.MarkerPair;
import com.daimler.analyzer.model.MarkerType;

public class FileProcessorTest {

    final String path = "src/test/resources/";
    private FileProcessor analyzerToTest;
    
    @Before
    public void before() throws Exception {
        analyzerToTest = new FileProcessor();
    }

    @Test
    public void process_pair() throws IOException {
        /* prepare */
        Marker start = new Marker(MarkerType.START, 3, 3);
        Marker end = new Marker(MarkerType.END, 9, 3);
        MarkerPair pair = new MarkerPair();
        pair.setEnd(end);
        pair.setStart(start);
        List<MarkerPair> expectedPairs = new LinkedList<>();
        expectedPairs.add(pair);
        
        File file = new File(path + "test_pair.txt");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_multiple() throws IOException {
        /* prepare */
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
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_start_only() throws IOException {
        /* prepare */
        List<MarkerPair> expectedPairs = new LinkedList<>();
        
        File file = new File(path + "test_only_start.txt");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_end_only() throws IOException {
        /* prepare */
        List<MarkerPair> expectedPairs = new LinkedList<>();
        
        File file = new File(path + "test_only_end.txt");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_two_ends() throws IOException {
        /* prepare */
        Marker start = new Marker(MarkerType.START, 3, 3);
        Marker end = new Marker(MarkerType.END, 9, 3);
        MarkerPair pair = new MarkerPair();
        pair.setEnd(end);
        pair.setStart(start);
        List<MarkerPair> expectedPairs = new LinkedList<>();
        expectedPairs.add(pair);
        
        File file = new File(path + "test_two_ends.txt");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_two_starts() throws IOException {
        /* prepare */
        Marker start = new Marker(MarkerType.START, 3, 3);
        Marker end = new Marker(MarkerType.END, 15, 2);
        MarkerPair pair = new MarkerPair();
        pair.setEnd(end);
        pair.setStart(start);
        List<MarkerPair> expectedPairs = new LinkedList<>();
        expectedPairs.add(pair);
        
        File file = new File(path + "test_two_starts.txt");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_c_single_comment() throws IOException {
        /* prepare */
        String codePath = path + "code/C/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(4, 5, 6, 5);
        
        File file = new File(codePath + "single_line.c");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_c_multiline_comment() throws IOException {
        /* prepare */
        String codePath = path + "code/C/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(4, 5, 8, 5);
        
        File file = new File(codePath + "multi_line.c");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_c_multiline_comment_comment_not_beginning() throws IOException {
        /* prepare */
        String codePath = path + "code/C/";
        
        List<MarkerPair> expectedPairs = new LinkedList<>();
        
        File file = new File(codePath + "multi_line_comment_not_beginning.c");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_java_multiline_comment() throws IOException {
        /* prepare */
        String codePath = path + "code/Java/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(5, 11, 7, 11);
        
        File file = new File(codePath + "MultiLineComment.java");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_java_multiline_comment_not_beginning() throws IOException {
        /* prepare */
        String codePath = path + "code/Java/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(6, 11, 10, 11);
        
        File file = new File(codePath + "MultiLineCommentNotBeginning.java");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_java_single_line() throws IOException {
        /* prepare */
        String codePath = path + "code/Java/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(6, 9, 8, 9);
        
        File file = new File(codePath + "SingleLineComment.java");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_abap_single_comment() throws IOException {
        /* prepare */
        String codePath = path + "code/ABAP/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(4, 6, 6, 6);
        
        File file = new File(codePath + "single_line_star.abap");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_vbnet_single_comment() throws IOException {
        /* prepare */
        String codePath = path + "code/VB.NET/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(3, 10, 5, 10);
        
        File file = new File(codePath + "single_line.vb");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_ada_single_comment() throws IOException {
        /* prepare */
        String codePath = path + "code/ADA/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(4, 6, 6, 6);
        
        File file = new File(codePath + "single_line.adb");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_assembly_single_comment() throws IOException {
        /* prepare */
        String codePath = path + "code/Assembly/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(9, 5, 11, 5);
        
        File file = new File(codePath + "single_line.asm");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_batch_single_comment_double_colon() throws IOException {
        /* prepare */
        String codePath = path + "code/Batch/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(2, 3, 4, 3);
        
        File file = new File(codePath + "single_line_double_colon.bat");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_batch_single_comment_REM() throws IOException {
        /* prepare */
        String codePath = path + "code/Batch/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(2, 4, 4, 4);
        
        File file = new File(codePath + "single_line_REM.bat");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_fortran_single_line() throws IOException {
        /* prepare */
        String codePath = path + "code/Fortran/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(2, 4, 5, 4);
        
        File file = new File(codePath + "single_line.f90");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_ocaml_single_line() throws IOException {
        /* prepare */
        String codePath = path + "code/OCaml/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(1, 3, 3, 3);
        
        File file = new File(codePath + "single_line.ml");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_pascal_single_line() throws IOException {
        /* prepare */
        String codePath = path + "code/Pascal/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(3, 6, 5, 6);
        
        File file = new File(codePath + "single_line.p");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    
    @Test
    public void process_python_single_line() throws IOException {
        /* prepare */
        String codePath = path + "code/Python/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(4, 4, 6, 4);
        
        File file = new File(codePath + "single_line_comment.py");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_python_multiple_comments() throws IOException {
        /* prepare */
        String codePath = path + "code/Python/";
        
        List<MarkerPair> expectedPairs = new LinkedList<>();
        
        List<MarkerPair> pair = createMarkerPairsOf(5, 4, 7, 4);
        List<MarkerPair> pair2 = createMarkerPairsOf(11, 8, 13, 4);
        
        expectedPairs.addAll(pair);
        expectedPairs.addAll(pair2);
        
        File file = new File(codePath + "single_line_comment_multiple.py");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_python_single_line_wrong() throws IOException {
        /* prepare */
        String codePath = path + "code/Python/";
        
        List<MarkerPair> expectedPairs = new LinkedList<>();
        
        File file = new File(codePath + "single_line_comment_wrong.py");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    
    @Test
    public void process_ruby_single_line() throws IOException {
        /* prepare */
        String codePath = path + "code/Ruby/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(3, 4, 5, 4);
        
        File file = new File(codePath + "single_line_comment.rb");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    
    @Test
    public void process_scheme_single_line() throws IOException {
        /* prepare */
        String codePath = path + "code/Scheme/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(1, 2, 3, 2);
        
        File file = new File(codePath + "single_line_comment.scm");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_shell_single_line() throws IOException {
        /* prepare */
        String codePath = path + "code/Shell/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(4, 4, 6, 4);
        
        File file = new File(codePath + "single_line_comment.sh");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_sql_single_line() throws IOException {
        /* prepare */
        String codePath = path + "code/SQL/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(2, 3, 4, 3);
        
        File file = new File(codePath + "single_line_comment.sql");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_tcl_single_line() throws IOException {
        /* prepare */
        String codePath = path + "code/Tcl/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(1, 2, 3, 2);
        
        File file = new File(codePath + "single_line_comment.tcl");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }
    
    @Test
    public void process_xml_single_line() throws IOException {
        /* prepare */
        String codePath = path + "code/XML/";
        
        List<MarkerPair> expectedPairs = createMarkerPairsOf(11, 8, 16, 8);
        
        File file = new File(codePath + "single_line_comment.xml");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs, is(expectedPairs));
    }

    @Test
    public void process_no_markers() throws IOException {
        /* prepare */
        File file = new File(path + "test_no_markers.txt");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs.isEmpty(), is(true));
    }
    
    @Test
    public void process_same_line() throws IOException {
        /* prepare */
        File file = new File(path + "test_same_line.txt");
        
        /* execute */
        List<MarkerPair> actualPairs = analyzerToTest.processFile(file);
        
        /* test */
        assertThat(actualPairs.isEmpty(), is(true));
    }
    
    @Test
    public void process_file_not_found() {
        /* prepare */
        File file = new File(path + "not_found.txt");
        String exceptionMessage = file.getPath() + " (No such file or directory)";
        
        try {
            /* execute */
            analyzerToTest.processFile(file);
            fail("The file does not exist. An exception was expected.");
        } catch (FileNotFoundException e) {
            /* test */
            assertThat(e.getMessage(), is(exceptionMessage));
        } catch (IOException e) {
            fail("Unexpected exception.");
        }
    }
    
    private List<MarkerPair> createMarkerPairsOf(int startLine, int startColumn, int endLine, int endColumn) {
        Marker start = new Marker(MarkerType.START, startLine, startColumn);
        Marker end = new Marker(MarkerType.END, endLine, endColumn);
        MarkerPair pair = new MarkerPair();
        pair.setEnd(end);
        pair.setStart(start);
        List<MarkerPair> pairs = new LinkedList<>();
        pairs.add(pair);
        
        return pairs;
    }
}
