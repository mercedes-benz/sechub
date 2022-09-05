package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class StatusService {
    @Autowired
    private WebClient webClient;

    @Value("${sechub.userid}")
    private String userId;

    @Value("${sechub.apiToken}")
    private String apiToken;

    public String getServerVersion() {
        return webClient.get().uri("/api/admin/info/version").headers(httpHeaders -> httpHeaders.setBasicAuth(userId, apiToken)).retrieve()
                .onStatus(HttpStatus::is4xxClientError, error -> Mono.error(new RuntimeException("API not found")))
                .onStatus(HttpStatus::is5xxServerError, error -> Mono.error(new RuntimeException("Server is not responding"))).bodyToMono(String.class).block();
    }
}
