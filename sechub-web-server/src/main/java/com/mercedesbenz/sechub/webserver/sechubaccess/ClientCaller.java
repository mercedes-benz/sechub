// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.sechubaccess;

import com.mercedesbenz.sechub.api.SecHubClient;

public interface ClientCaller<R> {
    public R callAndReturn(SecHubClient client) throws Exception;
}