// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core;

/**
 * Sometimes it is necessary to switch between different output locations. <br>
 * <br>
 * An example:<br>
 * A library is used from a CLI tool and also from a server application. The CLI
 * wants to send the output directly to command line (without using a logging
 * framework) but the server application wants to use slf4j logger. The library
 * code does use the output handler to separate the logic to the caller side.
 *
 * @author Albert Tregnaghi
 *
 */
public interface OutputHandler {

    public void info(String message);

    public void warn(String message);

    public default void error(String message) {
        error(message, null);
    }

    public void error(String message, Throwable t);

}
