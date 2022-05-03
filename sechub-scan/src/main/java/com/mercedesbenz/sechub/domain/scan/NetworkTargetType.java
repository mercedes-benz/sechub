// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

public enum NetworkTargetType {

    /**
     * NetworkTarget is an URL/IP available from INTRANET
     */
    INTRANET(true),

    /**
     * NetworkTarget is an URL/IP available from INTERNET
     */
    INTERNET(true),

    /**
     * An illegal target, e.g. "localhost", "127.0.0.0"
     */
    ILLEGAL(false),

    /**
     * Just an unidentifiable target
     */
    UNKNOWN(false);

    private boolean valid;

    private NetworkTargetType(boolean valid) {
        this.valid = valid;
    }

    /**
     * @return <code>true</code> when this is a valid target type, means with this
     *         target type a scan can generally be executed (but still dependent on
     *         the product)
     */
    public boolean isValid() {
        return valid;
    }

    public boolean isIntranet() {
        return NetworkTargetType.INTRANET.equals(this);
    }

    public boolean isInternet() {
        return NetworkTargetType.INTERNET.equals(this);
    }

    /**
     * @return an identifier prefix for the target type
     */
    public String getIdentifierPrefix() {
        return name().toLowerCase() + "://";
    }

}
