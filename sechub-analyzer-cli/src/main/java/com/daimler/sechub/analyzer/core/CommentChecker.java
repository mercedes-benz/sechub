package com.daimler.sechub.analyzer.core;

//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

public class CommentChecker {
    private String noSecHubLabel;
    private String endNoSecHubLabel;
    
    private CommentChecker(String noSecHubLabel, String endNoSecHubLabel) {
        this.noSecHubLabel = noSecHubLabel;
        this.endNoSecHubLabel = endNoSecHubLabel;
    }
    
    public static CommentChecker of(String noSecHubLabel, String endNoSecHubLabel) {
        return new CommentChecker(noSecHubLabel, endNoSecHubLabel);
    }
    
    public Boolean isCommentInLine(String line) {
        String regex = "^(\\s+)?(<!--|--|#|/\\*|//)([-*/ ]+)?(" + noSecHubLabel + "|" + endNoSecHubLabel + ")" ;
        
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        Boolean matches = matcher.lookingAt();
                
        return matches;
    }
    
    public String getNoSecHubLabel() {
        return noSecHubLabel;
    }
}
