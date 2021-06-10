// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class TextFileReader {
    
    public String loadTextFile(File file) {
        return loadTextFile(file,"\n");
    }

    public String loadTextFile(File file, String lineBreak) {
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
        } catch (Exception e) {
            throw new IllegalStateException("Testcase corrupt: Cannot read test file " + file.getAbsolutePath(), e);
        }
    }
}
