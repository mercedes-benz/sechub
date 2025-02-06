// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFileReader {

    private static final Logger LOG = LoggerFactory.getLogger(TestFileReader.class);

    public static String readTextFile(Path pathToFile) {
        return readTextFile(pathToFile.toFile());
    }

    public static String readTextFile(String pathToFile) {
        return readTextFile(new File(pathToFile));
    }

    public static String readTextFile(File file) {
        return readTextFile(file, "\n");
    }

    public static String readTextFile(File file, String lineBreak) {
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
