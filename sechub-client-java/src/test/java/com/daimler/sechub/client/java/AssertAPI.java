package com.daimler.sechub.client.java;

import com.daimler.sechub.client.java.report.SecHubReport;

public class AssertAPI {
    
    public static AssertReport assertReport(SecHubReport report) {
        return new AssertReport(report);
    }
}
