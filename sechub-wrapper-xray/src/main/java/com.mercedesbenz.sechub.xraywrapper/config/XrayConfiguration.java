package com.mercedesbenz.sechub.xraywrapper.config;

public class XrayConfiguration {

    private final String artifactory;

    private final String register;

    private final String ScanType;

    private final String zipDirectory;

    private final String secHubReport;

    private int waitUntilRetrySec = 10;

    private int requestRetries = 10;

    // todo: builder

    public XrayConfiguration(String artifactory, String register, String ScanType, String zipDirectory, String secHubReport) {
        this.artifactory = artifactory;
        this.register = register;
        this.ScanType = ScanType;
        this.zipDirectory = zipDirectory;
        this.secHubReport = secHubReport;
    }

    public String getArtifactory() {
        return artifactory;
    }

    public String getRegister() {
        return register;
    }

    public String getScan_type() {
        return ScanType;
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
