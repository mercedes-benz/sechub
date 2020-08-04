package com.daimler.sechub.analyzer.core;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

public class CommentChecker {
    private String noSecHubLabel;
    private String endNoSecHubLabel;
    
    private Pattern pattern;
    
    private CommentChecker(String noSecHubLabel, String endNoSecHubLabel) {
        this.noSecHubLabel = noSecHubLabel;
        this.endNoSecHubLabel = endNoSecHubLabel;
        
        initialize();
    }
    
    private void initialize() {
        /*
         * Explanation:
         * 
         * ^(\\+)? -> the string can start with a white spaces
         * 
         * (<!--|--|#|/\\*|//) -> the string has to contain a comment. Different programming languages use different comment styles.
         * 
         * ([-#/*\\s]+)? -> the string can contain further comment symbols or white spaces. ATTENTION: The `-` has to be at the beginning of the group. 
         * 
         * (" + noSecHubLabel + "|" + endNoSecHubLabel + ") -> look for SecHub marker labels.
         * 
         * (\\s|-|$)+ -> accepts a line end as well as any whitespace or `-` minus symbols after the SecHub marker label.
         */
        String regex = "^(\\s+)?(<!--|--|#|/\\*|//|\\(\\*|<#|;|!|%|REM|::|\\*|'|{)([-#/*\\s]+)?(" + noSecHubLabel + "|" + endNoSecHubLabel + ")(\\s|-|$)+";
        pattern = Pattern.compile(regex);
    }
    
    /**
     * Builds a new comment checker based on the given labels
     * 
     * The comment check compiles a regular expression. 
     * Make sure you do not call the buildFrom method too many times,
     * otherwise it may cause performance issues.
     * 
     * @param noSecHubLabel  the marker start label
     * @param endNoSecHubLabel  the marker end label
     * @return CommentChecker
     */
    public static CommentChecker buildFrom(String noSecHubLabel, String endNoSecHubLabel) {
        return new CommentChecker(noSecHubLabel, endNoSecHubLabel);
    }
    
    /**
     * Checks if their is a comment in the given line.
     * 
     * @param line  a string to check for a label
     * @return true if the string contains a comment otherwise false
     */
    public boolean isCommentInLine(String line) {        
        Matcher matcher = pattern.matcher(line);
        return matcher.lookingAt();
    }

    public String getNoSecHubLabel() {
        return noSecHubLabel;
    }
}
