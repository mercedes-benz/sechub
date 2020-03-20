package com.daimler.sechub.analyzer.cli;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;

/*
 * Try to optimize the string search by using a couple of tricks
 * 1. Look if the file is empty -> do not process
 * 2. Has file a mime type we want to search for? -> process -> how to determine that?
 *    - Only process if mime type is in list
 *      - Try to optimize search
 *        - Look for '//' '/*' '--' -> comment symbols at he beginning of the line -> only process if they are there
 *        - look for nosechub and end-nosechub
 *        - use RE2/J or stringsearch (boyer-moore) or both to optimize search
 *        
 * - Work in parallel on files
 * - Option to turn on/off optimizations -> measure performance
 */
public class Application {
    final static String ARCHIVE_OPTION = "archive";
    
    public static void main(String[] commandLineArguments) {
        CommandLineParser commandLineParser = new DefaultParser();
        try {
            CommandLine commandLine = commandLineParser.parse(cliOptions(), commandLineArguments);
            String archive = commandLine.getOptionValue(ARCHIVE_OPTION);
            
            uncompressArchive(archive);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private static Options cliOptions() {
        final Option archiveOption  = Option.builder("a")
                .required(true)
                .longOpt(ARCHIVE_OPTION)
                .hasArg()
                .desc("Archive to be processed. The archive types .tar, .zip and .jar can be processed.")
                .build();
        
        Options options = new Options();
        options.addOption(archiveOption);
        
        return options;
    }
    
    private static void uncompressArchive(String archive) {
        // TODO use the archive name as directory name and create the target directory
        File targetDir = new File("uncompressed");
        
        BufferedInputStream inputStream;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(archive));
            
            ArchiveInputStream input = new ArchiveStreamFactory()
                    .createArchiveInputStream(inputStream);
            
            ArchiveEntry ae = null;
            
            while ((ae = input.getNextEntry()) != null) {
                File newFile = new File(targetDir.getPath() + "/" + ae.getName());
                
                try (OutputStream output = Files.newOutputStream(newFile.toPath())) {
                    IOUtils.copy(input, output);
                }
            }
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ArchiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
