// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import java.io.IOException;

import org.springframework.stereotype.Component;

@Component
public class PDSProcessAdapterFactory {

    public ProcessAdapter startProcess(ProcessBuilder builder) throws IOException {
        Process process = builder.start();
        return new ProcessAdapter(process);
    }

}
