// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.daimler.sechub.domain.scan.resolve.TargetResolver;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.metadata.DefaultMetaDataInspector;
import com.daimler.sechub.sharedkernel.storage.StorageService;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {CheckmarxProductExecutor.class,CheckmarxResilienceConsultant.class,
		CheckmarxProductExecutorMockTest.SimpleTestConfiguration.class, DefaultMetaDataInspector.class })
public class CheckmarxProductExecutorMockTest {

	@Autowired
	CheckmarxProductExecutor executorToTest;

	@MockBean
	CheckmarxAdapter checkmarxAdapter;

	@MockBean
	CheckmarxInstallSetup installSetup;

	@MockBean
	StorageService storageService;

	@MockBean
	TargetResolver targetResolver;

	@Before
	public void before() {
	}

	@Test
	public void action_executor_contains_checkmarx_resilience_consultant_after_postConstruct() {
		assertTrue(executorToTest.resilientActionExecutor.containsConsultant(CheckmarxResilienceConsultant.class));
	}


	@TestConfiguration
	@Profile(Profiles.TEST)
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

	}

}
