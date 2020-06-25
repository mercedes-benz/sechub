package com.daimler.sechub.analyzer.cli;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

public class Main {
    private final static String APP_NAME = "analyzer";
    
    public static void main(String[] commandLineArguments) {  
        // set Log4j2 log level
        Configurator.setRootLevel(Level.WARN);
        
        Analyzer analyzer = new Analyzer(APP_NAME);
        
        String output = analyzer.start(commandLineArguments);
        
        if (output != null) {
            System.out.println(output);
        }
    }
}
