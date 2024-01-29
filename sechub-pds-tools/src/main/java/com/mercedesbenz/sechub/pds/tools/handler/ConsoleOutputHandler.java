// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools.handler;

import com.mercedesbenz.sechub.commons.core.OutputHandler;

public class ConsoleOutputHandler implements OutputHandler {

    private ConsoleHandler consoleHandler;

    public ConsoleOutputHandler(ConsoleHandler consoleHandler) {
        this.consoleHandler = consoleHandler;
    }

    @Override
    public void warn(String message) {
        getConsoleHandler().output("WARN:" + message);
    }

    @Override
    public void info(String message) {
        getConsoleHandler().output(message);

    }

    @Override
    public void error(String message, Throwable t) {
        getConsoleHandler().error(message, t);
    }

    private ConsoleHandler getConsoleHandler() {
        return consoleHandler;
    }
}
