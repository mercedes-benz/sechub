// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;

public class BaseTargetUriFactory {

    public URL create(String targetUri) {
        try {
            return createBaseURIForTarget(targetUri).toURL();
        } catch (MalformedURLException e) {
            throw new ZapWrapperRuntimeException("Target URL is not a valid URL.", ZapWrapperExitCode.TARGET_URL_INVALID);
        }
    }

    private URI createBaseURIForTarget(String targetUri) {
        if (targetUri == null) {
            throw new ZapWrapperRuntimeException("Target URI may not be null.", ZapWrapperExitCode.TARGET_URL_INVALID);
        }
        String sanitizedTargetUri = sanitizeTargetUri(targetUri);
        URI uri;
        try {
            uri = URI.create(sanitizedTargetUri);
        } catch (IllegalArgumentException e) {
            throw new ZapWrapperRuntimeException("Target URI could not be converted from string.", e, ZapWrapperExitCode.TARGET_URL_INVALID);
        }

        String scheme = uri.getScheme();
        if (!isValidScheme(scheme)) {
            throw new ZapWrapperRuntimeException("URI: " + uri.toString() + " does not contain valid scheme!", ZapWrapperExitCode.TARGET_URL_INVALID);
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
            throw new ZapWrapperRuntimeException("Was not able to build base uri for: " + uri, e, ZapWrapperExitCode.TARGET_URL_INVALID);
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
