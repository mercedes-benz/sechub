// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

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
 * Target registry is a data container object used by product executors. <br>
 * It is created on execution time and filled up with configuration data. <br>
 * After fill up the target will be registered and target registry info can be
 * fetched, which contains runtime information.
 *
 * @author Albert Tregnaghi
 *
 */
public class TargetRegistry {

    private Map<TargetType, List<Target>> map = new EnumMap<>(TargetType.class);

    public TargetRegistry() {

    }

    /**
     * Register given target which shall be scanned by product exececutor (if type
     * is supported...)
     *
     * @param target
     */
    public void register(Target target) {
        notNull(target, "target may not be null!");

        TargetType type = target.getType();
        notNull(type, "target type may not be null!");

        List<Target> list = getListFor(type);
        list.add(target);
    }

    /**
     * Creates a registry info containing results for given target type. (info will
     * resolve IPs,URLs etc.)
     *
     * @param type
     * @return
     */
    public TargetRegistryInfo createRegistryInfo(TargetType type) {
        return new TargetRegistryInfo(type);
    }

    /**
     * @param type
     * @return unmodifiable list for given target type, never <code>null</code>
     */
    public List<Target> getTargetsFor(TargetType type) {
        notNull(type, "given type may not be null!");

        List<Target> list = getListFor(type);
        return Collections.unmodifiableList(list);
    }

    List<Target> getListFor(TargetType type) {
        return map.computeIfAbsent(type, k -> new ArrayList<>());
    }

    /**
     * Represents target registry execution information.
     *
     * @author Albert Tregnaghi
     *
     */
    public class TargetRegistryInfo {

        private TargetType type;

        private TargetRegistryInfo(TargetType type) {
            notNull(type, "type may not be null!");

            this.type = type;
        }

        /**
         * Returns a set for target URIs for this kind of {@link TargetType}
         *
         * @return set, never <code>null</code>
         */
        public Set<URI> getURIs() {
            List<Target> list = TargetRegistry.this.getTargetsFor(type);
            Set<URI> uris = new LinkedHashSet<>();
            for (Target target : list) {
                URI uri = target.getUrl();
                if (uri != null) {
                    uris.add(uri);
                }
            }
            return uris;
        }

        /**
         * Returns target URI for this kind of {@link TargetType}, which will be the
         * first element from URI list.
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

        /**
         * @return a set, never <code>null</code> containing all system folder pathes
         *         used for code upload, or an empty list
         */
        public Set<String> getCodeUploadFileSystemFolders() {
            List<Target> list = TargetRegistry.this.getTargetsFor(type);
            Set<String> identifiers = new LinkedHashSet<>();
            for (Target target : list) {
                String identifier = target.getIdentifierWithoutPrefix();
                if (identifier != null) {
                    identifiers.add(identifier);
                }
            }
            return identifiers;
        }

        public Set<InetAddress> getIPs() {
            List<Target> list = TargetRegistry.this.getTargetsFor(type);
            Set<InetAddress> inetAdresses = new LinkedHashSet<>();
            for (Target target : list) {
                InetAddress uri = target.getInetAdress();
                if (uri != null) {
                    inetAdresses.add(uri);
                }
            }
            return inetAdresses;
        }

        /**
         * @return the {@link TargetType} for this scan
         */
        public TargetType getTargetType() {
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
            containsAtLeastOneTarget = containsAtLeastOneTarget || !getCodeUploadFileSystemFolders().isEmpty();
            return containsAtLeastOneTarget;
        }

    }

}
