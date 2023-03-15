// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

/**
 * Represents report versions. Those versions have a major and a minor part.
 * Minor number changes do represents non breaking changes (so only additions to
 * former report versions). But when a major number increases this means the
 * report structure has breaking changes.
 *
 * @author Albert Tregnaghi
 *
 */
public enum SecHubReportVersion {

    /**
     * With this report version fields were introduced: status, messages and
     * reportVersion.
     */
    VERSION_1_0(1, 0),

    ;

    private int major;
    private int minor;

    SecHubReportVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public String getVersionAsString() {
        return major + "." + minor;
    }
}
