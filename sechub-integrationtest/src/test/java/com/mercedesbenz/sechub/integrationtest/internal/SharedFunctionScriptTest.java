// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.integrationtest.api.TestOnlyForRegularExecution;

@TestOnlyForRegularExecution
class SharedFunctionScriptTest {

    private Process process;

    /**
     * We have a test script "test_shared-functions.sh" which can be started
     * manually and gives a fast feedback, if the shared functions (they are used
     * inside the test PDS launcher scripts) are working as expected. <br>
     * <br>
     * If this test fails, PDS integration tests will NOT work correctly and you
     * have to fix up the problems in the bash helper scripts before trying the
     * others ...
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    void test_shared_functions_script_can_be_executed_and_exits_with_0() throws IOException, InterruptedException {
        /* prepare */
        File folder = new File("./pds/product-scripts/");
        File script = new File(folder, "test_shared-functions.sh");

        ProcessBuilder processBuilder = new ProcessBuilder(script.getAbsolutePath());
        processBuilder.directory(folder);
        processBuilder.inheritIO();

        /* execute */
        process = processBuilder.start();

        /* test */
        boolean result = process.waitFor(3, TimeUnit.SECONDS);
        assertTrue(result, "Was not a regular exit of the application!");
        assertEquals(0, process.exitValue(), "Exit code is not 0. Means there were errors while testing the bash script functions!");
    }

}
