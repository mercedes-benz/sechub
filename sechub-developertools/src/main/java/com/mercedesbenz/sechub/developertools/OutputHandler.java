// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools;

public interface OutputHandler {

    public void output(String text);

    public default void error(String message) {
        error(message, null);
    }

    public void error(String message, Throwable t);
}
