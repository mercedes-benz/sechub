// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.SecHubClientException;
import com.mercedesbenz.sechub.api.SecHubStatus;
import com.mercedesbenz.sechub.webui.configuration.SecHubAccessService;

@Service
public class StatusService {
    @Autowired
    SecHubAccessService apiAccessService;

    public SecHubStatus getSecHubServerStatusInformation() {
        SecHubClient client = apiAccessService.getSecHubClient();

        SecHubStatus status = null;
        try {
            // refresh the status before retrieving it
            client.triggerRefreshOfSecHubSchedulerStatus();

            status = client.fetchSecHubStatus();
        } catch (SecHubClientException e) {
            e.printStackTrace();
        }

        return status;
    }

    public boolean isSecHubServerAlive() {
        SecHubClient client = apiAccessService.getSecHubClient();

        try {
            return client.isServerAlive();
        } catch (SecHubClientException e) {
            return false;
        }
    }

    public String getServerVersion() {
        try {
            return apiAccessService.getSecHubClient().getServerVersion();
        } catch (SecHubClientException e) {
            return "invalid";
        }
    }
}
