package com.daimler.sechub.analyzer.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Processor {

    /**
     * 
     * @param file process a file or folder
     * @return
     * @throws FileNotFoundException
     */
    public static Map<String, List<MarkerPair>> processFiles(List<String> filePaths, boolean debug) throws FileNotFoundException {
        Map<String, List<MarkerPair>> result = new HashMap<>();
        List<File> files = new LinkedList<>();
        
        for (String filePath : filePaths) {
            
            File file = new File(filePath);
    
            if (file.exists()) {
                files.add(file);
            } else {
                throw new FileNotFoundException("File not found: " + filePath);
            }
        }
        
        result = analyzeFiles(files, debug);
        
        return result;
    }

    protected static Map<String, List<MarkerPair>> analyzeFiles(List<File> rootFiles, boolean debug) {
        Map<String, List<MarkerPair>> result = new HashMap<>();

        Set<File> files = new HashSet<>();
        
        // create a list of all files
        for (File rootFile : rootFiles) {
            Set<File> filesUnderRoot = getFiles(rootFile, new HashSet<>());
            files.addAll(filesUnderRoot);
        }

        // process files
        for (File file : files) {
            try {
                SimpleLogger.log("Analyzing: " + file.getPath(), debug);
                
                List<MarkerPair> markerPairs = FileAnalyzer.processFile(file);
                
                // only add a file with findings
                if (!markerPairs.isEmpty()) {
                    result.put(file.getPath(), markerPairs);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    protected static Set<File> getFiles(File file, Set<File> files) {
        if (file.isFile()) {
            files.add(file);
        } else if (file.isDirectory()) {
            File[] filesInFolder = file.listFiles();

            for (File fileInFolder : filesInFolder) {
                getFiles(fileInFolder, files);
            }
        }

        return files;
    }
}
