package com.mercedesbenz.sechub.xraywrapper.config;

public class XrayWrapperConfiguration {

    private final String artifactory;

    private final String register;

    private final String zipDirectory;

    private final String secHubReport;

    private int waitUntilRetrySec = 10;

    private int requestRetries = 10;

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

        public static Builder create(String artifactory, String register, String zipDirectory, String secHubReport) {
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

    public String getZip_directory() {
        return zipDirectory;
    }

    public int getWaitUntilRetrySec() {
        return waitUntilRetrySec;
    }

    public int getRequestRetries() {
        return requestRetries;
    }
}
