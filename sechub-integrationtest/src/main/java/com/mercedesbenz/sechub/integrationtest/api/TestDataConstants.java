// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

public class TestDataConstants {
    public static final String RESOURCE_PATH_ZIPFILE_ONLY_TEST1_TXT = "zipfile_contains_only_test1.txt.zip";

    /**
     * Inside application-integrationtest.yml we have defined a general upload size
     * of <b>300 Kilobyte</b>. This constants does represent it. If this changes in
     * yaml it must be synched back here!
     */
    public static final int CONFIGURED_INTEGRATION_TEST_MAX_GENERAL_UPLOAD_IN_BYTES = 300 * 1024;

    /**
     * Inside application-integrationtest.yml we have defined a binary upload size
     * of <b>400 Kilobyte</b>.. This constants does represent it. If this changes in
     * yaml it must be synched back here!
     */
    public static final int CONFIGURED_INTEGRATION_TEST_MAX_BINARIES_UPLOAD_IN_BYTES = 400 * 1024;
}
