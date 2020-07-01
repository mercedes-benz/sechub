package com.daimler.sechub.analyzer.cli;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.analyzer.model.AnalyzerResult;
import com.daimler.sechub.analyzer.core.Processor;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSON.Feature;

import ch.qos.logback.classic.Level;

import com.fasterxml.jackson.jr.ob.JSONObjectException;

public class Analyzer {
    private String applicationName;
    private boolean prettyPrint = false;
    private final static Logger logger = LoggerFactory.getLogger(Analyzer.class.getName());
    
    public Analyzer(String applicationName) {
        this.applicationName = applicationName;
    }
    
    /**
     * Checks the command line parameters and starts the analysis process
     * 
     * @param commandLineArguments
     * @return
     */
    public String start(String[] commandLineArguments) {
        String output = null;
        
        CommandLineParser commandLineParser = new DefaultParser();
        try {
            Options options = cliOptions();
            
            CommandLine commandLine = commandLineParser.parse(options, commandLineArguments);
            
            List<String> files = commandLine.getArgList();
            
            if (commandLine.hasOption("d")) {
                /* Set LogBack logging level */
                ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.ALL);
                
                /* Output message to root logger */
                Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
                logger.info("Detailed output.");
            }
            
            boolean help = false;
            
            if (files.isEmpty() || commandLine.hasOption("h")) {
                String header = "\nFind markers in files.\n\n";
                String footer = "\nPlease report issues at https://github.com/daimler/sechub";
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(applicationName, header, options, footer, true);
                help = true;
            }
            
            if (commandLine.hasOption("p")) {
                prettyPrint = true;
            }
            
            if (!help) {
                output = analyze(files);
            }

        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            logger.warn(e.getMessage());
        }
        
        return output;
    }
    
    /**
     * Starts the analysis process
     * 
     * @param files
     * @return
     * @throws FileNotFoundException
     */
    protected String analyze(List<String> files) throws FileNotFoundException {
        String output = null;
        
        Processor processor = new Processor();
        AnalyzerResult result = processor.processFiles(files);
        
        String json = null;
        
        try {
            if (prettyPrint) {
                json = JSON.std.with(Feature.PRETTY_PRINT_OUTPUT).asString(result);
            } else {
                json = JSON.std.asString(result);
            }
        } catch (JSONObjectException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        
        output = json;

        return output;
    }

    /**
     * Create a list of all CLI Options
     * 
     * @return cliOptions
     */
    private Options cliOptions() {
        Options options = new Options();
        
        Option debug = new Option("d", "debug", false, "Show additional debug messages.");
        options.addOption(debug);
        
        Option help = new Option("h", "help", false, "Display this help.");
        options.addOption(help);
        
        Option pretty_print = new Option("p", "pretty-print", false, "Format output as pretty print.");
        options.addOption(pretty_print);
        
        return options;
    }
}


