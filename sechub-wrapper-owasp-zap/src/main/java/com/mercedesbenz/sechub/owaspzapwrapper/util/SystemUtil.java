// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.util;

public class SystemUtil {

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
