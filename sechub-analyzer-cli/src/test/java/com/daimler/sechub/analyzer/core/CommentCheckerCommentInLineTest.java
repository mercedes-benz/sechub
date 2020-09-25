// SPDX-License-Identifier: MIT
package com.daimler.sechub.analyzer.core;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CommentCheckerCommentInLineTest {
    private static final String NOSECHUB = "NOSECHUB";
    private static final String NOSECHUB_END = "END-NOSECHUB";

    private CommentChecker commentChecker;

    @Parameters(name = "{index}:\"{0}\" must be found:{1} {2}")
    public static Iterable<? extends Object> data() {
        List<Object[]> combined = new LinkedList<>();

        List<String> markerVariantsToTest = createMarkerVariantsToTest();

        for (String marker : markerVariantsToTest) {
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
    
    @Parameter(2)
    public String remark;

    @Before
    public void setUp() {
        commentChecker = CommentChecker.buildFrom(NOSECHUB, NOSECHUB_END);
    }

    @Test
    public void test_isCommentInLine() {
        /* execute */
        Boolean isCommentInLine = commentChecker.isCommentInLine(line);

        /* test */
        String reason = "The line: `" + line + "` is not: " + expected+" "+remark;
        assertThat(reason, isCommentInLine, is(expected));
    }

    /* @formatter:off */
    
    /* 
     * Star comments: Java, C, JavaScript, CSS etc.
     */
    private static List<Object[]> createStarCommentsTestData(String marker) {
        List<Object[]> comments = Arrays.asList(new Object[][] {           
            add("/* " + marker + "  */", true),
            add("/* " + marker + " \n */", true, "with new line-1a"),
            add("/* " + marker + " \r */", true, "with backslash r-1b"),
            add("  /* " + marker + " */", true),
            add("/*   " + marker, true),
            add("  /** " + marker, true),
            add("  /***** " + marker, true),
            add(" /* * * * * " + marker, true),
            add(" abc /** " + marker, false),
            add(" /** abc " + marker, false),
            add("  /**" + marker, true),
            add("\t/** " + marker, true),
            add("\t\t /** " + marker, true),
            add("/** \t " + marker, true),
            add(" /** " + marker + "abc def", false),
            add(" * " + marker, true),
        });
        
        return comments;
    }
    
    /* 
     * Slash comments: Java, PostgreSQL, C etc.
     */
    private static List<Object[]> createSlashCommentsTestData(String marker) {

        List<Object[]> comments = Arrays.asList(new Object[][] {
            add(" // " + marker, true),
            add("//" + marker + "  ", true),
            add("//" + marker + "\n", true,"with new line-2a"),
            add("   //" + marker + "\n", true,"with new line-2b"),
            add("\n   //" + marker + "\n", true,"with new line-2c"),
            add("  //" + marker, true),
            add(" ////// " + marker, true),
            add("//////" + marker, true),
            add(" abc // " + marker, false),
            add(" // abc " + marker, false),
            add(" // " + marker + "abc", false),
            add("\t// " + marker + " \t abc def", true),
            add("\t\t//\t\t" + marker + "\t\t", true),
            add("//#-*" + marker + "  ", true),
            add("//###" + marker + "  ", true),
        });
        
        return comments;
    }
    
    /*
     * Pound comments: Ruby, Python etc.
     */
    private static List<Object[]> createPoundCommentsTestData(String marker) {
        List<Object[]> comments = Arrays.asList(new Object[][] {
            add(" # " + marker, true),
            add("\t#\t" + marker, true),
            add("\t\t#\t\t" + marker, true),
            add(" \t\t #\t\t  " + marker, true),
            add("   #   " + marker, true),
            add(" abc # " + marker, false),
            add(" # abc " + marker, false),
            add(" # " + marker + "abc", false),
            add("### " + marker, true),
            add(" #" + marker, true),
        });
        
        return comments;
    }
    
    /* 
     * Languages: XML, HTML etc. 
     */
    private static List<Object[]> createArrowCommentsTestData(String marker) {
        List<Object[]> comments = Arrays.asList(new Object[][] {
            add(" <!-- " + marker, true),
            add("<!-- " + marker, true),
            add(" <!---------" + marker, true),
            add("<!--    " + marker, true),
            add(" abc <!-- " + marker, false),
            add(" <!-- abc " + marker, false),
            add(" <!-- " + marker + " abc", true),
            add("\t\t<!--\t\t" + marker + "\t\t", true),
            add("\t<!--\t" + marker + "\t", true),
            add("\t<!--\t" + marker + "", true),
            add("<!--" + marker + "-->", true),
            add("<!--" + marker + "abc", false),
            add("<!-- " + marker + " -->", true),
        });
        
        return comments;
    }
    
    /* 
     * Languages: SQL 
     */
    private static List<Object[]> createDoubleDashCommentsTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            add(" -- " + marker, true),
            add("-- " + marker, true),
            add("\t\t--\t\t" + marker + "\t\t", true),
            add("\t--\t" + marker + "\t ", true),
            add("abc -- " + marker, false),
            add(" -- abc " + marker, false),
            add(" -- " + marker + "abc", false),
            add("--" + marker, true),
            add(" -- " + marker + " abc", true),
            add(" -- " + marker + "\t\tabc", true),
        });
        
        return comments;
    }
    
    /* 
     * Languages: OCaml 
     */
    private static List<Object[]> createBracketStarTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            add(" (* " + marker, true),
            add("(* " + marker, true),
            add("\t\t(*\t\t" + marker + "\t\t", true),
            add("\t(*\t" + marker + "\t ", true),
            add("abc (* " + marker, false),
            add(" (* abc " + marker, false),
            add(" (* " + marker + "abc", false),
            add("(*" + marker, true),
            add(" (* " + marker + " abc", true),
            add(" (* " + marker + "\t\tabc", true),
        });
        

        
        return comments;
    }
    
    /* 
     * Languages: PowerShell 
     */
    private static List<Object[]> createLessThanPoundTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            add(" <# " + marker, true),
            add("<# " + marker, true),
            add("\t\t<#\t\t" + marker + "\t\t", true),
            add("\t<#\t" + marker + "\t ", true),
            add("abc <# " + marker, false),
            add(" <# abc " + marker, false),
            add(" <# " + marker + "abc", false),
            add("<#" + marker, true),
            add(" <# " + marker + " abc", true),
            add(" <# " + marker + "\t\tabc", true),
        });
        
        return comments;
    }
    
    /* 
     * Languages: Scheme, Assembly
     */
    private static List<Object[]> createSemiColonTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            add(" ; " + marker, true),
            add("; " + marker, true),
            add("\t\t;\t\t" + marker + "\t\t", true),
            add("\t;\t" + marker + "\t ", true),
            add("abc ; " + marker, false),
            add(" ; abc " + marker, false),
            add(" ; " + marker + "abc", false),
            add(";" + marker, true),
            add(" ; " + marker + " abc", true),
            add(" ; " + marker + "\t\tabc", true),
        });
        
        return comments;
    }
    
    /* 
     * Languages: Matlab, Prolog
     */
    private static List<Object[]> createPercentageTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            add(" % " + marker, true),
            add("% " + marker, true),
            add("\t\t%\t\t" + marker + "\t\t", true),
            add("\t%\t" + marker + "\t ", true),
            add("abc % " + marker, false),
            add(" % abc " + marker, false),
            add(" % " + marker + "abc", false),
            add("%" + marker, true),
            add(" % " + marker + " abc", true),
            add(" % " + marker + "\t\tabc", true),
        });
        
        return comments;
    }
    
    /* 
     * Languages: Fortran
     */
    private static List<Object[]> createExclamationMarkTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            add(" ! " + marker, true),
            add("! " + marker, true),
            add("\t\t!\t\t" + marker + "\t\t", true),
            add("\t!\t" + marker + "\t ", true),
            add("abc ! " + marker, false),
            add(" ! abc " + marker, false),
            add(" ! " + marker + "abc", false),
            add("!" + marker, true),
            add(" ! " + marker + " abc", true),
            add(" ! " + marker + "\t\tabc", true),
        });
        
        return comments;
    }
    
    /* 
     * Languages: Visual Basic, Windows Batch
     */
    private static List<Object[]> createREMTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            add(" REM " + marker, true),
            add("REM " + marker, true),
            add("\t\tREM\t\t" + marker + "\t\t", true),
            add("\tREM\t" + marker + "\t ", true),
            add("abc REM " + marker, false),
            add(" REM abc " + marker, false),
            add(" REM " + marker + "abc", false),
            add("REM" + marker, true),
            add(" REM " + marker + " abc", true),
            add(" REM " + marker + "\t\tabc", true),
        });

        return comments;
    }
    
    /* 
     * Languages: Cobol, ABAB
     */
    private static List<Object[]> createStarTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            add(" * " + marker, true),
            add("* " + marker, true),
            add("\t\t*\t\t" + marker + "\t\t", true),
            add("\t*\t" + marker + "\t ", true),
            add("abc * " + marker, false),
            add(" * abc " + marker, false),
            add(" * " + marker + "abc", false),
            add("*" + marker, true),
            add(" * " + marker + " abc", true),
            add(" * " + marker + "\t\tabc", true),
        });
        
        return comments;
    }
    
    /* 
     * Languages: VisualBasic
     */
    private static List<Object[]> createSingleQuoteTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            add(" ' " + marker, true),
            add("' " + marker, true),
            add("\t\t'\t\t" + marker + "\t\t", true),
            add("\t'\t" + marker + "\t ", true),
            add("abc ' " + marker, false),
            add(" ' abc " + marker, false),
            add(" ' " + marker + "abc", false),
            add("'" + marker, true),
            add(" ' " + marker + " abc", true),
            add(" ' " + marker + "\t\tabc", true),
        });
        
        return comments;
    }
    
    /* 
     * Languages: Windows Batch
     */
    private static List<Object[]> createDoubleColonTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            add(" :: " + marker, true),
            add(":: " + marker, true),
            add("\t\t::\t\t" + marker + "\t\t", true),
            add("\t::\t" + marker + "\t ", true),
            add("abc :: " + marker, false),
            add(" :: abc " + marker, false),
            add(" :: " + marker + "abc", false),
            add("::" + marker, true),
            add(" :: " + marker + " abc", true),
            add(" :: " + marker + "\t\tabc", true),
        });
        
        return comments;
    }
    
    /* 
     * Languages: Pascal
     */
    private static List<Object[]> createCurlyBracketTestData(String marker) {               
        List<Object[]> comments = Arrays.asList(new Object[][] {
            add(" { " + marker, true),
            add("{ " + marker, true),
            add("\t\t{\t\t" + marker + "\t\t", true),
            add("\t{\t" + marker + "\t ", true),
            add("abc { " + marker, false),
            add(" { abc " + marker, false),
            add(" { " + marker + "abc", false),
            add("{" + marker, true),
            add(" { " + marker + " abc", true),
            add(" { " + marker + "\t\tabc", true),
        });
        
        return comments;
    }
    /* @formatter:on */
    
    private static List<String> createMarkerVariantsToTest() {
        return Arrays.asList(NOSECHUB, NOSECHUB_END);
    }
    
    private static Object[] add(String comment, boolean expected) {
        return add(comment,expected,null);
    }

    private static Object[] add(String comment, boolean expected, String remark) {
        return new Object[] { comment, expected,remark ==null ? "" : " ("+remark+")"};
    }
}