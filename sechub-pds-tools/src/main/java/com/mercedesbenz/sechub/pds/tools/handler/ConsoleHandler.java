// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools.handler;

public interface ConsoleHandler {

    /**
     * Output given text to console
     *
     * @param text
     */
    public void output(String text);

    /**
     * Show error in console
     *
     * @param message
     */
    public default void error(String message) {
        error(message, null);
    }

    /**
     * Show error with given throwable in console
     *
     * @param message
     * @param t       can be <code>null</code>
     */
    public void error(String message, Throwable t);
}
