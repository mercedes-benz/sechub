// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.util;

import java.io.File;

import com.mercedesbenz.sechub.commons.TextFileReader;

public class DocGenTextFileReader {

    private TextFileReader reader = new TextFileReader();

    /**
     * Read text from file
     *
     * @param file file to read from
     * @return text, never <code>null</code>
     *
     * @throws IllegalStateException if file cannot be read.
     */
    public String readTextFromFile(File file) {
        try {
            return reader.readTextFromFile(file);
        } catch (Exception e) {
            throw new IllegalStateException("Doc generation / test case corrupt: Cannot read test file " + file.getAbsolutePath(), e);
        }
    }
}
