// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.test.TestFileReader;
import com.mercedesbenz.sechub.test.TestUtil;

class ProcessAdapterTest {

    @Test
    void process_adapter_enter_input_works_as_expected() throws Exception {

        /* prepare */
        Path tempFile = TestUtil.createTempFileInBuildFolder("process-adapter-test", "txt");
        ProcessBuilder pb = new ProcessBuilder("bash", "process-adapter-test.sh", tempFile.toString());
        pb.directory(new File("src/test/resources").toPath().toAbsolutePath().toFile());
        pb.redirectOutput(Redirect.INHERIT); // Attention: we do not redirect input stream here, otherwise it would not work
                                             // Every usage of "enterInput" must be done this way!
        pb.redirectError(Redirect.INHERIT);

        Process process = pb.start();

        /* execute */
        ProcessAdapter adapter = new ProcessAdapter(process);

        /* test */
        assertTrue(process.isAlive());
        adapter.enterInput("my-user input...via stdin".toCharArray());
        adapter.waitFor(2, TimeUnit.SECONDS);
        assertEquals(0, adapter.exitValue());

        // the bash script has written the input from stdin to a file, now we read and
        // check the content:
        String output = TestFileReader.loadTextFile(tempFile);
        assertEquals("user-input=my-user input...via stdin", output);

    }

}
