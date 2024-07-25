// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

import java.util.List;

public class DefaultProcessBuilderFactory implements ProcessBuilderFactory {

    public ProcessBuilder createForCommands(String... commands) {
        return new ProcessBuilder(commands);
    }

    @Override
    public ProcessBuilder createForCommandList(List<String> commands) {
        return new ProcessBuilder(commands);
    }
}
