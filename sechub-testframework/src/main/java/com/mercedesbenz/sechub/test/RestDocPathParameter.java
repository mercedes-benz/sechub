// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

/**
 * This enum represents common used identifiers inside rest documentation. Use
 * this enumeration for building URLs inside your RestDoc tests and use
 * parameters for data, so documentation is using these parameters - see
 * references for examples.
 *
 * @author Albert Tregnaghi
 *
 */
public enum RestDocPathParameter {
    JOB_UUID("jobUUID"),

    PROJECT_ID("projectId"),

    USER_ID("userId"),

    ONE_TIME_TOKEN("oneTimeToken"),

    EMAIL_ADDRESS("emailAddress"),

    MAPPING_ID("mappingId"),

    FINDING_ID("findingId"),

    PROFILE_ID("profileId"),

    UUID_PARAMETER("uuid"),

    PROJECT_ACCESS_LEVEL("projectAccessLevel"),

    SIZE("size"),

    PAGE("page"),

    WITH_META_DATA("withMetaData"),

    ;

    private String restDocName;
    private String urlPart;

    private RestDocPathParameter(String id) {
        this.restDocName = id;
        this.urlPart = "{" + id + "}";
    }

    /**
     *
     * We do NOT use name() because its an enum...
     *
     * @return The name of the parameter - e.g. when path element is "{userId}" then
     *         this method returns "userId".
     */
    public String paramName() {
        return restDocName;
    }

    /**
     * @return path element in url. For example: when pathName is "userId" this
     *         method returns "{userId}"
     */
    public String pathElement() {
        return urlPart;
    }
}