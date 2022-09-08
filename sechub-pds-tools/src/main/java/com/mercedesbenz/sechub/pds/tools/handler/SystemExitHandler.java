// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools.handler;

public class SystemExitHandler implements ExitHandler {

    @Override
    public void exit(int exitCode) {
        System.exit(exitCode);
    }

}
