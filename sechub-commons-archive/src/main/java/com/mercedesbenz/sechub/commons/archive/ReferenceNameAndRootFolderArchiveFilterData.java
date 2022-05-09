package com.mercedesbenz.sechub.commons.archive;

import java.util.ArrayList;
import java.util.List;

public class ReferenceNameAndRootFolderArchiveFilterData {

    List<String> acceptedReferenceNames = new ArrayList<>();

    boolean rootFolderAccepted;

    public List<String> getAcceptedReferenceNames() {
        return acceptedReferenceNames;
    }

    public boolean isRootFolderAccepted() {
        return rootFolderAccepted;
    }

}
