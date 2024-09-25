// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to read text from files. Will always use UTF-8.
 *
 * @author Albert Tregnaghi
 *
 */
public class TextFileReader {

    private static final String DEFAULT_LINE_BREAK = "\n";
    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    private static final Logger LOG = LoggerFactory.getLogger(TextFileReader.class);

    /**
     * Read complete text from file with default line break
     *
     * @param file resource to load
     * @return string, never <code>null</code>
     * @throws IOException when IO problems occur
     */
    public String readTextFromFile(File file) throws IOException {
        return readTextFromFile(file, DEFAULT_LINE_BREAK);
    }

    /**
     * Read complete text from file
     *
     * @param file      resource to load
     * @param lineBreak the line break to use
     * @return string, never <code>null</code>
     * @throws IOException when IO problems occur
     */
    public String readTextFromFile(File file, String lineBreak) throws IOException {
        return readTextFromFile(file, lineBreak, null);
    }

    /**
     * Read text from file - if max amount of lines is defined, only this amount of
     * lines will be read.
     *
     * @param file             resource to load
     * @param lineBreak        the line break to use
     * @param maxAmountOfLines maximum amount lines to read - minimum is 1. One line
     *                         will always be returned, even when the value is lower
     *                         than 1!
     * @return string, never <code>null</code>
     * @throws IOException when IO problems occur
     */
    public String readTextFromFile(File file, String lineBreak, Integer maxAmountOfLines) throws IOException {
        LOG.trace("Read text from file: {}", file);

        StringBuilder sb = new StringBuilder();

        int linesRead = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), CHARSET_UTF8))) {
            String line = null;

            while ((line = br.readLine()) != null) {
                linesRead++;

                if (linesRead > 1) { // this prevents additional line break at end of file...
                    sb.append(lineBreak);
                }
                sb.append(line);

                if (maxAmountOfLines != null) {
                    if (linesRead >= maxAmountOfLines) {
                        return sb.toString();
                    }
                }
            }
            return sb.toString();
        }
    }
}
