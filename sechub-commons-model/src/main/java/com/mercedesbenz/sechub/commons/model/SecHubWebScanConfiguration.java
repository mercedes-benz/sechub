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
    public static final String PROPERTY_INCLUDES = "includes";
    public static final String PROPERTY_EXCLUDES = "excludes";
    public static final String PROPERTY_HEADERS = "haders";

    Optional<WebLoginConfiguration> login = Optional.empty();
    Optional<WebScanDurationConfiguration> maxScanDuration = Optional.empty();

    Optional<SecHubWebScanApiConfiguration> api = Optional.empty();

    URI url;

    Optional<List<String>> includes = Optional.empty();
    Optional<List<String>> excludes = Optional.empty();

    Optional<List<HTTPHeaderConfiguration>> headers = Optional.empty();

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
}
