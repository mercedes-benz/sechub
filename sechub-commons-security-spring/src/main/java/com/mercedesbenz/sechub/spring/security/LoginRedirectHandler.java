// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The {@code LoginRedirectHandler} handles the redirection of the user the
 * desired redirect uri after successful authentication. The redirect uri is
 * specified as a query parameter in the request. If the redirect uri is not
 * specified, the default redirect uri from the configuration is used.
 *
 * @author hamidonos
 */
class LoginRedirectHandler {

    private static final String REDIRECT_PATH = "redirectPath";

    private final String defaultRedirectUri;

    LoginRedirectHandler(String defaultRedirectUri) {
        this.defaultRedirectUri = requireNonNull(defaultRedirectUri, "Property 'defaultRedirectUri' must not be null");
    }

    void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String[]> parameterMap = request.getParameterMap();

        String location = defaultRedirectUri;

        if (parameterMap.containsKey(REDIRECT_PATH)) {
            String redirectPath = parameterMap.remove(REDIRECT_PATH)[0];
            if (!isPathOnly(redirectPath)) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Invalid redirect path: %s".formatted(redirectPath));
            }
            String queryString = buildQueryString(parameterMap);
            location = redirectPath.concat(redirectPath.contains("?") ? "&" : "?") + queryString;
        }

        response.sendRedirect(location);
    }

    private static boolean isPathOnly(String input) {
        try {
            URI uri = new URI(input);
            /* Return true if the URI is not absolute and starts with "/" (i.e., a path) */
            return !uri.isAbsolute() && (uri.getPath() != null && input.startsWith("/"));
        } catch (Exception e) {
            /* If parsing fails, treat as not a valid path */
            return false;
        }
    }

    private static String buildQueryString(Map<String, String[]> paramMap) {
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            for (String value : entry.getValue()) {
                if (!query.isEmpty())
                    query.append("&");
                query.append(key).append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            }
        }
        return query.toString();
    }

}
