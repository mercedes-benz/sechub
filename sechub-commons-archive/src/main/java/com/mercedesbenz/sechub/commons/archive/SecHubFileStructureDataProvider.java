// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import java.util.Set;

public interface SecHubFileStructureDataProvider {

    boolean isRootFolderAccepted();

    Set<String> getUnmodifiableSetOfAcceptedReferenceNames();

    public static SecHubFileStructureDataProviderBuilder builder() {
        return new SecHubFileStructureDataProviderBuilder();
    }

}