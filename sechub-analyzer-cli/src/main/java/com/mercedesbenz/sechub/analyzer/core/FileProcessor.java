// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.analyzer.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.mercedesbenz.sechub.analyzer.model.Marker;
import com.mercedesbenz.sechub.analyzer.model.MarkerPair;
import com.mercedesbenz.sechub.analyzer.model.MarkerType;

/**
 * Searches through a file looking for SecHub markers
 */
public class FileProcessor {

    private static final String NOSECHUB = "NOSECHUB";
    private static final String NOSECHUB_END = "END-NOSECHUB";

    private static CommentChecker commentChecker = CommentChecker.buildFrom(NOSECHUB, NOSECHUB_END);

    FileProcessor() {
    }

    /**
     * Search through a given file for SecHub markers
     *
     * Markers: - Start: NOSECHUB - END: END-NOSECHUB
     *
     * @param file
     * @return List<MarerPair> a list of marker pairs
     * @throws FileNotFoundException
     * @throws IOException
     */
    public List<MarkerPair> processFile(File file) throws FileNotFoundException, IOException {
        List<MarkerPair> markerPairs = new LinkedList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line = null;
            int lineNumber = 0;

            Marker start = null;
            Marker end = null;

            /* iterate through every line in the file */
            while ((line = bufferedReader.readLine()) != null) {
                lineNumber++;

                /* search for the NOSECHUB marker */
                int noSecHubIndex = line.indexOf(NOSECHUB);
                if (noSecHubIndex == -1) {
                    continue;
                }

                /* check if the line is a comment */
                boolean isComment = commentChecker.isCommentInLine(line);
                if (!isComment) {
                    continue;
                }

                /* search for NOSECHB_END marker */
                int endNoSecHubIndex = line.indexOf(NOSECHUB_END);

                if (endNoSecHubIndex > -1) {
                    end = new Marker(MarkerType.END, lineNumber, endNoSecHubIndex);
                } else {
                    /* only set a new start marker if no previous start was found */
                    if (start == null) {
                        start = new Marker(MarkerType.START, lineNumber, noSecHubIndex);
                    }
                }
                /*
                 * It only detects a pair, if there is a start and end. This approach assumes,
                 * that the user marks the start and end explicitly in the code.
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
