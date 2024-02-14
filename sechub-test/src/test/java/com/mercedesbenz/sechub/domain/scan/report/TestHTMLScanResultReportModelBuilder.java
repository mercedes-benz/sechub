// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import com.mercedesbenz.sechub.commons.model.SecHubResultTrafficLightFilter;

public class TestHTMLScanResultReportModelBuilder extends HTMLScanResultReportModelBuilder {

    public TestHTMLScanResultReportModelBuilder(SecHubResultTrafficLightFilter trafficLightFilter) {
        this.trafficLightFilter = trafficLightFilter;
    }

}
