// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.docgen.AsciidocGenerator.AsciidocGeneratorExecutor;
import com.mercedesbenz.sechub.test.ManualTest;

class AsciidocGeneratorManualTest implements ManualTest {

    @Test
    void manualTestByDeveloper() throws Exception {

        /* check preconditions */
        File documentsFolder = new File("src/docs/asciidoc/documents/");
        if (!documentsFolder.exists()) {
            throw new IllegalStateException("Folder:" + documentsFolder.getAbsolutePath() + " does not exist!");
        }
        /* prepare */
        File targetFolder = new File(documentsFolder, "gen");
        System.setProperty("com.mercedesbenz.sechub.docgen.debug", "true");

        /* execute */
        AsciidocGenerator asciidocGenerator = new AsciidocGenerator();
        asciidocGenerator.generate(targetFolder.getAbsolutePath(), new AsciidocGeneratorExecutor(true)); // here we use an executor which only counts failures.
                                                                                                         // Will execute all sub generators and will fail only
                                                                                                         // at the end.
    }

}
