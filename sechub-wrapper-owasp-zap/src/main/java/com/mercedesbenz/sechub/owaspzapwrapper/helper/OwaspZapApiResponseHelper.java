// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;

public class OwaspZapApiResponseHelper {

    public static String getIdOfApiRepsonse(ApiResponse response) {
        return ((ApiResponseElement) response).getValue();
    }
}
