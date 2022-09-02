// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.scan.auth;

import org.zaproxy.clientapi.core.ClientApiException;

public interface AuthScan {

    public void init() throws ClientApiException;

}
