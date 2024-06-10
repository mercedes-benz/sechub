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

    /**
     * Sends given characters as user input to process. Attention: You are NOT
     * allowed to redirect the input stream of the process builder when you use this
     * method - Otherwise it will not work!
     *
     * <h3>Correct example</h3>
     *
     * <pre>
     * ProcessBuilder pb = new ProcessBuilder("bash", "script-with-userinput.sh");
     * pb.redirectOutput(Redirect.INHERIT);
     * pb.redirectError(Redirect.INHERIT);
     * </pre>
     *
     * <h3>Wrong examples</h3> Example W1
     *
     * <pre>
     * ProcessBuilder pb = new ProcessBuilder("bash", "script-with-userinput.sh");
     * pb.inheritIO(); // does also redirect input -> will not work...
     * </pre>
     *
     * Example W2
     *
     * <pre>
     * ProcessBuilder pb = new ProcessBuilder("bash", "script-with-userinput.sh");
     * pb.redirectOutput(Redirect.INHERIT);
     * pb.redirectError(Redirect.INHERIT);
     * pb.redirectInput(Redirect.INHERIT); // redirect input -> will not work...
     * </pre>
     *
     *
     *
     */
    public void enterInput(char[] unsealedPassword) throws IOException {
        if (process == null) {
            return;
        }
        /*
         * Don't be confused: in javadoc of this method we forbid the redirect of INPUT
         * stream when using enterInput and here we use the output stream of the
         * process... It is correct, because the output stream is connected to the input
         * stream of the process - see see javadoc of getOutputStream() for more
         * details.
         */
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
            bw.write(unsealedPassword);
            bw.flush();
        }

    }

}