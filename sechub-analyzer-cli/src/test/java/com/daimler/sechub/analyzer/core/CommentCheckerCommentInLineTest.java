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
        
        combined.addAll(createStarComments(NOSECHUB));
        combined.addAll(createStarComments(NOSECHUB_END));
         
           /* Slash comments: Java, PostgreSQL, C etc */
           List<Object[]> slashComments = 
                   Arrays.asList(new Object[][] {
                       {" // " + NOSECHUB, true},
                       {"//" + NOSECHUB + "  ", true},
                       {"  //" + NOSECHUB, true}
                   });
           
           /* Pound comments: Ruby, Python etc. */
           List<Object[]> poundComments = 
                   Arrays.asList(new Object[][] {
                       {" # " + NOSECHUB, true}
                   });
           
           /* Languages: XML, HTML etc. */
           List<Object[]> arrowComments = 
                   Arrays.asList(new Object[][] {
                       {" <!-- " + NOSECHUB, true},
                       {"<!-- " + NOSECHUB, true},
                       {" <!--------- " + NOSECHUB, true},
                       {"<!--    " + NOSECHUB, true},
                       {" abc <!-- " + NOSECHUB, false},
                       {" <!-- abc " + NOSECHUB, false},
                       {" <!-- " + NOSECHUB + " abc", true}
                   });
           
           /* Languages: SQL */
           List<Object[]> doubleDashComments = 
                   Arrays.asList(new Object[][] {
                       {" -- " + NOSECHUB, true }
                   });
           

//           combined.addAll(starComments);
//           combined.addAll(slashComments);
//           combined.addAll(poundComments);
//           combined.addAll(arrowComments);
//           combined.addAll(doubleDashComments);

           return combined;
    }
    
    @Parameter(0)
    public String line;
    
    @Parameter(1)
    public Boolean expected;
    
    @Before
    public void setUp() {
        commentChecker = CommentChecker.of(NOSECHUB, NOSECHUB_END);
    }
    
    @Test
    public void test_isCommentInLine() {        
        /* execute */
        Boolean isCommentInLine = commentChecker.isCommentInLine(line);
        
        /* test */
        assertThat(isCommentInLine, is(expected));
    }
    
    /* Star comments: Java, C, JavaScript, CSS etc. */
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
            {"  /**" + marker, true}
        });
        
        return comments;
    }
}