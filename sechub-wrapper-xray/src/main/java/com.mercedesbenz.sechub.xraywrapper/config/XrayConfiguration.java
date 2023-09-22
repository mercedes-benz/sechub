package com.mercedesbenz.sechub.xraywrapper.config;

public class XrayConfiguration {

    private final String artifactory;

    private final String register;

    private final String scan_type;

    private final String zip_directory;

    private final String secHubReport;

    private int waitUntilRetrySec = 10;

    private int requestRetries = 10;

    // todo: builder

    public XrayConfiguration(String artifactory, String register, String scan_type, String report_filename, String secHubReport) {
        this.artifactory = artifactory;
        this.register = register;
        this.scan_type = scan_type;
        this.zip_directory = report_filename;
        this.secHubReport = secHubReport;
    }

    public String getArtifactory() {
        return artifactory;
    }

    public String getRegister() {
        return register;
    }

    public String getScan_type() {
        return scan_type;
    }

    public String getSecHubReport() {
        return secHubReport;
    }

    public String getZip_directory() {
        return zip_directory;
    }

    public int getWaitUntilRetrySec() {
        return waitUntilRetrySec;
    }

    public int getRequestRetries() {
        return requestRetries;
    }
}
