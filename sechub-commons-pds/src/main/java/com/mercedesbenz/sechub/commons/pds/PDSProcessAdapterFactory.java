// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

import java.io.IOException;

public class PDSProcessAdapterFactory {

    public ProcessAdapter startProcess(ProcessBuilder builder) throws IOException {
        Process process = builder.start();
        return new ProcessAdapter(process);
    }

}
