package com.mercedesbenz.sechub.commons.archive;

import static com.mercedesbenz.sechub.commons.archive.ArchiveConstants.*;

public class ArchiveTransformationDataFactory {

    private static final String EMPTY_SUBFOLDER_PATH = "";

    private ArchiveTransformationDataFactory() {
    }

    public static ArchiveTransformationData create(SecHubFileStructureConfiguration configuration, String path) {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration may not be null!");
        }
        if (path == null) {
            throw new IllegalArgumentException("Path may not be null!");
        }

        if (path.startsWith(DATA_SECTION_FOLDER)) {

            String subFolderPath = path.substring(DATA_SECTION_FOLDER.length());
            return createDataSectionTransformationData(configuration, subFolderPath);

        } else if (path.equals(DATA_SECTION_IDENTIFIER)) {
            /*
             * edge case - the __data__folder as standalone is also not inside root folder
             */
            return createDataSectionTransformationData(configuration, EMPTY_SUBFOLDER_PATH);

        } else {
            return createRootFolderTransformationData(configuration);
        }
    }

    private static ArchiveTransformationData createRootFolderTransformationData(SecHubFileStructureConfiguration configuration) {
        MutableArchiveTransformationData result = new MutableArchiveTransformationData();
        if (configuration.isRootFolderAccepted()) {
            result.setAccepted(true);
        }
        return result;
    }

    private static ArchiveTransformationData createDataSectionTransformationData(SecHubFileStructureConfiguration configuration,
            String subfolderBelowDataFolder) {
        MutableArchiveTransformationData result = new MutableArchiveTransformationData();

        String referenceName = findAcceptedReferenceNameFor(configuration, subfolderBelowDataFolder);
        if (referenceName != null) {
            String referencePathStartElement = referenceName + "/";

            if (subfolderBelowDataFolder.startsWith(referencePathStartElement)) {

                String wantedPath = subfolderBelowDataFolder.substring(referencePathStartElement.length());
                result.setAccepted(true);
                result.setWantedPath(wantedPath);
            }
        }
        return result;
    }

    private static String findAcceptedReferenceNameFor(SecHubFileStructureConfiguration configuration, String path) {
        for (String acceptedName : configuration.getAcceptedReferenceNames()) {
            if (path.startsWith(acceptedName)) {
                return acceptedName;
            }
        }
        return null;
    }

}
