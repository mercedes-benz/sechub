// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.analyzer.cli;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.analyzer.core.Analyzer;
import com.mercedesbenz.sechub.analyzer.model.AnalyzerResult;

import ch.qos.logback.classic.Level;

public class SecHubAnalyzerApplication {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubAnalyzerApplication.class);

    private final static String APP_NAME = "analyzer";

    public static void main(String[] commandLineArguments) {
        /*
         * Set LogBack log level to Warning
         */
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.WARN);

        SecHubAnalyzerApplication application = new SecHubAnalyzerApplication();

        try {
            String output = application.start(commandLineArguments);
            if (output != null) {
                System.out.println(output);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            // we do system exit here - and nowhere else, so tests have no problems..
            System.exit(1);
        }

    }

    /**
     * Checks the command line parameters and starts the analysis process
     *
     * @param analyzer
     *
     * @param commandLineArguments
     * @return result as JSON or <code>null</code>
     */
    String start(String[] commandLineArguments) throws Exception {

        Analyzer analyzer = new Analyzer();

        CommandLineParser commandLineParser = new DefaultParser();
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

        if (files.isEmpty() || commandLine.hasOption("h")) {
            String header = "\nFind markers in files.\n\n";
            String footer = "\nPlease report issues at https://github.com/mercedes-benz/sechub";
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(APP_NAME, header, options, footer, true);
            return null;
        }

        boolean prettyPrint = false;
        if (commandLine.hasOption("p")) {
            prettyPrint = true;
        }

        AnalyzerResult result = analyzer.analyze(files);
        if (result == null) {
            return null;
        }
        return result.ToJSON(prettyPrint);
    }

    /*
     * Create a list of all CLI Options
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
