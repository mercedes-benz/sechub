// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

public class TestDataConstants {

    public static final String RESOURCE_PATH_ZIPFILE_ONLY_TEST1_TXT = "zipfile_contains_only_test1.txt.zip";

    public static final String RESOURCE_PATH_TARFILE_ONLY_TEST1_TXT = "tarfile_contains_only_test1.txt.tar";

    /**
     * Inside application-integrationtest.yml we have defined a file upload size of
     * <b>300 KiB</b>. This constants does represent it. If this changes in YAML it
     * must be synched back here!
     */
    public static final int CONFIGURED_INTEGRATION_TEST_MAX_FILE_UPLOAD_SIZE_IN_BYTES = 300 * 1024;

    /**
     * Inside application-integrationtest.yml we have defined a request size of
     * <b>320 KiB</b>. This constants does represent it. If this changes in YAML it
     * must be synched back here!
     */
    public static final int CONFIGURED_INTEGRATION_TEST_MAX_REQUEST_SIZE_IN_BYTES = 320 * 1024;

    /**
     * Inside application-integrationtest.yml we have defined a binary upload size
     * of <b>400 KiB</b>.. This constants does represent it. If this changes in YAML
     * it must be synched back here!
     */
    public static final int CONFIGURED_INTEGRATION_TEST_MAX_BINARIES_UPLOAD_IN_BYTES = 400 * 1024;
}
