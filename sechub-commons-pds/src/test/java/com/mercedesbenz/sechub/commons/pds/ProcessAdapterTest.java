// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.test.TestUtil;

class ProcessAdapterTest {

    @Test
    void process_adapter_enter_input_works_as_expected() throws Exception {
        /* prepare */
        Path tempFile = TestUtil.createTempFileInBuildFolder("process-adapter-test", "txt");
        ProcessBuilder pb = new ProcessBuilder("bash", "process-adapter-test.sh", tempFile.toString());
        pb.directory(new File("src/test/resources").toPath().toAbsolutePath().toFile());
        pb.redirectOutput(Redirect.INHERIT);
        pb.redirectError(Redirect.INHERIT);

        Process process = pb.start();

        /* execute */
        ProcessAdapter adapter = new ProcessAdapter(process);

        /* test */
        assertTrue(process.isAlive());
        adapter.enterInput("my-user input...".toCharArray());
        adapter.waitFor(2, TimeUnit.SECONDS);
        assertEquals(0, adapter.exitValue());

    }

}
