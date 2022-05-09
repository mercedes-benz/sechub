package com.mercedesbenz.sechub.commons.archive;

import static com.mercedesbenz.sechub.commons.archive.ArchiveConstants.*;

public class ReferenceNameAndRootFolderArchivePathInspector implements ArchivePathInspector {

    private ReferenceNameAndRootFolderArchiveFilterData data;

    public ReferenceNameAndRootFolderArchivePathInspector(ReferenceNameAndRootFolderArchiveFilterData data) {
        this.data = data;
    }

    @Override
    public ArchivePathInspectionResult inspect(String path) {
        ArchivePathInspectionResult result = new ArchivePathInspectionResult();
        if (path.startsWith(DATA_SECTION_PREFIX)) {
            String subName = path.substring(DATA_SECTION_PREFIX.length());
            for (String acceptedName : data.acceptedReferenceNames) {
                if (subName.startsWith(acceptedName)) {
                    String filteredName = subName.substring(acceptedName.length());
                    result.accepted = true;
                    result.wantedPath = filteredName;
                    return result;
                }
            }
        } else {
            if (data.rootFolderAccepted) {
                result.accepted = true;
                result.wantedPath = path;
            }
        }
        return result;
    }

}
