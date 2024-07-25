// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import static com.mercedesbenz.sechub.commons.archive.ArchiveConstants.*;

/**
 * A factory which creates archive transformation data.<br>
 * The transformation data contains information how to separate archive content
 * by data section (accepted, path transformation necessary, etc.)
 *
 * <h3>Explanation about data section format and impact on archives</h3> <br>
 * In a SecHub configuration we provide the possibility to use uploaded data by
 * multiple scan modules. <br>
 * <br>
 * For example: A user can upload sources as "source-ref1" in data section and
 * use it inside sourceScan and also in secretScan - means a SecHub user can do
 * multiple scans in one job without uploading data multiple times. It is also
 * possible to define multiple references inside the data section and so inside
 * the archive structure.<br>
 * <br>
 * For scan type "codeScan" it is possible to define sources directly without
 * using a reference name (but this is the legacy way). When no reference name
 * is defined, the archive contains those data directly inside the root
 * folder.<br>
 * <br>
 * To handle multiple references inside one archive file, we introduced the
 * artificial data root folder "__DATA__" which contains only sub directories
 * with the accepted reference names.
 *
 * Here an example:
 *
 * <pre>
 *
 * /legacy_way-I-am-inside-root.txt
 *
 * /__DATA__/source-ref1/
 *                      src/main/java
 *                            ExampleCode.java
 * /__DATA__/source-ref2/
 *                      src/main/go
 *                            example.go
 * </pre>
 *
 *
 * @author Albert Tregnaghi
 *
 */
public class ArchiveTransformationDataFactory {

    private static final String EMPTY_SUBFOLDER_PATH = "";
    SecHubFileStructureDataProviderIncludeExcludeFilter includeExcludefilter;

    public ArchiveTransformationDataFactory() {
        includeExcludefilter = new SecHubFileStructureDataProviderIncludeExcludeFilter();
    }

    public ArchiveTransformationData create(SecHubFileStructureDataProvider dataProvider, String path) {
        if (dataProvider == null) {
            throw new IllegalArgumentException("Configuration may not be null!");
        }
        if (path == null) {
            throw new IllegalArgumentException("Path may not be null!");
        }

        if (path.startsWith(DATA_SECTION_FOLDER)) {
            /* e.g "__DATA__/ref1/subfolder1/my-testdata.txt" */
            String targetPath = path.substring(DATA_SECTION_FOLDER.length());
            return createDataSectionTransformationData(dataProvider, targetPath);

        } else if (path.equals(DATA_SECTION_IDENTIFIER)) {
            /*
             * edge case - the __data__folder as standalone is also not inside root folder
             */
            return createDataSectionTransformationData(dataProvider, EMPTY_SUBFOLDER_PATH);

        } else {
            return createRootFolderTransformationData(dataProvider, path);
        }
    }

    private ArchiveTransformationData createRootFolderTransformationData(SecHubFileStructureDataProvider dataProvider, String path) {
        MutableArchiveTransformationData result = new MutableArchiveTransformationData();
        if (!includeExcludefilter.isFiltered(path, dataProvider)) {
            if (dataProvider.isRootFolderAccepted()) {
                result.setAccepted(true);
            }
        }
        return result;
    }

    private ArchiveTransformationData createDataSectionTransformationData(SecHubFileStructureDataProvider dataProvider, String pathBelowDataFolder) {
        MutableArchiveTransformationData result = new MutableArchiveTransformationData();

        /* @formatter:off
         *
         * For a better understanding here an example of the workflow. Let us assume that we have...
         *
         * - origin folder was "__DATA__/ref1/subfolder1/my-testdata.txt"
         * - so parameter pathBelowDataFolder is "ref1/subfolder1/my-testdata.txt"
         * - accepted reference names contains "ref1"
         * - my-testdata.txt is not filtered
         *
         * @formatter:on
         */
        String referencePathStartElement = findAcceptedReferencePathStartElement(dataProvider, pathBelowDataFolder);

        if (referencePathStartElement != null) {
            /* referencePathStartElement = "ref1/" */
            if (pathBelowDataFolder.startsWith(referencePathStartElement)) {

                String wantedPath = pathBelowDataFolder.substring(referencePathStartElement.length());
                /* wanted path = "subfolder1/my-testdata.txt" */
                if (!includeExcludefilter.isFiltered(wantedPath, dataProvider)) {
                    result.setAccepted(true);
                    result.setWantedPath(wantedPath);
                }
            }
        }

        return result;
    }

    private String findAcceptedReferencePathStartElement(SecHubFileStructureDataProvider dataProvider, String path) {
        for (String referenceName : dataProvider.getUnmodifiableSetOfAcceptedReferenceNames()) {
            String referencePathStartElement = referenceName + "/";

            if (path.startsWith(referencePathStartElement)) {
                return referencePathStartElement;
            }
        }
        return null;
    }

}
