// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

/**
 * Identifier for the product executors to use in integration test executor
 * configurations! This is only shallow copy of ProductIdentifier.java from
 * shared-kernel. But we have here no access to shared-kernel so a copy was
 * necessary. Remark: A tests exists which ensures that both classes are in
 * synch.
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

    PDS_IACSCAN,

    PDS_ANALYTICS,

    PDS_PREPARE,
    
    PDS_INFRALIGHT,

    /* Direct product executors */
    CHECKMARX,

    NETSPARKER,

    NESSUS,

    /* internal parts */
    SERECO,

    UNKNOWN

}
