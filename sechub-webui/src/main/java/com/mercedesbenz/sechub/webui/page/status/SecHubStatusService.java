// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page.status;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.api.SecHubStatus;
import com.mercedesbenz.sechub.webui.sechubaccess.SecHubAccessService;

@Service
class SecHubStatusService {

    private final SecHubAccessService accessService;

    SecHubStatusService(SecHubAccessService accessService) {
        this.accessService = accessService;
    }

    SecHubStatus getSecHubStatus() {
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
