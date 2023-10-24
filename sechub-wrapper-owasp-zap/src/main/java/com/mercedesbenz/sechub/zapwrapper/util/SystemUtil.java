// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.util;

public class SystemUtil {

    /**
     * Use Thread.sleep(milliseconds) to wait for the specified amount of
     * milliseconds.
     *
     * @param milliseconds
     */
    public void waitForMilliseconds(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public long getCurrentTimeInMilliseconds() {
        return System.currentTimeMillis();
    }
}
