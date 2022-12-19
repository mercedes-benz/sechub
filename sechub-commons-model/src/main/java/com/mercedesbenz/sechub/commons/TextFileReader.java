// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TextFileReader {

    public String loadTextFile(File file) throws IOException {
        return loadTextFile(file, "\n");
    }

    public String loadTextFile(File file, String lineBreak) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line = null;

            boolean firstEntry = true;
            while ((line = br.readLine()) != null) {
                if (!firstEntry) {
                    sb.append(lineBreak);
                }
                sb.append(line);
                firstEntry = false;// this prevents additional line break at end of file...
            }
            return sb.toString();
        }
    }
}
