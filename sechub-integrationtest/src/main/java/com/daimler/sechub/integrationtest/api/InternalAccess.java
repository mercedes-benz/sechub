// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

public class InternalAccess {

    public static void forceDeleteOfProfileEvenDefaults(AsUser user, String profileId) {
        user.deleteProductExecutionProfile(profileId, false);
    }
}
