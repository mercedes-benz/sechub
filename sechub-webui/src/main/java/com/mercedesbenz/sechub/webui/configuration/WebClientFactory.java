// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.configuration;

import javax.net.ssl.SSLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import com.mercedesbenz.sechub.webui.SecHubServerAccessService;

import reactor.netty.http.client.HttpClient;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

@Configuration
public class WebClientFactory {
	@Autowired
	SecHubServerAccessService accessService;

    @Bean
    public WebClient webClient() throws SSLException {
        WebClient webClient = null;

        /* @formatter:off */
		if (accessService.isTrustAllCertificates()) {
			SslContext sslContext = SslContextBuilder.
					forClient().
					trustManager(InsecureTrustManagerFactory.INSTANCE).
					build();

			HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));

			webClient = WebClient.builder().
					baseUrl(accessService.getSecHubServerUrl()).
					clientConnector(new ReactorClientHttpConnector(httpClient)).
					build();

		} else {
			webClient = WebClient.builder().
					baseUrl(accessService.getSecHubServerUrl()).
					build();
		}
		/* @formatter:on */

        return webClient;
    }
}
