// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.adapter.AdapterOptionKey;
import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.domain.scan.project.ScanMockData;
import com.daimler.sechub.domain.scan.project.ScanProjectMockDataConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

public class SecHubAdapterOptionsBuilderStrategyTest {

	private SecHubExecutionContext context;
	@SuppressWarnings("rawtypes")
	private AbstractAdapterConfigBuilder configBuilder;
	private ScanProjectMockDataConfiguration scanProjectMockDataConfig;

	@Before
	public void before() {
		context = mock(SecHubExecutionContext.class);
		configBuilder = mock(AbstractAdapterConfigBuilder.class);

		scanProjectMockDataConfig = new ScanProjectMockDataConfiguration();
		ScanMockData codeScan = new ScanMockData(TrafficLight.RED);
		ScanMockData webScan = new ScanMockData(TrafficLight.YELLOW);
		ScanMockData infraScan = new ScanMockData(TrafficLight.GREEN);

		scanProjectMockDataConfig.setCodeScan(codeScan);
		scanProjectMockDataConfig.setWebScan(webScan);
		scanProjectMockDataConfig.setInfraScan(infraScan);

		when(context.getData(ScanKey.PROJECT_MOCKDATA_CONFIGURATION)).thenReturn(scanProjectMockDataConfig);
	}
	/* --------------------------------- */
	/* --------- when defined ---------- */
	/* --------------------------------- */

	@SuppressWarnings("unchecked")
	@Test
	public void strategy_will_set_dediccated_mock_config_result_for_scantype_codescan() {
		/* prepare */
		SecHubAdapterOptionsBuilderStrategy strategyToTest = new SecHubAdapterOptionsBuilderStrategy(context, ScanType.CODE_SCAN);

		/* execute */
		strategyToTest.configure(configBuilder);

		/* test */
		verify(configBuilder).setOption(AdapterOptionKey.MOCK_CONFIGURATION_RESULT, "red");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void strategy_will_set_dediccated_mock_config_result_for_scantype_webscan() {
		/* prepare */
		SecHubAdapterOptionsBuilderStrategy strategyToTest = new SecHubAdapterOptionsBuilderStrategy(context, ScanType.WEB_SCAN);

		/* execute */
		strategyToTest.configure(configBuilder);

		/* test */
		verify(configBuilder).setOption(AdapterOptionKey.MOCK_CONFIGURATION_RESULT, "yellow");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void strategy_will_set_dediccated_mock_config_result_for_scantype_infrascan() {
		/* prepare */
		SecHubAdapterOptionsBuilderStrategy strategyToTest = new SecHubAdapterOptionsBuilderStrategy(context, ScanType.INFRA_SCAN);

		/* execute */
		strategyToTest.configure(configBuilder);

		/* test */
		verify(configBuilder).setOption(AdapterOptionKey.MOCK_CONFIGURATION_RESULT, "green");
	}
	
	/* --------------------------------- */
	/* ------when not defined ---------- */
	/* --------------------------------- */

	@SuppressWarnings("unchecked")
	@Test
	public void strategy_will_set_no_mock_config_result_for_type_code_scan_when_not_defined() {
		/* prepare */
		SecHubAdapterOptionsBuilderStrategy strategyToTest = new SecHubAdapterOptionsBuilderStrategy(context, ScanType.CODE_SCAN);
		scanProjectMockDataConfig.setCodeScan(null);
		/* execute */
		strategyToTest.configure(configBuilder);

		/* test */
		verify(configBuilder, never()).setOption(eq(AdapterOptionKey.MOCK_CONFIGURATION_RESULT), any());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void strategy_will_set_no_mock_config_result_for_type_web_scan_when_not_defined() {
		/* prepare */
		SecHubAdapterOptionsBuilderStrategy strategyToTest = new SecHubAdapterOptionsBuilderStrategy(context, ScanType.WEB_SCAN);
		scanProjectMockDataConfig.setWebScan(null);
		/* execute */
		strategyToTest.configure(configBuilder);

		/* test */
		verify(configBuilder, never()).setOption(eq(AdapterOptionKey.MOCK_CONFIGURATION_RESULT), any());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void strategy_will_set_no_mock_config_result_for_type_infra_scan_when_not_defined() {
		/* prepare */
		SecHubAdapterOptionsBuilderStrategy strategyToTest = new SecHubAdapterOptionsBuilderStrategy(context, ScanType.INFRA_SCAN);
		scanProjectMockDataConfig.setInfraScan(null);
		/* execute */
		strategyToTest.configure(configBuilder);

		/* test */
		verify(configBuilder, never()).setOption(eq(AdapterOptionKey.MOCK_CONFIGURATION_RESULT), any());
	}

}
