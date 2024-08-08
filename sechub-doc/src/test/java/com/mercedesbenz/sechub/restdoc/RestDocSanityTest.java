// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.docgen.util.DocGenTextFileReader;

class RestDocSanityTest {

    @Test
    void check_restdoc_tests_are_not_using_any_imports_of_com_epages() {
        /* prepare */
        File file = new File("./src/test/java/com/mercedesbenz/sechub/restdoc");
        assertTrue(file.exists());
        assertTrue(file.isDirectory());

        DocGenTextFileReader reader = new DocGenTextFileReader();
        StringBuilder sb = new StringBuilder();
        int count = 0;

        /* execute / inspect */
        for (File sourceFile : file.listFiles((dirFile, name) -> name.endsWith("RestDocTest.java"))) {
            count++;
            // System.out.println("sourcefile:"+sourceFile);
            String sourceCode = reader.readTextFromFile(sourceFile);
            if (sourceCode.indexOf("import static com.epages") != -1) {
                sb.append("- ").append(sourceFile.getName()).append(" does use static import of com.epages. Must be changed.\n");
            } else if (sourceCode.indexOf("import com.epages") != -1) {
                sb.append("- ").append(sourceFile.getName()).append(" does use an import of com.epages. Must be changed.\n");
            }
        }

        /* test */
        if (sb.length() > 0) {
            String problems = sb.toString();
            int problemCount = problems.split("\n").length;
            problems = "For " + count + " RestDocTest files " + problemCount + " have problems:\n" + problems;
            System.out.println(problems);
            fail(problems);
        }

    }

}
