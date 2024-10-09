// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.page.credentials;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.webui.sechubaccess.SecHubAccessService;

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
    public boolean requestNewApiToken(String emailAddress) {
        /* @formatter:off */
        Boolean succesfulSendNewApiToken = accessService.createExecutorForResult(Boolean.class).
            whenDoing("request a new api token").
            callAndReturn(client -> {
                client.requestNewApiToken(emailAddress);
                return Boolean.TRUE;
            }).
            onErrorReturn(exception -> Boolean.FALSE).
            execute();

        return succesfulSendNewApiToken;

        /* @formatter:on */
    }
}
