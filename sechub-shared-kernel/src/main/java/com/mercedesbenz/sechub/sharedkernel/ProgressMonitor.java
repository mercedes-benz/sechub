// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

public interface ProgressMonitor {

    /**
     * @return <code>true</code>when progress has been canceled
     */
    public boolean isCanceled();

    public default String getId() {
        return "" + hashCode();
    }

}
