package com.daimler.sechub.client.java;

public class AssertJavaClientAPI {
    
    public static AssertReport assertReport(SecHubReport report) {
        return new AssertReport(report);
    }
}
