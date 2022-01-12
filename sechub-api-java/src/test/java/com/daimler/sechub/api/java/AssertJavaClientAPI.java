// SPDX-License-Identifier: MIT
package com.daimler.sechub.api.java;

public class AssertJavaClientAPI {
    
    public static AssertReport assertReport(SecHubReport report) {
        return new AssertReport(report);
    }
}
