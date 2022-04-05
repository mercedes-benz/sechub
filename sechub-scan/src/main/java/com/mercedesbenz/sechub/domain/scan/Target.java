// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.net.InetAddress;
import java.net.URI;
import java.util.Objects;

import com.mercedesbenz.sechub.sharedkernel.util.Assert;

/**
 * Target class. This will contain meta data and information about the kind of
 * the target. E.g. if its INTRANET or INTERNET
 *
 * @author Albert Tregnaghi
 *
 */
public class Target {

    TargetType type;
    URI uri;
    InetAddress inetAdress;
    String identifier;

    /**
     * Creates a target for given identifier
     *
     * @param identifier
     * @param type
     * @throws IllegalStateException when target type is not for code
     */
    public Target(String identifier, TargetType type) {
        assertTargetTypeNotNull(type);
        Assert.notNull(identifier, "identifier may not be null!");
        this.type = type;

        boolean supported = type.isCodeUpload();
        if (!supported) {
            throw new IllegalStateException("Currently unsupported but not:" + type);
        }
        /* ensure identifier prefix added */
        this.identifier = type.getIdentifierPrefix() + getIdentifierWithoutPrefix(identifier);
    }

    public Target(InetAddress inetAdress, TargetType type) {
        assertTargetTypeNotNull(type);

        this.inetAdress = inetAdress;
        this.type = type;
    }

    public Target(URI uri, TargetType type) {
        assertTargetTypeNotNull(type);

        this.uri = uri;
        this.type = type;
    }

    /**
     * @return target type, never <code>null</code>
     */
    public TargetType getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public URI getUrl() {
        return uri;
    }

    public InetAddress getInetAdress() {
        return inetAdress;
    }

    public String getUriAsString() {
        if (uri == null) {
            return "null";
        }
        return uri.toString();
    }

    @Override
    public String toString() {
        return "Target [type=" + type + ", id=" + identifier + ", uri=" + uri + ", inetAdress=" + inetAdress + "]";
    }

    private void assertTargetTypeNotNull(TargetType type) {
        Assert.notNull(type, "target type may not be null!");
    }

    public String getIdentifierWithoutPrefix() {
        return getIdentifierWithoutPrefix(identifier);
    }

    protected String getIdentifierWithoutPrefix(String identifier) {
        if (identifier == null) {
            return null;
        }
        if (isStartingWithIdentifiedPrefix(identifier)) {
            return identifier.substring(type.getIdentifierPrefix().length());
        }
        return identifier;
    }

    boolean isStartingWithIdentifiedPrefix(String identifier) {
        return identifier.startsWith(type.getIdentifierPrefix());
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, inetAdress, type, uri);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Target other = (Target) obj;
        return Objects.equals(identifier, other.identifier) && Objects.equals(inetAdress, other.inetAdress) && type == other.type
                && Objects.equals(uri, other.uri);
    }

}
