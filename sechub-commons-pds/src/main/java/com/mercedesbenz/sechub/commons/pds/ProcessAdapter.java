// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * An adapter for processes. Via this class we can mock processes in tests very
 * easy with Mockito.
 */
public class ProcessAdapter {

    private Process process;

    public ProcessAdapter(Process process) {
        this.process = process;
    }

    public boolean waitFor(long timeOut, TimeUnit timeUnit) throws InterruptedException {
        if (process == null) {
            return false;
        }
        return process.waitFor(timeOut, timeUnit);
    }

    public boolean isAlive() {
        if (process == null) {
            return false;
        }
        return process.isAlive();
    }

    public void destroyForcibly() {
        if (process == null) {
            return;
        }
        process.destroyForcibly();
    }

    public int exitValue() {
        if (process == null) {
            return -1;
        }
        return process.exitValue();
    }

    /*
     * Sends given characters as user input to process.
     */
    public void enterInput(char[] unsealedPassword) throws IOException {
        if (process == null) {
            return;
        }
        // we must use the output stream from the child process - see javadoc of
        // getOutputStream()
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
            bw.write(unsealedPassword);
            bw.flush();
        }

    }

}