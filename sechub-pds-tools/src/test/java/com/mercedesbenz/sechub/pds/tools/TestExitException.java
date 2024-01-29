// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools;

/**
 * Special test exception to simulate exit codes and to stop/ break further
 * processing.
 *
 * @author Albert Tregnaghi
 *
 */
class TestExitException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private int exitCode;

    public TestExitException(int exitCode) {
        super("ExitCode:" + exitCode);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}