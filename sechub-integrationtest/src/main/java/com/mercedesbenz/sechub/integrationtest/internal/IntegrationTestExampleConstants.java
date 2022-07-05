// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntegrationTestExampleConstants {

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

    public static TestDataFolderList TESTDATA_FOLDERS = new TestDataFolderList();

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

    public static class TestDataFolderList {

        private List<IntegrationTestExampleFolder> exampleContentFolders = new ArrayList<>();

        public List<IntegrationTestExampleFolder> getExampleContentFolders() {
            return Collections.unmodifiableList(exampleContentFolders);
        }

        private TestDataFolderList() {
            for (MockData mockData : MockData.values()) {
                if (mockData.isTargetUsedAsFolder()) {
                    String target = mockData.getTarget();
                    exampleContentFolders.add(new IntegrationTestExampleFolder(target, mockData.isTargetNeedingExistingData()));
                }
            }
        }
    }
}
