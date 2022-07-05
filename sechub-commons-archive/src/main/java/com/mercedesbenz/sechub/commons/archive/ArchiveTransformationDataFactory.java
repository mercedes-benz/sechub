// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import static com.mercedesbenz.sechub.commons.archive.ArchiveConstants.*;

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

        String referenceName = findAcceptedReferenceNameFor(dataProvider, pathBelowDataFolder);
        if (referenceName != null) {
            String referencePathStartElement = referenceName + "/";

            if (pathBelowDataFolder.startsWith(referencePathStartElement)) {

                String wantedPath = pathBelowDataFolder.substring(referencePathStartElement.length());
                if (!includeExcludefilter.isFiltered(wantedPath, dataProvider)) {
                    result.setAccepted(true);
                    result.setWantedPath(wantedPath);
                }
            }
        }

        return result;
    }

    private String findAcceptedReferenceNameFor(SecHubFileStructureDataProvider dataProvider, String path) {
        for (String acceptedName : dataProvider.getUnmodifiableSetOfAcceptedReferenceNames()) {
            if (path.startsWith(acceptedName)) {
                return acceptedName;
            }
        }
        return null;
    }

}
