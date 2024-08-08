// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSSFileToFragementMerger {

    private static final Logger LOG = LoggerFactory.getLogger(CSSFileToFragementMerger.class);

    private static String CSS_FRAGMENT_START = "<style type=\"text/css\" th:fragment=\"styles\">";

    private static final TestFileWriter writer = new TestFileWriter();

    public void merge(File cssFile, File fragmentsFile) throws IOException {
        String cssContent = TestFileReader.readTextFromFile(cssFile);
        String fragmentsContent = TestFileReader.readTextFromFile(fragmentsFile);

        int fragmentIndex = fragmentsContent.indexOf(CSS_FRAGMENT_START);

        if (fragmentIndex == -1) {
            throw new IllegalStateException("Fragment start not found!");
        }
        fragmentIndex = fragmentIndex + CSS_FRAGMENT_START.length();
        int closeStyleIndex = fragmentsContent.indexOf("</style", fragmentIndex);
        if (closeStyleIndex == -1) {
            throw new IllegalStateException("Fragment end not found!");
        }

        String newFragmentContent = fragmentsContent.substring(0, fragmentIndex) + "\n" + cssContent + "\n" + fragmentsContent.substring(closeStyleIndex);
        writer.writeTextToFile(fragmentsFile, newFragmentContent, true);
        LOG.info("Updated fragment file: {}\nfrom with CSS content of: {}", fragmentsFile, cssFile);

    }

}
