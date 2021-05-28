// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.core.util;

public class SecHubStorageUtil {

    public static String createStoragePath(String projectId) {
        return "jobstorage/"+projectId;
    }
}
