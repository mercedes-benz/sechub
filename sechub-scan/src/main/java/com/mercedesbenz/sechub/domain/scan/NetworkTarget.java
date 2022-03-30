// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.net.InetAddress;
import java.net.URI;
import java.util.Objects;

import com.mercedesbenz.sechub.sharedkernel.util.Assert;

/**
 * NetworkTarget class. This will contain meta data and information about the
 * kind of the target. E.g. if its INTRANET or INTERNET
 *
 * @author Albert Tregnaghi
 *
 */
public class NetworkTarget {

    NetworkTargetType type;
    URI uri;
    InetAddress inetAdress;

    public NetworkTarget(InetAddress inetAdress, NetworkTargetType type) {
        assertTargetTypeNotNull(type);

        this.inetAdress = inetAdress;
        this.type = type;
    }

    public NetworkTarget(URI uri, NetworkTargetType type) {
        assertTargetTypeNotNull(type);

        this.uri = uri;
        this.type = type;
    }

    /**
     * @return target type, never <code>null</code>
     */
    public NetworkTargetType getType() {
        return type;
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
        return "NetworkTarget [type=" + type + ",uri=" + uri + ", inetAdress=" + inetAdress + "]";
    }

    private void assertTargetTypeNotNull(NetworkTargetType type) {
        Assert.notNull(type, "target type may not be null!");
    }

    @Override
    public int hashCode() {
        return Objects.hash(inetAdress, type, uri);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NetworkTarget other = (NetworkTarget) obj;
        return Objects.equals(inetAdress, other.inetAdress) && type == other.type && Objects.equals(uri, other.uri);
    }

}
