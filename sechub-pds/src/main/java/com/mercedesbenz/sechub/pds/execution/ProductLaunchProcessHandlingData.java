// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

public class ProductLaunchProcessHandlingData implements ProcessHandlingData {

    private int minutesToWaitBeforeProductTimeout;

    public ProductLaunchProcessHandlingData(int minutesToWaitBeforeProductTimeout) {
        this.minutesToWaitBeforeProductTimeout = minutesToWaitBeforeProductTimeout;
    }

    public int getMinutesToWaitBeforeProductTimeout() {
        return minutesToWaitBeforeProductTimeout;
    }
}
