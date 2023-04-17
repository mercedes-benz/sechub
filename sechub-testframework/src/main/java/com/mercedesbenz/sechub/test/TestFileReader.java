// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFileReader {

    private static final Logger LOG = LoggerFactory.getLogger(TestFileReader.class);

    public static String loadTextFile(Path pathToFile) {
        return loadTextFile(pathToFile.toFile());
    }

    public static String loadTextFile(String pathToFile) {
        return loadTextFile(new File(pathToFile));
    }

    public static String loadTextFile(File file) {
        return loadTextFile(file, "\n");
    }

    public static String loadTextFile(File file, String lineBreak) {
        if (TestUtil.isTraceEnabled()) {
            LOG.info("Loading file:{}", file);
        }
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
