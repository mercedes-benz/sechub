// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.configuration;

import javax.annotation.PostConstruct;
import javax.crypto.SealedObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

@Service
public class CredentialService {
    private SealedObject sealedApiToken;

    private CryptoAccess<String> cryptoAccess = new CryptoAccess<>();

    @Value("${webui.sechub.userid}")
    private String userId;

    @Value("${webui.sechub.apitoken}")
    private String apiToken;

    @PostConstruct
    void handleSensitiveInformation() {
        // Encrypt (seal apiToken)
        sealedApiToken = cryptoAccess.seal(apiToken);

        // Overwrite apiToken value in memory
        apiToken = null;
    }

    public String getUserId() {
        return userId;
    }

    public String getApiToken() {
        return cryptoAccess.unseal(sealedApiToken);
    }
}
