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
            combined.addAll(createStarCommentsTestData(marker));
            combined.addAll(createSlashCommentsTestData(marker));
            combined.addAll(createPoundCommentsTestData(marker));
            combined.addAll(createArrowCommentsTestData(marker));
            combined.addAll(createDoubleDashCommentsTestData(marker));
            combined.addAll(createBracketStarTestData(marker));
            combined.addAll(createLessThanPoundTestData(marker));
            combined.addAll(createSemiColonTestData(marker));
            combined.addAll(createPercentageTestData(marker));
            combined.addAll(createExclamationMarkTestData(marker));
            combined.addAll(createREMTestData(marker));
            combined.addAll(createStarTestData(marker));
            combined.addAll(createDoubleColonTestData(marker));
            combined.addAll(createSingleQuoteTestData(marker));
            combined.addAll(createCurlyBracketTestData(marker));
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
    private static List<Object[]> createStarCommentsTestData(String marker) {
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
            {" * " + marker, true},
        });
        
        return comments;
    }
    
    /* 
     * Slash comments: Java, PostgreSQL, C etc.
     */
    private static List<Object[]> createSlashCommentsTestData(String marker) {

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
    private static List<Object[]> createPoundCommentsTestData(String marker) {
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
    private static List<Object[]> createArrowCommentsTestData(String marker) {
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
    private static List<Object[]> createDoubleDashCommentsTestData(String marker) {               
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
    
    /* 
     * Languages: OCaml 
     */
    private static List<Object[]> createBracketStarTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            {" (* " + marker, true},
            {"(* " + marker, true},
            {"\t\t(*\t\t" + marker + "\t\t", true},
            {"\t(*\t" + marker + "\t ", true},
            {"abc (* " + marker, false},
            {" (* abc " + marker, false},
            {" (* " + marker + "abc", false},
            {"(*" + marker, true },
            {" (* " + marker + " abc", true},
            {" (* " + marker + "\t\tabc", true},
        });
        

        
        return comments;
    }
    
    /* 
     * Languages: PowerShell 
     */
    private static List<Object[]> createLessThanPoundTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            {" <# " + marker, true},
            {"<# " + marker, true},
            {"\t\t<#\t\t" + marker + "\t\t", true},
            {"\t<#\t" + marker + "\t ", true},
            {"abc <# " + marker, false},
            {" <# abc " + marker, false},
            {" <# " + marker + "abc", false},
            {"<#" + marker, true },
            {" <# " + marker + " abc", true},
            {" <# " + marker + "\t\tabc", true},
        });
        
        return comments;
    }
    
    /* 
     * Languages: Scheme, Assembly
     */
    private static List<Object[]> createSemiColonTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            {" ; " + marker, true},
            {"; " + marker, true},
            {"\t\t;\t\t" + marker + "\t\t", true},
            {"\t;\t" + marker + "\t ", true},
            {"abc ; " + marker, false},
            {" ; abc " + marker, false},
            {" ; " + marker + "abc", false},
            {";" + marker, true },
            {" ; " + marker + " abc", true},
            {" ; " + marker + "\t\tabc", true},
        });
        
        return comments;
    }
    
    /* 
     * Languages: Matlab, Prolog
     */
    private static List<Object[]> createPercentageTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            {" % " + marker, true},
            {"% " + marker, true},
            {"\t\t%\t\t" + marker + "\t\t", true},
            {"\t%\t" + marker + "\t ", true},
            {"abc % " + marker, false},
            {" % abc " + marker, false},
            {" % " + marker + "abc", false},
            {"%" + marker, true },
            {" % " + marker + " abc", true},
            {" % " + marker + "\t\tabc", true},
        });
        
        return comments;
    }
    
    /* 
     * Languages: Fortran
     */
    private static List<Object[]> createExclamationMarkTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            {" ! " + marker, true},
            {"! " + marker, true},
            {"\t\t!\t\t" + marker + "\t\t", true},
            {"\t!\t" + marker + "\t ", true},
            {"abc ! " + marker, false},
            {" ! abc " + marker, false},
            {" ! " + marker + "abc", false},
            {"!" + marker, true },
            {" ! " + marker + " abc", true},
            {" ! " + marker + "\t\tabc", true},
        });
        
        return comments;
    }
    
    /* 
     * Languages: Visual Basic, Windows Batch
     */
    private static List<Object[]> createREMTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            {" REM " + marker, true},
            {"REM " + marker, true},
            {"\t\tREM\t\t" + marker + "\t\t", true},
            {"\tREM\t" + marker + "\t ", true},
            {"abc REM " + marker, false},
            {" REM abc " + marker, false},
            {" REM " + marker + "abc", false},
            {"REM" + marker, true },
            {" REM " + marker + " abc", true},
            {" REM " + marker + "\t\tabc", true},
        });

        return comments;
    }
    
    /* 
     * Languages: Cobol, ABAB
     */
    private static List<Object[]> createStarTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            {" * " + marker, true},
            {"* " + marker, true},
            {"\t\t*\t\t" + marker + "\t\t", true},
            {"\t*\t" + marker + "\t ", true},
            {"abc * " + marker, false},
            {" * abc " + marker, false},
            {" * " + marker + "abc", false},
            {"*" + marker, true },
            {" * " + marker + " abc", true},
            {" * " + marker + "\t\tabc", true},
        });
        
        return comments;
    }
    
    /* 
     * Languages: VisualBasic
     */
    private static List<Object[]> createSingleQuoteTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            {" ' " + marker, true},
            {"' " + marker, true},
            {"\t\t'\t\t" + marker + "\t\t", true},
            {"\t'\t" + marker + "\t ", true},
            {"abc ' " + marker, false},
            {" ' abc " + marker, false},
            {" ' " + marker + "abc", false},
            {"'" + marker, true },
            {" ' " + marker + " abc", true},
            {" ' " + marker + "\t\tabc", true},
        });
        
        return comments;
    }
    
    /* 
     * Languages: Windows Batch
     */
    private static List<Object[]> createDoubleColonTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            {" :: " + marker, true},
            {":: " + marker, true},
            {"\t\t::\t\t" + marker + "\t\t", true},
            {"\t::\t" + marker + "\t ", true},
            {"abc :: " + marker, false},
            {" :: abc " + marker, false},
            {" :: " + marker + "abc", false},
            {"::" + marker, true },
            {" :: " + marker + " abc", true},
            {" :: " + marker + "\t\tabc", true},
        });
        
        return comments;
    }
    
    /* 
     * Languages: Pascal
     */
    private static List<Object[]> createCurlyBracketTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            {" { " + marker, true},
            {"{ " + marker, true},
            {"\t\t{\t\t" + marker + "\t\t", true},
            {"\t{\t" + marker + "\t ", true},
            {"abc { " + marker, false},
            {" { abc " + marker, false},
            {" { " + marker + "abc", false},
            {"{" + marker, true },
            {" { " + marker + " abc", true},
            {" { " + marker + "\t\tabc", true},
        });
        
        return comments;
    }
    
    private static List<String> createMarkers() {
        List<String> markers = Arrays.asList(NOSECHUB, NOSECHUB_END);
        
        return markers;
    }
}