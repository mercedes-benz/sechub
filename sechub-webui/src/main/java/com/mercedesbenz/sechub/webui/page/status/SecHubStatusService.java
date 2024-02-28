// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.api.SecHubStatus;
import com.mercedesbenz.sechub.webui.sechubaccess.SecHubAccessService;

@Service
public class SecHubStatusService {

    @Autowired
    SecHubAccessService accessService;

    public SecHubStatus getSecHubStatus() {
        /* @formatter:off */

        return accessService.createExecutorForResult(SecHubStatus.class).
                whenDoing("fetching SecHub status").
                callAndReturn(client->{
                      client.triggerRefreshOfSecHubSchedulerStatus();
                      return client.fetchSecHubStatus();
                }).
                onErrorReturnAlways(null).
                execute();
        /* @formatter:on */
    }

}
