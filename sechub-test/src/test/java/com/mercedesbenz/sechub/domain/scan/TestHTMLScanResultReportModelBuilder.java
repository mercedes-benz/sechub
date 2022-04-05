// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import com.mercedesbenz.sechub.domain.scan.report.ScanReportTrafficLightCalculator;

public class TestHTMLScanResultReportModelBuilder extends HTMLScanResultReportModelBuilder {

    public TestHTMLScanResultReportModelBuilder(ScanReportTrafficLightCalculator calculator) {
        this.trafficLightCalculator = calculator;
    }

}
