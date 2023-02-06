// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 2018-02-20 we defined following logic which is tested here:
 *
 * <table border="1">
 * <tr>
 * <td>Critical</td>
 * <td rowspan="2" >Red</td>
 * </tr>
 * <tr>
 * <td>High</td>
 * </tr>
 * <tr>
 * <td>Medium</td>
 * <td>Yellow</td>
 * </tr>
 * <tr>
 * <td>Unclassified</td>
 * <td rowspan="4" >Green</td>
 * </tr>
 * <tr>
 * <td>Low</td>
 * </tr>
 * <tr>
 * <td>Info</td>
 * </tr>
 * <tr>
 * <td>"No findings at all"</td>
 * </tr>
 * </table>
 *
 * @author Albert Tregnaghi
 *
 */
public class TrafficLightSupportTest {

    private TrafficLightSupport supportToTest;

    @BeforeEach
    void before() {
        supportToTest = new TrafficLightSupport();
    }
    /* +-----------------------------------------------------------------------+ */
    /* +............................ filter test ..........................+ */
    /* +-----------------------------------------------------------------------+ */

    @Test
    void having_critical_findings_filtering_to_green_returns_only_empty() {
        assertSupport(supportToTest).withResult(prepareSechubResultWithFindings(Severity.CRITICAL)).isFilteringFindingsTo(TrafficLight.GREEN);
    }

    @Test
    void having_critical_findings_filtering_to_yellow_returns_only_empty() {
        assertSupport(supportToTest).withResult(prepareSechubResultWithFindings(Severity.CRITICAL)).isFilteringFindingsTo(TrafficLight.GREEN);
    }

    @Test
    void having_setup_findings_filtering_to_red_returns_critical_and_high() {
        /* prepare */
        MultiFindingsTestSetup setup = new MultiFindingsTestSetup();

        /* test */
        assertSupport(supportToTest).withResult(setup.reportTransformationResult).isFilteringFindingsTo(TrafficLight.RED, setup.findingCritical,
                setup.findingHigh);
    }

    @Test
    void having_setup_findings_filtering_to_yellow_returns_medium_only() {
        /* prepare */
        MultiFindingsTestSetup setup = new MultiFindingsTestSetup();

        /* test */
        assertSupport(supportToTest).withResult(setup.reportTransformationResult).isFilteringFindingsTo(TrafficLight.YELLOW, setup.findingMedium);
    }

    @Test
    void having_setup_findings_filtering_to_green_returns_low_unclassfied_and_info() {
        /* prepare */
        MultiFindingsTestSetup setup = new MultiFindingsTestSetup();

        /* test */
        assertSupport(supportToTest).withResult(setup.reportTransformationResult).isFilteringFindingsTo(TrafficLight.GREEN, setup.findingLow, setup.findingInfo,
                setup.findingUnclassified);
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Single variants ..........................+ */
    /* +-----------------------------------------------------------------------+ */
    @Test
    void calculator_called_with_null_returns_() {
        assertThrows(IllegalArgumentException.class, () -> supportToTest.calculateTrafficLight(null));
    }

    @Test
    void a_sechub_result_containing_finding_critical__results_in_red() {
        /* @formatter:off */
		assertSupport(supportToTest).
			withResult(prepareSechubResultWithFindings(Severity.CRITICAL)).
			isCalculatedTo(TrafficLight.RED);
		/* @formatter:on */
    }

    @Test
    void a_sechub_result_containing_finding_high__results_in_red() {
        /* @formatter:off */
		assertSupport(supportToTest).
			withResult(prepareSechubResultWithFindings(Severity.HIGH)).
			isCalculatedTo(TrafficLight.RED);
		/* @formatter:on */

    }

    @Test
    void a_sechub_result_containing_finding_medium__results_in_yellow() {
        /* @formatter:off */
		assertSupport(supportToTest).
			withResult(prepareSechubResultWithFindings(Severity.MEDIUM)).
			isCalculatedTo(TrafficLight.YELLOW);
		/* @formatter:on */
    }

    @Test
    void a_sechub_result_containing_finding_low__results_in_green() {
        /* @formatter:off */
		assertSupport(supportToTest).
			withResult(prepareSechubResultWithFindings(Severity.LOW)).
			isCalculatedTo(TrafficLight.GREEN);
		/* @formatter:on */
    }

    @Test
    void a_sechub_result_containing_finding_info__results_in_green() {
        /* @formatter:off */
		assertSupport(supportToTest).
			withResult(prepareSechubResultWithFindings(Severity.INFO)).
			isCalculatedTo(TrafficLight.GREEN);
		/* @formatter:on */
    }

    @Test
    void even_an_empty_sechub_results_returns_not_null_but_green() {
        /* @formatter:off */
		assertSupport(supportToTest).
			withResult(prepareSechubResultWithFindings()).
			isCalculatedTo(TrafficLight.GREEN);
		/* @formatter:on */
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Combined variants ........................+ */
    /* +-----------------------------------------------------------------------+ */
    @Test
    void a_sechub_result_containing_finding_info_low_info_results_in_green() {
        /* @formatter:off */
		assertSupport(supportToTest).
			withResult(prepareSechubResultWithFindings(Severity.INFO, Severity.LOW, Severity.INFO)).
			isCalculatedTo(TrafficLight.GREEN);
		/* @formatter:on */
    }

    @Test
    void a_sechub_result_containing_finding_info_low_info_medium_results_in_yellow() {
        /* @formatter:off */
		assertSupport(supportToTest).
			withResult(prepareSechubResultWithFindings(Severity.INFO, Severity.LOW, Severity.INFO, Severity.MEDIUM)).
			isCalculatedTo(TrafficLight.YELLOW);
		/* @formatter:on */
    }

    @Test
    void a_sechub_result_containing_finding_info_high_info_medium_results_in_red() {
        /* @formatter:off */
		assertSupport(supportToTest).
			withResult(prepareSechubResultWithFindings(Severity.INFO, Severity.HIGH, Severity.INFO, Severity.MEDIUM)).
			isCalculatedTo(TrafficLight.RED);
		/* @formatter:on */
    }

    @Test
    void a_sechub_result_containing_finding_info_critical_info_medium_results_in_red() {
        /* @formatter:off */
		assertSupport(supportToTest).
			withResult(prepareSechubResultWithFindings(Severity.INFO, Severity.CRITICAL, Severity.INFO, Severity.MEDIUM)).
			isCalculatedTo(TrafficLight.RED);
		/* @formatter:on */
    }
    /* +-----------------------------------------------------------------------+ */
    /* +............................ Helpers ..................................+ */
    /* +-----------------------------------------------------------------------+ */

    private SecHubReportModel prepareSechubResultWithFindings(Severity... severities) {
        List<SecHubFinding> findingList = new ArrayList<>();
        for (Severity severity : severities) {
            SecHubFinding finding = new SecHubFinding();
            finding.setSeverity(severity);
            findingList.add(finding);
        }
        SecHubReportModel model = new SecHubReportModel();
        model.getResult().getFindings().addAll(findingList);
        return model;
    }

    private class MultiFindingsTestSetup {
        private SecHubFinding findingCritical;
        private SecHubFinding findingHigh;
        private SecHubFinding findingLow;
        private SecHubFinding findingMedium;
        private SecHubFinding findingInfo;
        private SecHubFinding findingUnclassified;

        private SecHubReportModel reportTransformationResult;

        MultiFindingsTestSetup() {
            reportTransformationResult = new SecHubReportModel();

            findingCritical = createAndRegisterFinding(Severity.CRITICAL);
            findingHigh = createAndRegisterFinding(Severity.HIGH);
            findingMedium = createAndRegisterFinding(Severity.MEDIUM);
            findingLow = createAndRegisterFinding(Severity.LOW);
            findingInfo = createAndRegisterFinding(Severity.INFO);
            findingUnclassified = createAndRegisterFinding(Severity.UNCLASSIFIED);

        }

        private SecHubFinding createAndRegisterFinding(Severity severity) {
            List<SecHubFinding> findings = reportTransformationResult.getResult().getFindings();
            SecHubFinding find = new SecHubFinding();
            find.setSeverity(severity);
            findings.add(find);
            return find;
        }
    }

    private static class AssertTrafficLightSupport {

        private TrafficLightSupport calculator;
        private SecHubReportModel reportModel;

        private AssertTrafficLightSupport(TrafficLightSupport calculator) {
            this.calculator = calculator;
        }

        public AssertTrafficLightSupport isFilteringFindingsTo(TrafficLight wanted, SecHubFinding... findings) {
            List<SecHubFinding> filtered = calculator.filterFindingsFor(reportModel.getResult(), wanted);

            assertNotNull(filtered); // never null!
            for (SecHubFinding finding : findings) {
                assertTrue(filtered.contains(finding), "Finding missing:" + finding); // just same object. equals not custom implemented
            }
            assertEquals(findings.length, filtered.size());
            return this;

        }

        public AssertTrafficLightSupport isCalculatedTo(TrafficLight light) {
            TrafficLight calcLight = calculator.calculateTrafficLight(reportModel.getResult());

            assertNotNull(calcLight); // never null!
            assertEquals(light, calcLight, "calculated light not as expected!");

            return this;

        }

        public AssertTrafficLightSupport withResult(SecHubReportModel result) {
            this.reportModel = result;
            return this;
        }

    }

    private AssertTrafficLightSupport assertSupport(TrafficLightSupport calculator) {
        assertNotNull(calculator);
        return new AssertTrafficLightSupport(calculator);
    }
}
