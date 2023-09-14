package com.mercedesbenz.sechub.xraywrapper.config;

public class XrayConfiguration {

    private String artifactory;

    private String register;

    private String scan_type;

    private String report_filename;

    public XrayConfiguration(String artifactory, String register, String scan_type, String report_filename) {
        this.artifactory = artifactory;
        this.register = register;
        this.scan_type = scan_type;
        this.report_filename = report_filename;
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

    public String getReport_filename() {
        return report_filename;
    }
}
