// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import java.net.URI;
import java.net.URISyntaxException;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitRuntimeException;

public class BaseTargetUriFactory {

    public URI create(String targetUri) {
        return createBaseURIForTarget(targetUri);
    }

    private URI createBaseURIForTarget(String targetUri) {
        if (targetUri == null) {
            throw new MustExitRuntimeException("Target URI may not be null.", MustExitCode.TARGET_URL_CONFIGURATION_INVALID);
        }
        String sanitizedTargetUri = sanitizeTargetUri(targetUri);
        URI uri;
        try {
            uri = URI.create(sanitizedTargetUri);
        } catch (IllegalArgumentException e) {
            throw new MustExitRuntimeException("Target URI could not be converted from string.", e, MustExitCode.TARGET_URL_CONFIGURATION_INVALID);
        }

        String scheme = uri.getScheme();
        if (!isValidScheme(scheme)) {
            throw new MustExitRuntimeException("URI: " + uri.toString() + " does not contain valid scheme!", MustExitCode.TARGET_URL_CONFIGURATION_INVALID);
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
            throw new MustExitRuntimeException("Was not able to build base uri for: " + uri, e, MustExitCode.TARGET_URL_CONFIGURATION_INVALID);
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
