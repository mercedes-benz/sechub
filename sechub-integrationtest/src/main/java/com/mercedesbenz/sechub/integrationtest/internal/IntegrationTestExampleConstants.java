// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntegrationTestExampleConstants {

    /**
     * This is just a information for developers to find the PDS configuration file
     * easier
     */
    public static final String PDS_INTEGRATIONTEST_CONFIG_FILEPATH = "sechub-integrationtest/src/main/resources/pds-config-integrationtest.json";

    public static final String EXAMPLE_CONTENT_ROOT_PATH = "sechub-integrationtest/build/sechub/example/content/";

    /**
     * The default infrascan white list URI. The usage can be found
     * "/sechub-integrationtest/src/test/resources/sechub-integrationtest-client-infrascan.json"
     */
    public static final String INFRASCAN_DEFAULT_WHITELEIST_ENTRY = "https://fscan.intranet.example.org";

    /**
     * Zip file structure
     *
     * <pre>
     *   /data.txt
     * </pre>
     *
     * Content of the file "data.txt":
     *
     * <pre>
     * CRITICAL:i am a critical error
     * MEDIUM:i am a medium error
     * LOW:i am just a low error
     * INFO:i am just an information
     * </pre>
     */
    public static final String PATH_TO_ZIPFILE_WITH_PDS_CODESCAN_CRITICAL_FINDINGS = "pds/codescan/upload/zipfile_contains_inttest_codescan_with_critical.zip";

    /**
     * Zip file structure
     *
     * <pre>
     *   /data.txt
     * </pre>
     *
     * Content of the file "data.txt":
     *
     * <pre>
     * LOW:i am just a low error
     * INFO:i am just an information
     * </pre>
     */
    public static final String PATH_TO_ZIPFILE_WITH_PDS_CODESCAN_LOW_FINDINGS = "pds/codescan/upload/zipfile_contains_inttest_codescan_with_low.zip";

    /**
     * Zip file structure is exactly like
     * {@link #PATH_TO_ZIPFILE_WITH_PDS_CODESCAN_LOW_FINDINGS} , but the file name
     * has some umlauts.
     *
     * <pre>
     *   /data-äüÖ.txt
     * </pre>
     *
     * Content of the file "data-äüÖ.txt":
     *
     * <pre>
     * LOW:i am just a low error
     * INFO:i am just an information
     * </pre>
     */
    public static final String PATH_TO_ZIPFILE_WITH_PDS_CODESCAN_LOW_FINDINGS_BUT_FILENAME_WITH_UMLAUTS = "pds/codescan/upload/zipfile_contains_inttest_codescan_with_low_but_filename_with_umlauts.zip";

    /**
     * /** Sha256 checksum for
     * {@value #PATH_TO_ZIPFILE_WITH_PDS_CODESCAN_LOW_FINDINGS}/data.txt
     */
    public static final String SHA256SUM_FOR_DATA_TXT_FILE_IN_ZIPFILE_WITH_PDS_CODESCAN_LOW_FINDINGS = "7d09bec9c44ba241e4dc948706727456fbca2fce9a6b024371f63307ae017372";

    /**
     * Tar file structure
     *
     * <pre>
     *   /__data__
     *      /files-a
     *         file-a-1.txt (low finding)
     *         file-a-2.txt (low finding)
     *         subfolder-1/
     *             file-a-3.txt ((medium finding)
     *      /files-b
     *         file-b-1.txt (low finding)
     *         file-b-2.txt (low finding)
     *         subfolder-2/
     *             file-b-3.txt (critical finding)
     * </pre>
     */
    public static final String PATH_TO_TARFILE_WITH_DIFFERENT_DATA_SECTIONS = "pds/codescan/upload/tarfile_contains_different_finding_files_in_different_data_sections.tar";

    /**
     * Tar file structure
     *
     * <pre>
     *   /__data__
     *      /files-a
     *         file-a-1.txt (low finding)
     *         file-a-2.txt (low finding)
     *         subfolder-1/
     *             file-a-3.txt ((medium finding)
     *      /files-b/
     *        /included-folder
     *          file-b-1.txt (low finding)
     *          file-b-2.txt (low finding)
     *          excluded-1.txt (low finding)
     *          subfolder-2/
     *             file-b-3.txt (critical finding)
     *          excluded-folder/
     *             file-b-4.txt (critical finding)
     *        /not-included-subfolder-3
     *          file-b-5.txt (low finding)
     * </pre>
     *
     * This file will be used with file filtering having
     *
     * <pre>
     *
     * includes: {@value IntegrationTestDefaultExecutorConfigurations#INCLUDES_1} and
     * excludes: {@value IntegrationTestDefaultExecutorConfigurations#EXCLUDES_1}
     *
     * by using {@link IntegrationTestDefaultProfiles#PROFILE_10_PDS_CODESCAN_INCLUDES_EXCLUDES profile 10}.
     * </pre>
     */
    public static final String PATH_TO_TARFILE_WITH_DATA_SECTION_FOR_INCLUDE_EXCLUDES = "pds/codescan/upload/tarfile_with_data_being_included_and_excluded.tar";

    /**
     * Tar file structure (without __data__ section, everything is inside root
     * folder!)
     *
     * <pre>
     * /
     *      /files-a
     *         file-a-1.txt (low finding)
     *         file-a-2.txt (low finding)
     *         subfolder-1/
     *             file-a-3.txt ((medium finding)
     *      /files-b/
     *        /included-folder
     *          file-b-1.txt (low finding)
     *          file-b-2.txt (low finding)
     *          excluded-1.txt (low finding)
     *          subfolder-2/
     *             file-b-3.txt (critical finding)
     *          excluded-folder/
     *             file-b-4.txt (critical finding)
     *        /not-included-subfolder-3
     *          file-b-5.txt (low finding)
     * </pre>
     *
     * This file will be used with file filtering having
     *
     * <pre>
     *
     * includes: {@value IntegrationTestDefaultExecutorConfigurations#INCLUDES_1} and
     * excludes: {@value IntegrationTestDefaultExecutorConfigurations#EXCLUDES_1}
     *
     * by using {@link IntegrationTestDefaultProfiles#PROFILE_10_PDS_CODESCAN_INCLUDES_EXCLUDES profile 10}.
     * </pre>
     */
    public static final String PATH_TO_TARFILE_WITH_DATA_SECTION_FOR_INCLUDE_EXCLUDES__BINARIES_ARCHIVE_ROOT_USED = "pds/codescan/upload/tarfile_with_data_being_included_and_excluded_binaries_archive_root.tar";

    /**
     * Zip file structure
     *
     * <pre>
     *   /__data__
     *      /files-a
     *         file-a-1.txt (low finding)
     *         file-a-2.txt (low finding)
     *         subfolder-1/
     *             file-a-3.txt ((medium finding)
     *      /files-b/
     *        /included-folder
     *          file-b-1.txt (low finding)
     *          file-b-2.txt (low finding)
     *          excluded-1.txt (low finding)
     *          subfolder-2/
     *             file-b-3.txt (critical finding)
     *          excluded-folder/
     *             file-b-4.txt (critical finding)
     *        /not-included-subfolder-3
     *          file-b-5.txt (low finding)
     * </pre>
     *
     * This file will be used with file filtering having
     *
     * <pre>
     *
     * includes: {@value IntegrationTestDefaultExecutorConfigurations#INCLUDES_1} and
     * excludes: {@value IntegrationTestDefaultExecutorConfigurations#EXCLUDES_1}
     *
     * by using {@link IntegrationTestDefaultProfiles#PROFILE_10_PDS_CODESCAN_INCLUDES_EXCLUDES profile 10}.
     * </pre>
     */
    public static final String PATH_TO_ZIPFILE_WITH_DATA_SECTION_FOR_INCLUDE_EXCLUDES = "pds/codescan/upload/zipfile_with_data_being_included_and_excluded.zip";

    /**
     * Zip file structure
     *
     * <pre>
     *   /__data__
     *      /medium-id
     *         data.txt <<1>>
     *      /low-id
     *         data.txt <<2>>
     *      /criticial-id
     *         data.txt <<3>>
     * </pre>
     *
     * Content:
     *
     * <pre>
     * <<1>>
     * MEDIUM:i am a medium error
     * LOW:i am just a low error
     * INFO:i am just an information
     * </pre>
     *
     * <pre>
     * <<2>>
     * LOW:i am just a low error
     * INFO:i am just an information
     * </pre>
     *
     * <pre>
     * <<3>>
     * CRITICAL:i am a critical error
     * MEDIUM:i am a medium error
     * LOW:i am just a low error
     * INFO:i am just an information
     * </pre>
     */
    public static final String PATH_TO_ZIPFILE_WITH_DIFFERENT_DATA_SECTIONS = "pds/codescan/upload/zipfile_contains_inttest_codescan_with_different_data_sections.zip";

    public static final MockDataIdentifierExampleContentFolderProvider MOCKDATA_EXAMPLE_CONTENT_PROVIDER = new MockDataIdentifierExampleContentFolderProvider();

    public static class IntegrationTestExampleFolder {
        private boolean isExistingContent;
        private String path;

        public IntegrationTestExampleFolder(String path, boolean isExistingContent) {
            this.path = path;
            this.isExistingContent = isExistingContent;
        }

        public String getPath() {
            return path;
        }

        public boolean isExistingContent() {
            return isExistingContent;
        }

        @Override
        public String toString() {
            return "IntegrationTestExampleFolder [isExistingContent=" + isExistingContent + ", " + (path != null ? "path=" + path : "") + "]";
        }

    }

    public static final String MAPPING_1_PATTERN_ANY_PROJECT1 = ".*project1*";
    public static final String MAPPING_1_REPLACEMENT_FOR_PROJECT1 = "replacement-project1";
    public static final String MAPPING_1_COMMENT = "test mapping for project names, handles project1";

    /**
     * This mapping will be automatically created and is available inside tests. It
     * uses {@link #MAPPING_ID_1_REPLACE_ANY_PROJECT1}
     * ({@value #MAPPING_ID_1_REPLACE_ANY_PROJECT1} ) as pattern and
     * {@link #MAPPING_1_REPLACEMENT_FOR_PROJECT1}
     * ({@value #MAPPING_1_REPLACEMENT_FOR_PROJECT1}) for replacement
     */
    public static final String MAPPING_ID_1_REPLACE_ANY_PROJECT1 = "test.mapping1.replace.project1";

    /**
     * Just the resulting PDS environment name for mapping
     * {@link #MAPPING_ID_1_REPLACE_ANY_PROJECT1}
     */
    public static final String PDS_ENV_NAME_MAPPING_ID_1_REPLACE_ANY_PROJECT1 = "TEST_MAPPING1_REPLACE_PROJECT1";

    /**
     * This mapping does just not exists on SecHub side. But the id can be
     * referenced - e.g. inside a PDS SecHub executor configuration to enable
     * testing if an empty mapping is injected as fallback on PDS side.
     */
    public static final String MAPPING_ID_2_NOT_EXISTING_IN_SECHUB = "test.mapping2.not.existing.in.sechub";

    /**
     * Just the resulting PDS environment name for mapping
     * {@link #MAPPING_ID_2_NOT_EXISTING_IN_SECHUB}
     */
    public static final String PDS_ENV_NAME_MAPPING_ID_2_NOT_EXISTING_IN_SECHUB = "TEST_MAPPING2_NOT_EXISTING_IN_SECHUB";

    public static class MockDataIdentifierExampleContentFolderProvider {

        private List<IntegrationTestExampleFolder> exampleContentFolders = new ArrayList<>();

        public List<IntegrationTestExampleFolder> getExampleContentFolders() {
            return Collections.unmodifiableList(exampleContentFolders);
        }

        private MockDataIdentifierExampleContentFolderProvider() {
            for (MockData mockData : MockData.values()) {
                if (mockData.isMockDataIdentifierUsedAsFolder()) {
                    String mockDataIdentifierAsFolder = mockData.getMockDataIdentifier();
                    exampleContentFolders.add(new IntegrationTestExampleFolder(mockDataIdentifierAsFolder, mockData.isNeedsExistingFolder()));
                }
            }
        }
    }
}
