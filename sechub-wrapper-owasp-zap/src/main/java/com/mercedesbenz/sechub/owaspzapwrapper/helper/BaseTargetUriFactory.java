// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import java.net.URI;
import java.net.URISyntaxException;

public class BaseTargetUriFactory {

    public URI create(String targetUri) {
        return createBaseURIForTarget(targetUri);
    }

    private URI createBaseURIForTarget(String targetUri) {
        if (targetUri == null) {
            throw new IllegalArgumentException("Target URI shall not be null.");
        }
        String sanitizedTargetUri = sanitizeTargetUri(targetUri);
        URI uri = URI.create(sanitizedTargetUri);

        String scheme = uri.getScheme();
        if (!isValidScheme(scheme)) {
            throw new IllegalArgumentException("URI: " + uri.toString() + " does not contain valid scheme!");
        }

        String userInfo = null;
        String host = uri.getHost();
        int port = uri.getPort();

        String path = uri.getPath();
        String query = null;
        String fragment = null;

        URI rootURI = null;
        try {
            rootURI = new URI(scheme, userInfo, host, port, path, query, fragment);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Was not able to build base uri for: " + uri, e);
        }
        return rootURI;
    }

    private String sanitizeTargetUri(String targetUri) {
        int index = targetUri.indexOf("/#!");
        if (index != -1) {
            targetUri = targetUri.substring(0, index);
        }

        targetUri = targetUri.replaceAll("#/", "");

        if (targetUri.endsWith("/")) {
            targetUri = targetUri.substring(0, targetUri.lastIndexOf("/"));
        }

        if (targetUri.endsWith("/#")) {
            targetUri = targetUri.substring(0, targetUri.lastIndexOf("/#"));
        }

        return targetUri;
    }

    private boolean isValidScheme(String scheme) {
        if ("http".equals(scheme)) {
            return true;
        }
        if ("https".equals(scheme)) {
            return true;
        }
        return false;
    }
}
