// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TextFileReader {

    /**
     * Load complete text file with default line break (new line)
     *
     * @param file resource to load
     * @return string, never {@link NullPointerException}
     * @throws IOException
     */
    public String loadTextFile(File file) throws IOException {
        return loadTextFile(file, "\n");
    }

    /**
     * Load complete text file
     *
     * @param file      resource to load
     * @param lineBreak the line break to use
     * @return
     * @throws IOException
     */
    public String loadTextFile(File file, String lineBreak) throws IOException {
        return loadTextFile(file, lineBreak, null);
    }

    /**
     * Loads text file - if max amount of lines is defined, only this amount of
     * lines will be read.
     *
     * @param file             resource to load
     * @param lineBreak        the line break to use
     * @param maxAmountOfLines maximum amount lines to read - minimum is 1. One line
     *                         will always be returned, even when the value is lower
     *                         than 1!
     * @return
     * @throws IOException
     */
    public String loadTextFile(File file, String lineBreak, Integer maxAmountOfLines) throws IOException {
        StringBuilder sb = new StringBuilder();

        int linesRead = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
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
