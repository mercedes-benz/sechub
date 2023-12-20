// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.config;

public class XrayWrapperConfiguration {

    /**
     * Artifactory url of the jfrog xray instance example: <a
     * https://my-url.artifactory.com /a>
     */
    private final String artifactory;

    /**
     * Register name in the artifactory where artifacts are stored example:
     * local-docker-register
     */
    private final String registry;

    private final String zipDirectory;

    private final String xrayPdsReport;

    private int waitUntilRetrySec = 10;

    private int requestRetries = 10;

    private long maxScanDurationMinutes = 120;

    public static class Builder {
        private String artifactory;

        private String registry;

        private String zipDirectory;

        private String xrayPdsReport;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public XrayWrapperConfiguration build() {
            if (artifactory == null || zipDirectory == null) {
                throw new IllegalStateException("Artifactory URL or Zip file directory cannot be null");
            }
            return new XrayWrapperConfiguration(this.artifactory, this.registry, this.zipDirectory, this.xrayPdsReport);
        }

        public Builder artifactory(String artifactory) {
            this.artifactory = artifactory;
            return this;
        }

        public Builder registry(String registry) {
            this.registry = registry;
            return this;
        }

        public Builder zipDirectory(String zipDirectory) {
            this.zipDirectory = zipDirectory;
            return this;
        }

        public Builder xrayPdsReport(String xrayPdsReport) {
            this.xrayPdsReport = xrayPdsReport;
            return this;
        }
    }

    private XrayWrapperConfiguration(String artifactory, String registry, String zipDirectory, String xrayPdsReport) {
        this.artifactory = artifactory;
        this.registry = registry;
        this.zipDirectory = zipDirectory;
        this.xrayPdsReport = xrayPdsReport;
    }

    public String getArtifactory() {
        return artifactory;
    }

    public String getRegistry() {
        return registry;
    }

    public String getXrayPdsReport() {
        return xrayPdsReport;
    }

    public String getZipDirectory() {
        return zipDirectory;
    }

    public int getWaitUntilRetrySeconds() {
        return waitUntilRetrySec;
    }

    public int getRequestRetries() {
        return requestRetries;
    }

    public long getMaxScanDurationMinutes() {
        return maxScanDurationMinutes;
    }
}
