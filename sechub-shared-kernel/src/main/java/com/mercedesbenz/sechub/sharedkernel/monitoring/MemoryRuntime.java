// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.monitoring;

/**
 * Memory runtime data
 *
 * @author Albert Tregnaghi
 *
 */
public class MemoryRuntime {

    public long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

}
