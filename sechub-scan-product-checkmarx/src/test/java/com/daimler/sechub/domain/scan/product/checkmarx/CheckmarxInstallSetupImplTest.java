// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.scan.config.NamePatternIdprovider;
import com.daimler.sechub.domain.scan.config.ScanConfigService;
import com.daimler.sechub.sharedkernel.mapping.MappingIdentifier;

public class CheckmarxInstallSetupImplTest {

    private CheckmarxInstallSetupImpl setupImplToTest;
    private ScanConfigService scanConfigService;

    @Before
    public void before() {
        setupImplToTest = new CheckmarxInstallSetupImpl();
        setupImplToTest.teamIdForNewProjects = "A0";
        scanConfigService = mock(ScanConfigService.class);
        setupImplToTest.scanConfigService = scanConfigService;
    }

    @Test
    public void teamId_found_name_by_provider_returns_team_id_from_provider() {
        /* prepare */
        NamePatternIdprovider provider = mock(NamePatternIdprovider.class);
        when(provider.getIdForName("abc")).thenReturn("A1");

        when(scanConfigService.getNamePatternIdProvider(MappingIdentifier.CHECKMARX_NEWPROJECT_TEAM_ID)).thenReturn(provider);

        /* execute */
        String result = setupImplToTest.getTeamIdForNewProjects("abc");
        /* test */
        assertEquals("A1", result);
    }

    @Test
    public void teamId_not_found_name_by_provider_returns_default_teamId() {
        /* prepare */
        NamePatternIdprovider provider = mock(NamePatternIdprovider.class);
        when(provider.getIdForName("abc")).thenReturn(null);

        when(scanConfigService.getNamePatternIdProvider(MappingIdentifier.CHECKMARX_NEWPROJECT_TEAM_ID)).thenReturn(provider);

        /* execute */
        String result = setupImplToTest.getTeamIdForNewProjects("abc");
        /* test */
        assertEquals("A0", result);
    }

    @Test
    public void presetId_found_name_by_provider_returns_preset_id_from_provider() {
        /* prepare */
        NamePatternIdprovider provider = mock(NamePatternIdprovider.class);
        when(provider.getIdForName("abc")).thenReturn("1234");

        when(scanConfigService.getNamePatternIdProvider(MappingIdentifier.CHECKMARX_NEWPROJECT_PRESET_ID)).thenReturn(provider);

        /* execute */
        Long result = setupImplToTest.getPresetIdForNewProjects("abc");
        /* test */
        assertEquals(Long.valueOf(1234), result);
    }

    @Test
    public void presetId_not_found_name_by_provider_returns_null() {
        /* prepare */
        NamePatternIdprovider provider = mock(NamePatternIdprovider.class);
        when(provider.getIdForName("abc")).thenReturn(null);

        when(scanConfigService.getNamePatternIdProvider(MappingIdentifier.CHECKMARX_NEWPROJECT_PRESET_ID)).thenReturn(provider);

        /* execute */
        Long result = setupImplToTest.getPresetIdForNewProjects("abc");
        /* test */
        assertEquals(null, result);
    }

}
