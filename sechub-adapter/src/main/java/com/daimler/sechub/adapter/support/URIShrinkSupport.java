// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.support;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URIShrinkSupport {

    private static final Logger LOG = LoggerFactory.getLogger(URIShrinkSupport.class);

    /**
     * Shrinks given uris to a set containing only {@link URI} elements which
     * represent same protocol, hostname and port
     *
     * @param uris
     * @return set of {@link URI} , never <code>null</code>
     */
    public Set<URI> shrinkToRootURIs(Collection<URI> uris) {
        Set<URI> rootURIs = new LinkedHashSet<>();

        if (uris == null) {
            return rootURIs;
        }

        for (URI uri : uris) {
            if (uri != null) {
                URI rootURI = buildRootURI(uri);
                rootURIs.add(rootURI);
            }
        }

        return rootURIs;
    }

    /**
     * Shrinks a given URI to a set containing only {@link URI} elements which
     * represent same protocol, hostname and port
     *
     * @param uri
     * @return set of {@link URI} , never <code>null</code>
     */
    public URI shrinkToRootURI(URI uri) {
        if (uri == null) {
            return null;
        }

        return buildRootURI(uri);
    }

    private URI buildRootURI(URI uri) {

        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();

        String userInfo = null;
        String path = null;
        String query = null;
        String fragment = null;

        URI rootURI = null;
        try {
            rootURI = new URI(scheme, userInfo, host, port, path, query, fragment);
        } catch (URISyntaxException e) {
            LOG.error("Was not able to build root uri for:" + uri, e);
        }

        return rootURI;
    }
}
