// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class StatusService {
    @Autowired
    WebClient webClient;

    @Autowired
    CredentialService credentialService;

    // TODO: Use the generated SecHub API client
    // TODO: Figure out what credentials are used to communicate to SecHub and how
    // they get injected
    public Mono<String> getServerVersion() {
        return webClient.get().uri("/api/admin/info/version")
                .headers(httpHeaders -> httpHeaders.setBasicAuth(credentialService.getUserId(), credentialService.getApiToken())).retrieve()
                .onStatus(HttpStatus::is4xxClientError, error -> Mono.error(new RuntimeException("API not found")))
                .onStatus(HttpStatus::is5xxServerError, error -> Mono.error(new RuntimeException("Server is not responding"))).bodyToMono(String.class);

    }
}
