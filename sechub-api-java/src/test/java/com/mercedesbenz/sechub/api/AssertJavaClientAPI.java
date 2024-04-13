// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

public class AssertJavaClientAPI {

    public static AssertReport assertReport(SecHubReport report) {
        return new AssertReport(report);
    }
}
