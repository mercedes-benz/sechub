// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools.handler;

import com.beust.jcommander.internal.Console;

public interface ConsoleHandler extends Console {

    /**
     * Output given text to console
     *
     * @param text
     */
    public default void output(String text) {
        println(text);
    }

    @Override
    default void print(String msg) {

    }

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
