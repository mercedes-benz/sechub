package com.daimler.sechub.domain.scan.product.checkmarx;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import com.daimler.sechub.domain.scan.config.NamePatternIdprovider;
import com.daimler.sechub.domain.scan.config.ScanConfigService;

public class CheckmarxInstallSetupImplTest {

	private CheckmarxInstallSetupImpl setupImplToTest;
	private ScanConfigService scanConfigService;

	@Before
	public void before() {
		setupImplToTest = new CheckmarxInstallSetupImpl();
		setupImplToTest.teamIdForNewProjects="A0";
		scanConfigService = mock(ScanConfigService.class);
		setupImplToTest.scanConfigService = scanConfigService;
	}

	@Test
	public void teamId_found_name_by_provider_returns_team_id_from_provider() {
		/* prepare */
		NamePatternIdprovider provider = mock(NamePatternIdprovider.class);
		when(provider.getIdForName("abc")).thenReturn("A1");

		when(scanConfigService.getNamePatternIdProvider("checkmarx.newproject.teamid")).thenReturn(provider);

		/* execute */
		String result = setupImplToTest.getTeamIdForNewProjects("abc");
		/* test */
		assertEquals("A1",result);
	}

	@Test
	public void teamId_not_found_name_by_provider_returns_default_teamId() {
		/* prepare */
		NamePatternIdprovider provider = mock(NamePatternIdprovider.class);
		when(provider.getIdForName("abc")).thenReturn(null);

		when(scanConfigService.getNamePatternIdProvider("checkmarx.newproject.teamid")).thenReturn(provider);

		/* execute */
		String result = setupImplToTest.getTeamIdForNewProjects("abc");
		/* test */
		assertEquals("A0",result);
	}

	@Test
	public void presetId_found_name_by_provider_returns_preset_id_from_provider() {
		/* prepare */
		NamePatternIdprovider provider = mock(NamePatternIdprovider.class);
		when(provider.getIdForName("abc")).thenReturn("B1");

		when(scanConfigService.getNamePatternIdProvider("checkmarx.newproject.presetid")).thenReturn(provider);

		/* execute */
		String result = setupImplToTest.getPresetIdForNewProjects("abc");
		/* test */
		assertEquals("B1",result);
	}

	@Test
	public void presetId_not_found_name_by_provider_returns_null() {
		/* prepare */
		NamePatternIdprovider provider = mock(NamePatternIdprovider.class);
		when(provider.getIdForName("abc")).thenReturn(null);

		when(scanConfigService.getNamePatternIdProvider("checkmarx.newproject.presetid")).thenReturn(provider);

		/* execute */
		String result = setupImplToTest.getPresetIdForNewProjects("abc");
		/* test */
		assertEquals(null,result);
	}

}
