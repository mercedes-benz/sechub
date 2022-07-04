// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

public class AbstractTestURLBuilder {

    private String protocol;
    private String hostname;
    private int port;

    public AbstractTestURLBuilder(String protocol, int port) {
        this(protocol, port, "localhost");
    }

    public AbstractTestURLBuilder(String protocol, int port, String hostname) {
        this.protocol = protocol;
        this.port = port;
        this.hostname = hostname;
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ common ...................................+ */
    /* +-----------------------------------------------------------------------+ */

    public final String buildUrl(String custom, Object... parts) {
        StringBuilder sb = new StringBuilder();
        sb.append(createRootPath());
        sb.append(custom);
        for (Object pathVariable : parts) {
            boolean notAlreadyEndsWithSlash = sb.charAt(sb.length() - 1) != '/';
            if (notAlreadyEndsWithSlash) {
                sb.append("/");
            }
            sb.append(pathVariable);
        }
        return sb.toString();
    }

    public String buildServerURL() {
        return createRootPath();
    }

    private String createRootPath() {
        return protocol + "://" + hostname + ":" + port;
    }

    public final String buildBaseURL() {
        return buildUrl("");
    }

}
