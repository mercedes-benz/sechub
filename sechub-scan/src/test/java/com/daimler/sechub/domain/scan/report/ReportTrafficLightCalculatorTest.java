// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.report;

import static com.daimler.sechub.domain.scan.report.AssertCalculation.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.commons.model.SecHubFinding;
import com.daimler.sechub.commons.model.SecHubResult;
import com.daimler.sechub.commons.model.Severity;
import com.daimler.sechub.commons.model.TrafficLight;

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
public class ReportTrafficLightCalculatorTest {

	private ScanReportTrafficLightCalculator calculatorToTest;

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Before
	public void before() {
		calculatorToTest = new ScanReportTrafficLightCalculator();
	}
	/* +-----------------------------------------------------------------------+ */
	/* +............................ filter test     ..........................+ */
	/* +-----------------------------------------------------------------------+ */

	@Test
	public void having_critical_findings_filtering_to_green_returns_only_mpty() {
		assertCalculator(calculatorToTest).
			withResult(prepareSechubResultWithFindings(Severity.CRITICAL)).
			isFilteringFindingsTo(TrafficLight.GREEN);
	}
	
	@Test
	public void having_critical_findings_filtering_to_yellow_returns_only_mpty() {
		assertCalculator(calculatorToTest).
			withResult(prepareSechubResultWithFindings(Severity.CRITICAL)).
			isFilteringFindingsTo(TrafficLight.GREEN);
	}
	
	@Test
	public void having_setup_findings_filtering_to_red_returns_critical_and_high() {
		/* prepare */
		MultiFindingsTestSetup setup = new MultiFindingsTestSetup();
		
		/* test*/
		assertCalculator(calculatorToTest).
			withResult(setup.result).
			isFilteringFindingsTo(TrafficLight.RED,setup.findingCritical,setup.findingHigh);
	}
	
	@Test
	public void having_setup_findings_filtering_to_yellow_returns_medium_only() {
		/* prepare */
		MultiFindingsTestSetup setup = new MultiFindingsTestSetup();
		
		/* test*/
		assertCalculator(calculatorToTest).
			withResult(setup.result).
			isFilteringFindingsTo(TrafficLight.YELLOW,setup.findingMedium);
	}
	
	@Test
	public void having_setup_findings_filtering_to_green_returns_low_unclassfied_and_info() {
		/* prepare */
		MultiFindingsTestSetup setup = new MultiFindingsTestSetup();
		
		/* test*/
		assertCalculator(calculatorToTest).
			withResult(setup.result).
			isFilteringFindingsTo(TrafficLight.GREEN,setup.findingLow, setup.findingInfo, setup.findingUnclassified);
	}
	
	/* +-----------------------------------------------------------------------+ */
	/* +............................ Single variants ..........................+ */
	/* +-----------------------------------------------------------------------+ */
	@Test
	public void calculatore_called_with_null_returns_() {
		/* prepare for test */
		expected.expect(IllegalArgumentException.class);

		/* execute */
		calculatorToTest.calculateTrafficLight(null);
	}

	@Test
	public void a_sechub_result_containing_finding_critical__results_in_red() {
		/* @formatter:off */
		assertCalculator(calculatorToTest).
			withResult(prepareSechubResultWithFindings(Severity.CRITICAL)).
			isCalculatedTo(TrafficLight.RED);
		/* @formatter:on */
	}

	@Test
	public void a_sechub_result_containing_finding_high__results_in_red() {
		/* @formatter:off */
		assertCalculator(calculatorToTest).
			withResult(prepareSechubResultWithFindings(Severity.HIGH)).
			isCalculatedTo(TrafficLight.RED);
		/* @formatter:on */

	}

	@Test
	public void a_sechub_result_containing_finding_medium__results_in_yellow() {
		/* @formatter:off */
		assertCalculator(calculatorToTest).
			withResult(prepareSechubResultWithFindings(Severity.MEDIUM)).
			isCalculatedTo(TrafficLight.YELLOW);
		/* @formatter:on */
	}

	@Test
	public void a_sechub_result_containing_finding_low__results_in_green() {
		/* @formatter:off */
		assertCalculator(calculatorToTest).
			withResult(prepareSechubResultWithFindings(Severity.LOW)).
			isCalculatedTo(TrafficLight.GREEN);
		/* @formatter:on */
	}

	@Test
	public void a_sechub_result_containing_finding_info__results_in_green() {
		/* @formatter:off */
		assertCalculator(calculatorToTest).
			withResult(prepareSechubResultWithFindings(Severity.INFO)).
			isCalculatedTo(TrafficLight.GREEN);
		/* @formatter:on */
	}

	@Test
	public void even_an_empty_sechub_results_returns_not_null_but_green() {
		/* @formatter:off */
		assertCalculator(calculatorToTest).
			withResult(prepareSechubResultWithFindings()).
			isCalculatedTo(TrafficLight.GREEN);
		/* @formatter:on */
	}

	/* +-----------------------------------------------------------------------+ */
	/* +............................ Combined variants ........................+ */
	/* +-----------------------------------------------------------------------+ */
	@Test
	public void a_sechub_result_containing_finding_info_low_info_results_in_green() {
		/* @formatter:off */
		assertCalculator(calculatorToTest).
			withResult(prepareSechubResultWithFindings(Severity.INFO, Severity.LOW, Severity.INFO)).
			isCalculatedTo(TrafficLight.GREEN);
		/* @formatter:on */
	}

	@Test
	public void a_sechub_result_containing_finding_info_low_info_medium_results_in_yellow() {
		/* @formatter:off */
		assertCalculator(calculatorToTest).
			withResult(prepareSechubResultWithFindings(Severity.INFO, Severity.LOW, Severity.INFO, Severity.MEDIUM)).
			isCalculatedTo(TrafficLight.YELLOW);
		/* @formatter:on */
	}

	@Test
	public void a_sechub_result_containing_finding_info_high_info_medium_results_in_red() {
		/* @formatter:off */
		assertCalculator(calculatorToTest).
			withResult(prepareSechubResultWithFindings(Severity.INFO, Severity.HIGH, Severity.INFO, Severity.MEDIUM)).
			isCalculatedTo(TrafficLight.RED);
		/* @formatter:on */
	}

	@Test
	public void a_sechub_result_containing_finding_info_critical_info_medium_results_in_red() {
		/* @formatter:off */
		assertCalculator(calculatorToTest).
			withResult(prepareSechubResultWithFindings(Severity.INFO, Severity.CRITICAL, Severity.INFO, Severity.MEDIUM)).
			isCalculatedTo(TrafficLight.RED);
		/* @formatter:on */
	}
	/* +-----------------------------------------------------------------------+ */
	/* +............................ Helpers ..................................+ */
	/* +-----------------------------------------------------------------------+ */

	private SecHubResult prepareSechubResultWithFindings(Severity... severities) {
		SecHubResult secHubResult = mock(SecHubResult.class);
		List<SecHubFinding> findingList = new ArrayList<>();
		for (Severity severity : severities) {
			SecHubFinding finding = new SecHubFinding();
			finding.setSeverity(severity);
			findingList.add(finding);
		}
		when(secHubResult.getFindings()).thenReturn(findingList);
		return secHubResult;
	}

	private class MultiFindingsTestSetup{
		private SecHubFinding findingCritical;
		private SecHubFinding findingHigh;
		private SecHubFinding findingLow;
		private SecHubFinding findingMedium;
		private SecHubFinding findingInfo;
		private SecHubFinding findingUnclassified;

		private SecHubResult result;
	
		MultiFindingsTestSetup(){
			result = new SecHubResult();
		
			findingCritical = createAndRegisterFinding(Severity.CRITICAL);
			findingHigh = createAndRegisterFinding(Severity.HIGH);
			findingMedium = createAndRegisterFinding(Severity.MEDIUM);
			findingLow = createAndRegisterFinding(Severity.LOW);
			findingInfo = createAndRegisterFinding(Severity.INFO);
			findingUnclassified= createAndRegisterFinding(Severity.UNCLASSIFIED);
			
		}
		
		private SecHubFinding createAndRegisterFinding(Severity severity) {
			List<SecHubFinding> findings = result.getFindings();
			SecHubFinding find = new SecHubFinding();
			find.setSeverity(severity);
			findings.add(find);
			return find;
		}
	}
}
