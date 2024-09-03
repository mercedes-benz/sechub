// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveEntry;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveMetaData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectConfiguration;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectData;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigID;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigService;
import com.mercedesbenz.sechub.domain.scan.project.WebscanFalsePositiveProjectData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

public class SerecoFalsePositiveMarkerTest {

    private static final String PROJECT_ID = "project1";
    private SerecoFalsePositiveMarker markerToTest;
    private ScanProjectConfig config;
    private FalsePositiveProjectConfiguration projectConfig;

    private static final ScanProjectConfigService scanProjectConfigService = mock();
    private static final SerecoJobDataFalsePositiveFinder jobDataFalsePositiveFinder = mock();

    private static final SerecoProjectDataFalsePositiveFinder projectDataFalsePositiveFinder = mock();
    private static final SerecoProjectDataPatternMapFactory projectDataPatternMapFactory = mock();

    @BeforeEach
    void beforeEach() throws Exception {
        /* @formatter:off */
        Mockito.reset(jobDataFalsePositiveFinder,
                projectDataFalsePositiveFinder,
                scanProjectConfigService,
                projectDataPatternMapFactory);

        markerToTest = new SerecoFalsePositiveMarker(jobDataFalsePositiveFinder,
                projectDataFalsePositiveFinder,
                scanProjectConfigService,
                projectDataPatternMapFactory);
        /* @formatter:on */

        config = new ScanProjectConfig(ScanProjectConfigID.FALSE_POSITIVE_CONFIGURATION, PROJECT_ID);
        when(scanProjectConfigService.get(PROJECT_ID, ScanProjectConfigID.FALSE_POSITIVE_CONFIGURATION, false)).thenReturn(config);

        projectConfig = new FalsePositiveProjectConfiguration();
    }

    @Test
    void a_webscan_triggers_falsePositiveFinder_for_fp_setting_for_webscan() {

        /* prepare */
        FalsePositiveMetaData metaData = addEntryAndReturnMetaData(projectConfig, ScanType.WEB_SCAN);
        config.setData(projectConfig.toJSON());

        List<SerecoVulnerability> all = new ArrayList<>();
        SerecoVulnerability v1 = addVulnerability(all, ScanType.WEB_SCAN);

        /* execute */
        markerToTest.markFalsePositives(PROJECT_ID, all);

        /* test */
        verify(jobDataFalsePositiveFinder).isFound(v1, metaData);
        verify(projectDataFalsePositiveFinder, never()).isFound(any(), any(), any());
    }

    @Test
    void a_webscan_triggers_NOT_falsePositiveFinder_for_fp_setting_for_codescan() {

        /* prepare */
        FalsePositiveMetaData metaData = addEntryAndReturnMetaData(projectConfig, ScanType.CODE_SCAN);
        config.setData(projectConfig.toJSON());

        List<SerecoVulnerability> all = new ArrayList<>();
        SerecoVulnerability v1 = addVulnerability(all, ScanType.WEB_SCAN);

        /* execute */
        markerToTest.markFalsePositives(PROJECT_ID, all);

        /* test */
        verify(jobDataFalsePositiveFinder, never()).isFound(v1, metaData);
        verify(projectDataFalsePositiveFinder, never()).isFound(any(), any(), any());
    }

    @Test
    void a_codescan_triggers_falsePositiveFinder_for_fp_setting_for_codescan() {

        /* prepare */
        FalsePositiveMetaData metaData = addEntryAndReturnMetaData(projectConfig, ScanType.CODE_SCAN);
        config.setData(projectConfig.toJSON());

        List<SerecoVulnerability> all = new ArrayList<>();
        SerecoVulnerability v1 = addVulnerability(all, ScanType.CODE_SCAN);

        /* execute */
        markerToTest.markFalsePositives(PROJECT_ID, all);

        /* test */
        verify(jobDataFalsePositiveFinder).isFound(v1, metaData);
        verify(projectDataFalsePositiveFinder, never()).isFound(any(), any(), any());
    }

    @Test
    void a_codescan_triggers_NOT_falsePositiveFinder_for_fp_setting_for_webscan() {

        /* prepare */
        FalsePositiveMetaData metaData = addEntryAndReturnMetaData(projectConfig, ScanType.WEB_SCAN);
        config.setData(projectConfig.toJSON());

        List<SerecoVulnerability> all = new ArrayList<>();
        SerecoVulnerability v1 = addVulnerability(all, ScanType.CODE_SCAN);

        /* execute */
        markerToTest.markFalsePositives(PROJECT_ID, all);

        /* test */
        verify(jobDataFalsePositiveFinder, never()).isFound(v1, metaData);
        verify(projectDataFalsePositiveFinder, never()).isFound(any(), any(), any());
    }

    @Test
    void a_webscan_triggers_projectDataFalsePositiveFinder_when_projectData_with_webscan_available() {
        /* prepare */
        FalsePositiveProjectData projectData = addEntryProjectDataWithWebscanAndReturnProjectData(projectConfig);
        config.setData(projectConfig.toJSON());

        List<SerecoVulnerability> all = new ArrayList<>();
        SerecoVulnerability v1 = addVulnerability(all, ScanType.WEB_SCAN);

        @SuppressWarnings("unchecked")
        Map<String, Pattern> mockedMap = mock(Map.class);
        when(projectDataPatternMapFactory.create(projectConfig.getFalsePositives())).thenReturn(mockedMap);

        /* execute */
        markerToTest.markFalsePositives(PROJECT_ID, all);

        /* test */
        verify(projectDataFalsePositiveFinder).isFound(v1, projectData, mockedMap);
        verify(jobDataFalsePositiveFinder, never()).isFound(any(), any());
    }

    @Test
    void a_webscan_triggers_projectDataFalsePositiveFinder_when_projectData_without_webscan_available() {
        /* prepare */
        FalsePositiveProjectData projectData = addEntryProjectDataWithoutWebscanAndReturnProjectData(projectConfig);
        config.setData(projectConfig.toJSON());

        List<SerecoVulnerability> all = new ArrayList<>();
        SerecoVulnerability v1 = addVulnerability(all, ScanType.WEB_SCAN);

        @SuppressWarnings("unchecked")
        Map<String, Pattern> mockedMap = mock(Map.class);
        when(projectDataPatternMapFactory.create(projectConfig.getFalsePositives())).thenReturn(mockedMap);

        /* execute */
        markerToTest.markFalsePositives(PROJECT_ID, all);

        /* test */
        verify(projectDataFalsePositiveFinder).isFound(v1, projectData, mockedMap);
        verify(jobDataFalsePositiveFinder, never()).isFound(any(), any());
    }

    private SerecoVulnerability addVulnerability(List<SerecoVulnerability> all, ScanType scanType) {
        SerecoVulnerability v1 = new SerecoVulnerability();
        v1.setScanType(scanType);
        all.add(v1);
        return v1;
    }

    private FalsePositiveProjectData addEntryProjectDataWithWebscanAndReturnProjectData(FalsePositiveProjectConfiguration projectConfig) {
        WebscanFalsePositiveProjectData webscan = new WebscanFalsePositiveProjectData();
        webscan.setUrlPattern("https://myapp-*.example.com:80*/rest/*/search?*");

        FalsePositiveProjectData projectData = new FalsePositiveProjectData();
        projectData.setWebScan(webscan);

        FalsePositiveEntry entry = new FalsePositiveEntry();
        entry.setProjectData(projectData);

        List<FalsePositiveEntry> fp = projectConfig.getFalsePositives();
        fp.add(entry);
        return projectData;
    }

    private FalsePositiveProjectData addEntryProjectDataWithoutWebscanAndReturnProjectData(FalsePositiveProjectConfiguration projectConfig) {
        FalsePositiveProjectData projectData = new FalsePositiveProjectData();
        FalsePositiveEntry entry = new FalsePositiveEntry();
        entry.setProjectData(projectData);

        List<FalsePositiveEntry> fp = projectConfig.getFalsePositives();
        fp.add(entry);
        return projectData;
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
