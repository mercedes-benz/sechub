// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class NewApiTokenService {
    @Autowired
    private WebClient webClient;

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
    public Mono<String> requestNewApiToken(String email) {

        return webClient.post().uri("api/anonymous/refresh/apitoken/" + email).retrieve()
                .onStatus(HttpStatus::is4xxClientError, error -> Mono.error(new RuntimeException("API not found")))
                .onStatus(HttpStatus::is5xxServerError, error -> Mono.error(new RuntimeException("Server is not responding"))).bodyToMono(String.class);
    }
}
