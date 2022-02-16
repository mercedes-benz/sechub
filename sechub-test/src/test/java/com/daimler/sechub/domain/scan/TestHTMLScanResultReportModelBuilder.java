package com.daimler.sechub.domain.scan;

import com.daimler.sechub.domain.scan.report.ScanReportTrafficLightCalculator;

public class TestHTMLScanResultReportModelBuilder extends HTMLScanResultReportModelBuilder {

    public TestHTMLScanResultReportModelBuilder(ScanReportTrafficLightCalculator calculator) {
        this.trafficLightCalculator = calculator;
    }

}
