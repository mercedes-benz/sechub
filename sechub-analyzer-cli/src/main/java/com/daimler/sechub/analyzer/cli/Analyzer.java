package com.daimler.sechub.analyzer.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.daimler.sechub.analyzer.core.Marker;
import com.daimler.sechub.analyzer.core.MarkerPair;
import com.daimler.sechub.analyzer.core.Processor;
import com.daimler.sechub.analyzer.core.SimpleLogger;

public class Analyzer {
    private final static String APP_NAME = "analyzer";
    
    public static void main(String[] commandLineArguments) {
        String output = start(commandLineArguments);
        
        if (output != null) {
            System.out.println(output);
        }
    }
    
    // TODO: logs via log4j -> debug
    // TODO: output as JSON -> jackson?
    public static String start(String[] commandLineArguments) {
        String output = null;
        
        CommandLineParser commandLineParser = new DefaultParser();
        try {
            Options options = cliOptions();
            
            CommandLine commandLine = commandLineParser.parse(options, commandLineArguments);
            
            List<String> files = commandLine.getArgList();
            
            boolean debug = false;
            
            if (commandLine.hasOption("d")) {
                debug = true;
            }
            
            SimpleLogger.log("Detailed output.", debug);
            
            String outputFile = null;
            
            if (commandLine.hasOption("o")) {
                outputFile = commandLine.getOptionValue("o");
            }
            
            if (files.isEmpty() || commandLine.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(APP_NAME, options);
            }

            Map<String, List<MarkerPair>> result = Processor.processFiles(files, debug);
            
            if (outputFile != null) {
                writeFile(outputFile, result.toString());
            } else {
                if (!result.isEmpty()) {
                    
                    output = toJSON(result);
                }
            }
           
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        
        return output;
    }

    private static Options cliOptions() {
        Options options = new Options();
        
        Option debug = new Option("d", "debug", false, "Show a debug log.");
        options.addOption(debug);
        
        Option outputFile = new Option("o", "output-file", true, "Save the output into a file.");
        options.addOption(outputFile);
        
        Option help = new Option("h", "help", false, "Display help.");
        options.addOption(help);
        
        return options;
    }
    
    private static void writeFile(String outputFile, String content) {
        File file = new File(outputFile);
        
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"))) {
            if (!file.exists()) {
                file.createNewFile();
            }
            
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    protected static String toJSON(Map<String, List<MarkerPair>> result) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("{");
        sb.append("\"findings\": [");
        for (Map.Entry<String, List<MarkerPair>> entry : result.entrySet() ) {
            sb.append("{");
            sb.append("\"file\": \"" + entry.getKey() + "\",");
            sb.append("\"markerPairs\": [");
            
            for (MarkerPair markerPair : entry.getValue()) {
                Marker start = markerPair.getStart();
                Marker end = markerPair.getEnd();

                sb.append("\"markerPair\": {");
                sb.append("\"start\": {");
                sb.append("\"line\": " + start.getLine() + ",");
                sb.append("\"column\": " + start.getColumn() + "," );
                sb.append("},");
                sb.append("\"end\": {");
                sb.append("\"line\": "+ end.getLine() + ",");
                sb.append("\"column\": " + end.getColumn() + "," );
                sb.append("}");
                sb.append("},");
            }
            sb.append("]");
            sb.append("},");
        }
        sb.append("]");
        sb.append("}");
        
        return sb.toString();
    }
}


