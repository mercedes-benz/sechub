package com.daimler.sechub.analyzer.cli;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class Main {
    private final static String APP_NAME = "analyzer";
    
    public static void main(String[] commandLineArguments) {  
//        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
//        RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.ERROR);
//        rootLogger.add(builder.newAppenderRef("stdout"));
        
        Configurator.setRootLevel(Level.WARN);
        
        Analyzer analyzer = new Analyzer(APP_NAME);
        
        String output = analyzer.start(commandLineArguments);
        
        if (output != null) {
            System.out.println(output);
        }
    }
}
