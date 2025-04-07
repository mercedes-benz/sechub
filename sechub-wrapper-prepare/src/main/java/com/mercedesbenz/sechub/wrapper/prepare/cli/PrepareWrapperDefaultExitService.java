// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.cli;

import org.springframework.stereotype.Service;

@Service
public class PrepareWrapperDefaultExitService {

    public void exitWithCode(int exitCode) {
        System.exit(exitCode);
    }
}
