package com.daimler.sechub.analyzer.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Analyzes a file for SecHub marker.
 */
public class FileAnalyzer {
    private static final String NOSECHUB = "NOSECHUB";
    private static final String NOSECHUB_END = "END-NOSECHUB";

    /**
     * Search through a given file for SecHub marker and returns a list with marker.
     * 
     * Markers: 
     * 
     * @param file
     * @return List<MarerPair> a list of marker pairs
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<MarkerPair> processFile(File file) throws FileNotFoundException, IOException {
        List<MarkerPair> markerPairs = new LinkedList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line = null;
            int lineNumber = 0;

            Marker start = null;
            Marker end = null;

            // iterate through every line in the file
            while ((line = bufferedReader.readLine()) != null) {
                lineNumber++;

                // search for the NOSECHUB marker
                int noSecHub = line.indexOf(NOSECHUB);

                if (noSecHub > -1) {
                    // search for NOSECHB_END marker
                    int endNoSecHub = line.indexOf(NOSECHUB_END);

                    if (endNoSecHub > -1) {
                        end = new Marker(MarkerType.END, lineNumber, endNoSecHub);
                    } else {
                        // only set a new start marker if no previous start was found
                        if (start == null) {
                            start = new Marker(MarkerType.START, lineNumber, noSecHub);
                        }
                    }
                }

                /*
                 * It only detects a pair, if there is a start and end This approach assumes,
                 * that the user sets start and end explicitly
                 */
                if (start != null && end != null) {
                    MarkerPair pair = new MarkerPair();
                    pair.setStart(start);
                    pair.setEnd(end);
                    markerPairs.add(pair);

                    start = null;
                    end = null;
                }
            }
        }

        return markerPairs;
    }
}
