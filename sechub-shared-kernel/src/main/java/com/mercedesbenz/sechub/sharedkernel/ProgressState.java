// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

public interface ProgressState {

    /**
     * @return <code>true</code> when progress has been suspended
     */
    public boolean isSuspended();

    /**
     * @return <code>true</code> when progress has been canceled
     */
    public boolean isCanceled();

}
