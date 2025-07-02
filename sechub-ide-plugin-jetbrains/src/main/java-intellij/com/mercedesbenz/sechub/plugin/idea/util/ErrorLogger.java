// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.util;

import com.intellij.openapi.diagnostic.Logger;
import com.mercedesbenz.sechub.plugin.util.ErrorLog;

public class ErrorLogger implements ErrorLog {

    private static final Logger LOG = Logger.getInstance(ErrorLogger.class);
    private static final ErrorLogger INSTANCE = new ErrorLogger();

    public static ErrorLogger getInstance() {
        return INSTANCE;
    }

    public void error(String message, Throwable t) {
        LOG.error(message, t);
    }
}
