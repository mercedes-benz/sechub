// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import static org.junit.Assert.*;

import java.util.List;

import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubReportModel;
import com.mercedesbenz.sechub.commons.model.TrafficLight;

class AssertCalculation {

    private ScanReportTrafficLightCalculator calculator;
    private SecHubReportModel currentResult;

    private AssertCalculation(ScanReportTrafficLightCalculator calculator) {
        this.calculator = calculator;
    }

    public AssertCalculation isFilteringFindingsTo(TrafficLight wanted, SecHubFinding... findings) {
        List<SecHubFinding> filtered = calculator.filterFindingsFor(currentResult.getResult(), wanted);

        assertNotNull(filtered); // never null!
        for (SecHubFinding finding : findings) {
            assertTrue("Finding missing:" + finding, filtered.contains(finding)); // just same object. equals not custom implemented
        }
        assertEquals(findings.length, filtered.size());
        return this;

    }

    public AssertCalculation isCalculatedTo(TrafficLight light) {
        TrafficLight calcLight = calculator.calculateTrafficLight(currentResult);

        assertNotNull(calcLight); // never null!
        assertEquals("calculated light not as expected!", light, calcLight);

        return this;

    }

    public AssertCalculation withResult(SecHubReportModel result) {
        this.currentResult = result;
        return this;
    }

    public static AssertCalculation assertCalculator(ScanReportTrafficLightCalculator calculator) {
        assertNotNull(calculator);
        return new AssertCalculation(calculator);
    }
}