// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.util;

public class SecHubStorageUtil {

    public static String createStoragePath(String projectId) {
        return "jobstorage/" + projectId;
    }
    
    public static String createAssetStoragePath() {
        return "assets";
    }
}
