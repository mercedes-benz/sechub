// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.credentials;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.webui.sechubaccess.SecHubAccessService;

@Service
class NewApiTokenService {

    private final SecHubAccessService accessService;

    NewApiTokenService(SecHubAccessService accessService) {
        this.accessService = accessService;
    }

    /**
     * Request new API token as described in <a href=
     * "https://mercedes-benz.github.io/sechub/latest/sechub-restapi.html#user-
     * requests-new-api-token">documentation</a>
     *
     * <pre>
     *  curl
     * 'https://sechub.example.com/api/anonymous/refresh/apitoken/emailAddress@test.
     * com' -i -X POST -H 'Content-Type: application/json;charset=UTF-8'
     * </pre>
     */
    boolean requestNewApiToken(String emailAddress) {
        /* @formatter:off */

        return accessService.createExecutorForResult(Boolean.class).
            whenDoing("request a new api token").
            callAndReturn(client -> {
                client.requestNewApiToken(emailAddress);
                return Boolean.TRUE;
            }).
            onErrorReturn(exception -> Boolean.FALSE).
            execute();

        /* @formatter:on */
    }
}
