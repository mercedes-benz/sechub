// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.util;

public class SecHubStorageUtil {

    /**
     * Creates a job storage path for given project id. Will return
     * "jobstorage/${projectId}.
     *
     * @param projectId project id to be used for calculation
     * @return storage path for project
     */
    public static String createStoragePathForProject(String projectId) {
        return "jobstorage/" + projectId;
    }

    public static String createAssetStoragePath() {
        return "assets";
    }
}
