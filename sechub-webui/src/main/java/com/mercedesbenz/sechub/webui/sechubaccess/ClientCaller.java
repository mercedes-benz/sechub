package com.mercedesbenz.sechub.webui.sechubaccess;

import com.mercedesbenz.sechub.api.SecHubClient;

public interface ClientCaller<R> {
    public R callAndReturn(SecHubClient client) throws Exception;
}