package com.daimler.sechub.analyzer.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

public class SechubAnalyzerApplication {
    private final static String APP_NAME = "analyzer";
    
    public static void main(String[] commandLineArguments) {  
        /* 
         * Set LogBack log level to Warning
         */
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.WARN);
        
        Analyzer analyzer = new Analyzer(APP_NAME);
        
        String output = analyzer.start(commandLineArguments);
        
        if (output != null) {
            System.out.println(output);
        }
    }
}
