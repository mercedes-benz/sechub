// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.core;

public interface StorageSetup {

    public static final String UNDEFINED = "undefined";

    /**
     * @return <code>true</code> when all necessary settings are available
     */
    public boolean isAvailable();

}
