// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.sereco;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.domain.scan.project.FalsePositiveEntry;
import com.daimler.sechub.domain.scan.project.FalsePositiveMetaData;
import com.daimler.sechub.domain.scan.project.FalsePositiveProjectConfiguration;
import com.daimler.sechub.domain.scan.project.ScanProjectConfig;
import com.daimler.sechub.domain.scan.project.ScanProjectConfigID;
import com.daimler.sechub.domain.scan.project.ScanProjectConfigService;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

public class SerecoFalsePositiveMarkerTest {

    private static final String PROJECT_ID = "project1";
    private SerecoFalsePositiveMarker markerToTest;
    private ScanProjectConfigService scanProjectConfigService;
    private ScanProjectConfig config;
    private SerecoFalsePositiveFinder falsePositiveFinder;
    private FalsePositiveProjectConfiguration projectConfig;

    @Before
    public void before() throws Exception {
        markerToTest = new SerecoFalsePositiveMarker();

        scanProjectConfigService = mock(ScanProjectConfigService.class);
        falsePositiveFinder = mock(SerecoFalsePositiveFinder.class);

        config = new ScanProjectConfig(ScanProjectConfigID.FALSE_POSITIVE_CONFIGURATION, PROJECT_ID);
        when(scanProjectConfigService.get(PROJECT_ID, ScanProjectConfigID.FALSE_POSITIVE_CONFIGURATION, false)).thenReturn(config);
        markerToTest.scanProjectConfigService = scanProjectConfigService;
        markerToTest.falsePositiveFinder = falsePositiveFinder;

        projectConfig = new FalsePositiveProjectConfiguration();
    }

    @Test
    public void a_webscan_triggers_falsePositiveFinder_for_fp_setting_for_webscan() {

        /* prepare */
        FalsePositiveMetaData metaData = addEntryAndReturnMetaData(projectConfig, ScanType.WEB_SCAN);
        config.setData(projectConfig.toJSON());

        List<SerecoVulnerability> all = new ArrayList<>();
        SerecoVulnerability v1 = addVulnerability(all, ScanType.WEB_SCAN);

        /* execute */
        markerToTest.markFalsePositives(PROJECT_ID, all);

        /* test */
        verify(falsePositiveFinder).isFound(v1, metaData);
    }

    @Test
    public void a_webscan_triggers_NOT_falsePositiveFinder_for_fp_setting_for_codescan() {

        /* prepare */
        FalsePositiveMetaData metaData = addEntryAndReturnMetaData(projectConfig, ScanType.CODE_SCAN);
        config.setData(projectConfig.toJSON());

        List<SerecoVulnerability> all = new ArrayList<>();
        SerecoVulnerability v1 = addVulnerability(all, ScanType.WEB_SCAN);

        /* execute */
        markerToTest.markFalsePositives(PROJECT_ID, all);

        /* test */
        verify(falsePositiveFinder, never()).isFound(v1, metaData);
    }

    @Test
    public void a_codescan_triggers_falsePositiveFinder_for_fp_setting_for_codescan() {

        /* prepare */
        FalsePositiveMetaData metaData = addEntryAndReturnMetaData(projectConfig, ScanType.CODE_SCAN);
        config.setData(projectConfig.toJSON());

        List<SerecoVulnerability> all = new ArrayList<>();
        SerecoVulnerability v1 = addVulnerability(all, ScanType.CODE_SCAN);

        /* execute */
        markerToTest.markFalsePositives(PROJECT_ID, all);

        /* test */
        verify(falsePositiveFinder).isFound(v1, metaData);
    }

    @Test
    public void a_codescan_triggers_NOT_falsePositiveFinder_for_fp_setting_for_webscan() {

        /* prepare */
        FalsePositiveMetaData metaData = addEntryAndReturnMetaData(projectConfig, ScanType.WEB_SCAN);
        config.setData(projectConfig.toJSON());

        List<SerecoVulnerability> all = new ArrayList<>();
        SerecoVulnerability v1 = addVulnerability(all, ScanType.CODE_SCAN);

        /* execute */
        markerToTest.markFalsePositives(PROJECT_ID, all);

        /* test */
        verify(falsePositiveFinder, never()).isFound(v1, metaData);
    }

    private SerecoVulnerability addVulnerability(List<SerecoVulnerability> all, ScanType scanType) {
        SerecoVulnerability v1 = new SerecoVulnerability();
        v1.setScanType(scanType);
        all.add(v1);
        return v1;
    }

    private FalsePositiveMetaData addEntryAndReturnMetaData(FalsePositiveProjectConfiguration projectConfig, ScanType scanType) {
        List<FalsePositiveEntry> fp = projectConfig.getFalsePositives();
        FalsePositiveEntry e = new FalsePositiveEntry();
        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setScanType(scanType);
        e.setMetaData(metaData);
        fp.add(e);
        return metaData;
    }

}
