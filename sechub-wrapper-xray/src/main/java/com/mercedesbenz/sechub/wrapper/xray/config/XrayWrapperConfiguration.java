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
    private final String register;

    private final String zipDirectory;

    private final String secHubReport;

    private int waitUntilRetrySec = 10;

    private int requestRetries = 10;

    private long maxScanDurationHours = 5;

    public static class Builder {
        private final String artifactory;

        private final String register;

        private final String zipDirectory;

        private final String secHubReport;

        private Builder(String artifactory, String register, String zipDirectory, String secHubReport) {
            this.artifactory = artifactory;
            this.register = register;
            this.zipDirectory = zipDirectory;
            this.secHubReport = secHubReport;
        }

        public static Builder builder(String artifactory, String register, String zipDirectory, String secHubReport) {
            if (artifactory == null || zipDirectory == null) {
                throw new NullPointerException("Artifactory URL or Zip file directory cannot be null");
            }
            return new Builder(artifactory, register, zipDirectory, secHubReport);
        }

        public XrayWrapperConfiguration build() {
            return new XrayWrapperConfiguration(this.artifactory, this.register, this.zipDirectory, this.secHubReport);
        }
    }

    private XrayWrapperConfiguration(String artifactory, String register, String zipDirectory, String secHubReport) {
        this.artifactory = artifactory;
        this.register = register;
        this.zipDirectory = zipDirectory;
        this.secHubReport = secHubReport;
    }

    public String getArtifactory() {
        return artifactory;
    }

    public String getRegister() {
        return register;
    }

    public String getSecHubReport() {
        return secHubReport;
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

    public long getMaxScanDurationHours() {
        return maxScanDurationHours;
    }
}
