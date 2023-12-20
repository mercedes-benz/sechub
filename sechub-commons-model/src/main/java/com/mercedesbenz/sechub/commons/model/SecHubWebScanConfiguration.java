// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubWebScanConfiguration {

    public static final String PROPERTY_URL = "url";
    public static final String PROPERTY_LOGIN = "login";
    public static final String PROPERTY_MAX_SCAN_DURATION = "maxScanDuration";
    public static final String PROPERTY_API = "api";
    public static final String PROPERTY_INCLUDES = "includes";
    public static final String PROPERTY_EXCLUDES = "excludes";
    public static final String PROPERTY_HEADERS = "headers";
    public static final String PROPERTY_CLIENT_CERTIFICATE = "clientCertificate";

    public static final String WEBSCAN_URL_WILDCARD_SYMBOL = "<*>";

    Optional<WebLoginConfiguration> login = Optional.empty();
    Optional<WebScanDurationConfiguration> maxScanDuration = Optional.empty();

    Optional<SecHubWebScanApiConfiguration> api = Optional.empty();

    URI url;

    Optional<List<String>> includes = Optional.empty();
    Optional<List<String>> excludes = Optional.empty();

    Optional<List<HTTPHeaderConfiguration>> headers = Optional.empty();

    Optional<ClientCertificateConfiguration> clientCertificate = Optional.empty();

    public URI getUrl() {
        return url;
    }

    public Optional<SecHubWebScanApiConfiguration> getApi() {
        return api;
    }

    public Optional<WebLoginConfiguration> getLogin() {
        return login;
    }

    public Optional<WebScanDurationConfiguration> getMaxScanDuration() {
        return maxScanDuration;
    }

    public Optional<List<String>> getIncludes() {
        return includes;
    }

    public Optional<List<String>> getExcludes() {
        return excludes;
    }

    public Optional<List<HTTPHeaderConfiguration>> getHeaders() {
        return headers;
    }

    public Optional<ClientCertificateConfiguration> getClientCertificate() {
        return clientCertificate;
    }

    public void setLogin(Optional<WebLoginConfiguration> login) {
        this.login = login;
    }

    public void setMaxScanDuration(Optional<WebScanDurationConfiguration> maxScanDuration) {
        this.maxScanDuration = maxScanDuration;
    }

    public void setApi(Optional<SecHubWebScanApiConfiguration> api) {
        this.api = api;
    }

    public void setUrl(URI url) {
        this.url = url;
    }

    public void setIncludes(Optional<List<String>> includes) {
        this.includes = includes;
    }

    public void setExcludes(Optional<List<String>> excludes) {
        this.excludes = excludes;
    }

    public void setClientCertificate(Optional<ClientCertificateConfiguration> clientCertificate) {
        this.clientCertificate = clientCertificate;
    }

}
