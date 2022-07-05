// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.mercedesbenz.sechub.test.TestConstants;

class AsciidocGeneratorManualTest {

    @Test
    @EnabledIfSystemProperty(named = TestConstants.MANUAL_TEST_BY_DEVELOPER, matches = "true", disabledReason = TestConstants.DESCRIPTION_DISABLED_BECAUSE_A_MANUAL_TEST_FOR_GENERATION)
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
        AsciidocGenerator.main(new String[] { targetFolder.getAbsolutePath() });
    }

}
