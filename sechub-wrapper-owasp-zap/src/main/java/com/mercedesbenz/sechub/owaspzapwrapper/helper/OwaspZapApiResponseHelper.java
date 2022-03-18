// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitRuntimeException;

public class OwaspZapApiResponseHelper {

    public String getIdOfApiRepsonse(ApiResponse response) {
        if (response instanceof ApiResponseElement) {
            return ((ApiResponseElement) response).getValue();
        } else {
            throw new MustExitRuntimeException("Parameter \"response\" is not an instance of ApiResponseElement.", MustExitCode.EXECUTION_FAILED);
        }
    }
}
