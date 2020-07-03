package com.daimler.sechub.analyzer.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@RunWith(Parameterized.class)
public class CommentCheckerCommentInLineTest {
    private static final String NOSECHUB = "NOSECHUB";
    private static final String NOSECHUB_END = "END-NOSECHUB";
    
    private CommentChecker commentChecker;
    
    @Parameters
    public static Iterable<? extends Object> data() {
        List<Object[]> combined = new LinkedList<>();
        
        List<String> markers = createMarkers();
        
        for (String marker : markers) {
            combined.addAll(createStarComments(marker));
            combined.addAll(createSlashComments(marker));
            combined.addAll(createPoundComments(marker));
            combined.addAll(createArrowComments(marker));
            combined.addAll(createDoubleDashComments(marker));
        }
           
        return combined;
    }
    
    @Parameter(0)
    public String line;
    
    @Parameter(1)
    public Boolean expected;
    
    @Before
    public void setUp() {
        commentChecker = CommentChecker.buildFrom(NOSECHUB, NOSECHUB_END);
    }
    
    @Test
    public void test_isCommentInLine() {        
        /* execute */
        Boolean isCommentInLine = commentChecker.isCommentInLine(line);
        
        /* test */
        String reason = "The line: `" + line + "` is not: " + expected;
        assertThat(reason, isCommentInLine, is(expected));
    }
    
    /* 
     * Star comments: Java, C, JavaScript, CSS etc.
     */
    private static List<Object[]> createStarComments(String marker) {
        List<Object[]> comments = Arrays.asList(new Object[][] {           
            {"/* " + marker + "  */", true},
            {"  /* " + marker + " */", true},
            {"/*   " + marker, true},
            {"  /** " + marker, true},
            {"  /***** " + marker, true},
            {" /* * * * * " + marker, true},
            {" abc /** " + marker, false},
            {" /** abc " + marker, false},
            {"  /**" + marker, true},
            {"\t/** " + marker, true},
            {"\t\t /** " + marker, true},
            {"/** \t " + marker, true},
            {" /** " + marker + "abc def", false},
            {" * " + marker, false},
        });
        
        return comments;
    }
    
    /* 
     * Slash comments: Java, PostgreSQL, C etc.
     */
    private static List<Object[]> createSlashComments(String marker) {

        List<Object[]> comments = Arrays.asList(new Object[][] {
            {" // " + marker, true},
            {"//" + marker + "  ", true},
            {"  //" + marker, true},
            {" ////// " + marker, true},
            {"//////" + marker, true},
            {" abc // " + marker, false},
            {" // abc " + marker, false},
            {" // " + marker + "abc", false},
            {"\t// " + marker + " \t abc def", true},
            {"\t\t//\t\t" + marker + "\t\t", true},
            {"//#-*" + marker + "  ", true},
            {"//###" + marker + "  ", true},
        });
        
        return comments;
    }
    
    /*
     * Pound comments: Ruby, Python etc.
     */
    private static List<Object[]> createPoundComments(String marker) {
        List<Object[]> comments = Arrays.asList(new Object[][] {
            {" # " + marker, true},
            {"\t#\t" + marker, true},
            {"\t\t#\t\t" + marker, true},
            {" \t\t #\t\t  " + marker, true},
            {"   #   " + marker, true},
            {" abc # " + marker, false},
            {" # abc " + marker, false},
            {" # " + marker + "abc", false},
            {"### " + marker, true},
            {" #" + marker, true},
        });
        
        return comments;
    }
    
    /* 
     * Languages: XML, HTML etc. 
     */
    private static List<Object[]> createArrowComments(String marker) {
        List<Object[]> comments = Arrays.asList(new Object[][] {
            {" <!-- " + marker, true},
            {"<!-- " + marker, true},
            {" <!---------" + marker, true},
            {"<!--    " + marker, true},
            {" abc <!-- " + marker, false},
            {" <!-- abc " + marker, false},
            {" <!-- " + marker + " abc", true},
            {"\t\t<!--\t\t" + marker + "\t\t", true},
            {"\t<!--\t" + marker + "\t", true},
            {"\t<!--\t" + marker + "", true},
            {"<!--" + marker + "-->", true},
            {"<!--" + marker + "abc", false},
            {"<!-- " + marker + " -->", true},
        });
        
        return comments;
    }
    
    /* 
     * Languages: SQL 
     */
    private static List<Object[]> createDoubleDashComments(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            {" -- " + marker, true},
            {"-- " + marker, true},
            {"\t\t--\t\t" + marker + "\t\t", true},
            {"\t--\t" + marker + "\t ", true},
            {"abc -- " + marker, false},
            {" -- abc " + marker, false},
            {" -- " + marker + "abc", false},
            {"--" + marker, true },
            {" -- " + marker + " abc", true},
            {" -- " + marker + "\t\tabc", true},
        });
        
        return comments;
    }
    
    private static List<String> createMarkers() {
        List<String> markers = Arrays.asList(NOSECHUB, NOSECHUB_END);
        
        return markers;
    }
}