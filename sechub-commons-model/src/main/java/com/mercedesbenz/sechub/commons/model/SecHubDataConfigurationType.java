// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

public enum SecHubDataConfigurationType {

    /**
     * Data contains binaries
     */
    BINARY,

    /**
     * Data contains sources
     */
    SOURCE,

    /**
     * No data - this is a type which can be used when we have a scan where we do
     * not need data from configuration. E.g. when doing a simple web scan without
     * defining an OpenAPI file.
     */
    NONE,

    ;

}
