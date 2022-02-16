// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

public interface Abandonable {

    /**
     * @return <code>true</code>when instance has been abandonded
     */
    public boolean isAbandoned();
}
