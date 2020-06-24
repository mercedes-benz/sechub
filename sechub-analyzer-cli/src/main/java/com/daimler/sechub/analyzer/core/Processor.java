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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.daimler.analyzer.model.AnalyzerResult;
import com.daimler.analyzer.model.MarkerPair;

public class Processor {
    private final static Logger logger = LogManager.getLogger(Processor.class.getName());
    
    public Processor() {}
    
    /**
     * Processes the given files
     * 
     * @param file process a file or folder
     * @return
     * @throws FileNotFoundException
     */
    public AnalyzerResult processFiles(List<String> filePaths) throws FileNotFoundException {
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
        
        result = analyzeFiles(files);
        
        AnalyzerResult analyzerResult = new AnalyzerResult(result);
        
        return analyzerResult;
    }

    /**
     * Analyze files
     * 
     * Creates a list of all files which have be be analyzed.
     * Starts the file analysis of all files.
     * 
     * @param rootFiles
     * @return
     */
    protected Map<String, List<MarkerPair>> analyzeFiles(List<File> rootFiles) {
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
                logger.debug("Analyzing: " + file.getPath());
                 
                List<MarkerPair> markerPairs = FileAnalyzer.getInstance().processFile(file);
                
                // only add a file with findings
                if (!markerPairs.isEmpty()) {
                    result.put(file.getPath(), markerPairs);
                }

            } catch (FileNotFoundException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return result;
    }

    /**
     * Traverses the file system from a given file (root)
     * 
     * The file system is traversed recursively.
     * 
     * @param file the root folder or file
     * @param files a set of files
     * @return
     */
    protected Set<File> getFiles(File file, Set<File> files) {
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
