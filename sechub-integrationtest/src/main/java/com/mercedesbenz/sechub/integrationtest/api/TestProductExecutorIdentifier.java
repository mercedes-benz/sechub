// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

/**
 * Identifier for the product executors to use in integration test executor
 * configurations!
 *
 * @author Albert Tregnaghi
 *
 */
public enum TestProductExecutorIdentifier {

    /* PDS product executors */
    PDS_CODESCAN,

    PDS_WEBSCAN,

    PDS_INFRASCAN,

    PDS_LICENSESCAN,

    PDS_SECRETSCAN,

    PDS_ANALYTICS,

    PDS_PREPARE,

    /* Direct product executors */
    CHECKMARX,

    NETSPARKER,

    NESSUS,

}
