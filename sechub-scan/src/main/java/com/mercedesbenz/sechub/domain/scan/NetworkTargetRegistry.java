// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NetworkTarget registry is a data container object used by product executors.
 * <br>
 * It is created on execution time and filled up with configuration data. <br>
 * After fill up the target will be registered and target registry info can be
 * fetched, which contains runtime information.
 *
 * @author Albert Tregnaghi
 *
 */
public class NetworkTargetRegistry {

    private Map<NetworkTargetType, List<NetworkTarget>> map = new EnumMap<>(NetworkTargetType.class);

    public NetworkTargetRegistry() {

    }

    /**
     * Register given target which shall be scanned by product exececutor (if type
     * is supported...)
     *
     * @param target
     */
    public void register(NetworkTarget target) {
        notNull(target, "target may not be null!");

        NetworkTargetType type = target.getType();
        notNull(type, "target type may not be null!");

        List<NetworkTarget> list = getListFor(type);
        list.add(target);
    }

    /**
     * Creates a registry info containing results for given target type. (info will
     * resolve IPs,URLs etc.)
     *
     * @param type
     * @return
     */
    public NetworkTargetInfo createRegistryInfo(NetworkTargetType type) {
        return new NetworkTargetInfo(type);
    }

    /**
     * @param type
     * @return unmodifiable list for given target type, never <code>null</code>
     */
    public List<NetworkTarget> getTargetsFor(NetworkTargetType type) {
        notNull(type, "given type may not be null!");

        List<NetworkTarget> list = getListFor(type);
        return Collections.unmodifiableList(list);
    }

    List<NetworkTarget> getListFor(NetworkTargetType type) {
        return map.computeIfAbsent(type, k -> new ArrayList<>());
    }

    /**
     * Represents target registry execution information.
     *
     * @author Albert Tregnaghi
     *
     */
    public class NetworkTargetInfo {

        private NetworkTargetType type;

        private NetworkTargetInfo(NetworkTargetType type) {
            notNull(type, "type may not be null!");

            this.type = type;
        }

        /**
         * Returns a set for target URIs for this kind of {@link NetworkTargetType}
         *
         * @return set, never <code>null</code>
         */
        public Set<URI> getURIs() {
            List<NetworkTarget> list = NetworkTargetRegistry.this.getTargetsFor(type);
            Set<URI> uris = new LinkedHashSet<>();
            for (NetworkTarget target : list) {
                URI uri = target.getURI();
                if (uri != null) {
                    uris.add(uri);
                }
            }
            return uris;
        }

        /**
         * Returns target URI for this kind of {@link NetworkTargetType}, which will be
         * the first element from URI list.
         *
         * This is a convenience method for scan types where we can have only ONE target
         * URI (e.g. web scans).
         *
         * @return uri or <code>null</code>
         */
        public URI getURI() {
            URI uri = null;

            Set<URI> uris = getURIs();

            if (!uris.isEmpty()) {
                // get the first element
                uri = uris.iterator().next();
            }

            return uri;
        }

        public Set<InetAddress> getIPs() {
            List<NetworkTarget> list = NetworkTargetRegistry.this.getTargetsFor(type);
            Set<InetAddress> inetAdresses = new LinkedHashSet<>();
            for (NetworkTarget target : list) {
                InetAddress uri = target.getInetAdress();
                if (uri != null) {
                    inetAdresses.add(uri);
                }
            }
            return inetAdresses;
        }

        /**
         * @return the {@link NetworkTargetType} for this scan
         */
        public NetworkTargetType getTargetType() {
            return type;
        }

        /**
         *
         * @return <code>true</code> when at least one acceptable target exists matching
         *         the configuration of the product.
         */
        public boolean containsAtLeastOneTarget() {
            boolean containsAtLeastOneTarget = !getURIs().isEmpty();
            containsAtLeastOneTarget = containsAtLeastOneTarget || !getIPs().isEmpty();
            return containsAtLeastOneTarget;
        }

    }

}
