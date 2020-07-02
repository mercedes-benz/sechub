package com.daimler.sechub.analyzer.core;

//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

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
        String regex = "^(\\s+)?(<!--|--|#|/\\*|//)([-*/ ]+)?(" + noSecHubLabel + "|" + endNoSecHubLabel + ")";
        pattern = Pattern.compile(regex);
    }
    
    public static CommentChecker of(String noSecHubLabel, String endNoSecHubLabel) {
        return new CommentChecker(noSecHubLabel, endNoSecHubLabel);
    }
    
    public Boolean isCommentInLine(String line) {        
        Matcher matcher = pattern.matcher(line);
        Boolean matches = matcher.lookingAt();
                
        return matches;
    }
    
    public String getNoSecHubLabel() {
        return noSecHubLabel;
    }
}
