// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.api.SecHubClientException;
import com.mercedesbenz.sechub.webui.configuration.SecHubAccessService;

@Service
public class NewApiTokenService {

    @Autowired
    SecHubAccessService accessService;

    /**
     * Request new API token as described in
     *
     * <a href=
     * "https://mercedes-benz.github.io/sechub/latest/sechub-restapi.html#user-
     * requests-new-api-token">documentation</a>
     *
     * <pre>
     *  curl
     * 'https://sechub.example.com/api/anonymous/refresh/apitoken/emailAddress@test.
     * com' -i -X POST -H 'Content-Type: application/json;charset=UTF-8'
     * </pre>
     */
    public boolean userRequestsNewApiToken(String emailAddress) {
        try {
            accessService.getSecHubClient().userRequestsNewApiToken(emailAddress);
            return true;
        } catch (SecHubClientException e) {
            return false;
        }
    }
}
