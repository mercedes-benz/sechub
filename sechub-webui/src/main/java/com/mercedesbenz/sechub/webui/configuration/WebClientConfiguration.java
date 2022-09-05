package com.mercedesbenz.sechub.webui.configuration;

import javax.net.ssl.SSLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfiguration {
	@Value("${sechub.serverUrl}")
	private String secHubServerUrl;
	
	@Value("${sechub.trustAllCertificates}")
	private boolean trustAllCertificates;
	
	@Bean
	public WebClient webClient() throws SSLException {
		WebClient webClient = null;
		
		/* @formatter:off */
		if (trustAllCertificates) {
			SslContext sslContext = SslContextBuilder.
					forClient().
					trustManager(InsecureTrustManagerFactory.INSTANCE).
					build();
			
			HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
			
			webClient = WebClient.builder().
					baseUrl(secHubServerUrl).
					clientConnector(new ReactorClientHttpConnector(httpClient)).
					build();
			
		} else {
			webClient = WebClient.builder().
					baseUrl(secHubServerUrl).
					build();
		}
		/* @formatter:on */
		
		return webClient;
	}
}
