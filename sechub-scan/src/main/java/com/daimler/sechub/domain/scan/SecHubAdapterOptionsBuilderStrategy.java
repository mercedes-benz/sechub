// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.adapter.AdapterConfig;
import com.daimler.sechub.adapter.AdapterConfigurationStrategy;
import com.daimler.sechub.adapter.AdapterOptionKey;
import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.domain.scan.project.ScanMockData;
import com.daimler.sechub.domain.scan.project.ScanProjectMockDataConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

/**
 * A common strategy to provide SecHub options to adapters <br>
 * Using this strategy will reduce boilerplate code in web scan executors. <br>
 * <br>
 * <b>Content:</b>
 * <ul>
 * <li>mock configuration result as lower cased value from traffic light
 * (green|yellow|red)</li>
 * </ul>
 *
 * @author Albert Tregnaghi
 *
 * @param <B> builder
 * @param <C> configuration
 */
public class SecHubAdapterOptionsBuilderStrategy implements AdapterConfigurationStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(SecHubAdapterOptionsBuilderStrategy.class);

	private SecHubExecutionContext context;
	private ScanType scanType;

	public SecHubAdapterOptionsBuilderStrategy(SecHubExecutionContext context, ScanType scanType) {
		this.context = context;
		this.scanType = scanType;
	}

	@Override
	public <B extends AbstractAdapterConfigBuilder<B, C>, C extends AdapterConfig> void configure(B configBuilder) {
		/* @formatter:off */
		String mockDataResultLowerCased = fetchMockConfigurationResultLowerCased();
		if (mockDataResultLowerCased!=null) {
		configBuilder.
		 	setOption(AdapterOptionKey.MOCK_CONFIGURATION_RESULT, mockDataResultLowerCased)
		 	;
		}
		/* @formatter:on */

	}

	private String fetchMockConfigurationResultLowerCased() {
		String mockConfigurationResult = null;
		ScanProjectMockDataConfiguration mockConfiguration = context.getData(ScanKey.PROJECT_MOCKDATA_CONFIGURATION);
		Optional<ScanMockData> mockData = Optional.empty();
		if (mockConfiguration != null) {
			switch (scanType) {
			case CODE_SCAN:
				mockData = mockConfiguration.getCodeScan();
				break;
			case WEB_SCAN:
				mockData = mockConfiguration.getWebScan();
				break;
			case INFRA_SCAN:
				mockData = mockConfiguration.getInfraScan();
				break;
			default:
				LOG.error("Cannot fetch mock configuration result for scan type:'{}'", scanType);
			}
			if (mockData.isPresent()) {
				ScanMockData scanMockData = mockData.get();
				mockConfigurationResult = scanMockData.getResult().name().toLowerCase();
			}
		}
		return mockConfigurationResult;
	}

}
