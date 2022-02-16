// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

/**
 * Those folders will be used inside tests and mockdata.json. They will be used
 * in conjunction with
 * {@link IntegrationTestExampleConstants#EXAMPLE_CONTENT_ROOT_PATH}
 *
 * @author Albert Tregnaghi
 *
 */
public enum IntegrationTestExampleFolders {

    FOLDER_CHECKSUMCHECK("../../../../../sechub-integrationtest/src/test/resources/checksum-testfiles", true),

    CHECKMARX_MOCKDATA_MULTIPLE("../sechub-doc/src/main/java"),

    CHECKMARX_MOCKDATA_EMPTY_10_MS("../sechub-integrationtest/src/main/java"),

    CHECKMARX_MOCKDATA_EMPTY_4000_MS("../sechub-test/src/main/java"),

    CHECKMARX_MOCKDATA_EMPTY_1000_MS("testProject1/src/java"),

    ;

    private String path;
    private boolean existingContent;

    private IntegrationTestExampleFolders(String path) {
        this(path, false);
    }

    private IntegrationTestExampleFolders(String path, boolean mustAlreadyExist) {
        this.path = path;
        this.existingContent = mustAlreadyExist;
    }

    public String getPath() {
        return path;
    }

    public boolean isExistingContent() {
        return existingContent;
    }
}
