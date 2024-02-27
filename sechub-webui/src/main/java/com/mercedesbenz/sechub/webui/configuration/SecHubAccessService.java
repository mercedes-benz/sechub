package com.mercedesbenz.sechub.webui.configuration;

import java.net.URI;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.api.DefaultSecHubClient;
import com.mercedesbenz.sechub.api.MockedSecHubClient;
import com.mercedesbenz.sechub.api.SecHubClient;

@Service
public class SecHubAccessService {
    @Value("${webui.sechub.server-url}")
    private String secHubServerUrl;

    @Value("${webui.sechub.trust-all-certificates}")
    private boolean trustAllCertificates;

    @Value("${webui.client.mocked}")
    private boolean useMockedClient;

    @Autowired
    private CredentialService credentialService;

    private SecHubClient client;

    @PostConstruct
    void setupSecHubClient() {
        URI serverUri = URI.create(secHubServerUrl);

        /* @formatter:off */
        if (useMockedClient) {
            this.client = MockedSecHubClient.from(serverUri, "mocked", "verySecretTrustMe", trustAllCertificates);
        } else {

            this.client = DefaultSecHubClient.builder().
                            server(serverUri).
                            user(credentialService.getUserId()).
                            apiToken(credentialService.getApiToken()).
                            trustAll(trustAllCertificates).
                            build();
        }
        /* @formatter:on */
    }

    public SecHubClient getSecHubClient() {
        return this.client;
    }

    public URI getSecHubServerUri() {
        return this.client.getServerUri();
    }
}
