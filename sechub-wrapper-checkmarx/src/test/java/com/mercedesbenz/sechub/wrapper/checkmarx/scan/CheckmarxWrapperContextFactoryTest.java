package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConstants;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProvider;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProviderFactory;
import com.mercedesbenz.sechub.commons.model.CodeScanPathCollector;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperCLIEnvironment;

class CheckmarxWrapperContextFactoryTest {

    private static final String PROJECT1 = "project1";
    private CheckmarxWrapperContextFactory factoryToTest;
    private CheckmarxWrapperCLIEnvironment environment;
    private NamePatternIdProviderFactory providerFactory;
    private ArchiveSupport archiveSupport;
    private NamePatternIdProvider presetIdProvider;
    private NamePatternIdProvider teamIdProvider;
    private CodeScanPathCollector codeScanPathCollector;

    @BeforeEach
    void beforeEach() {
        providerFactory = mock(NamePatternIdProviderFactory.class);
        environment = mock(CheckmarxWrapperCLIEnvironment.class);

        presetIdProvider = mock(NamePatternIdProvider.class);
        teamIdProvider = mock(NamePatternIdProvider.class);
        archiveSupport = mock(ArchiveSupport.class);
        codeScanPathCollector = mock(CodeScanPathCollector.class);

        factoryToTest = new CheckmarxWrapperContextFactory();

        factoryToTest.providerFactory = providerFactory;
        factoryToTest.archiveSupport = archiveSupport;
        factoryToTest.codeScanPathCollector = codeScanPathCollector;

        when(providerFactory.createProvider(eq(CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_PRESET_ID), any())).thenReturn(presetIdProvider);
        when(providerFactory.createProvider(eq(CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_TEAM_ID), any())).thenReturn(teamIdProvider);

    }

    @Test
    void created_context_has_fields() {
        /* prepare */
        String secHubModelJson = createValidSecHubModel();

        when(environment.getSechubConfigurationModelAsJson()).thenReturn(secHubModelJson);

        /* execute */
        CheckmarxWrapperContext result = factoryToTest.create(environment);

        /* test */
        assertNotNull(result.configuration);
        assertNotNull(result.environment);
        assertNotNull(result.presetIdProvider);
        assertNotNull(result.teamIdProvider);
        assertNotNull(result.archiveSupport);
        assertNotNull(result.codeScanPathCollector);

        assertEquals(teamIdProvider, result.teamIdProvider);
        assertEquals(presetIdProvider, result.presetIdProvider);
        assertEquals(codeScanPathCollector, result.codeScanPathCollector);
        assertEquals(archiveSupport, result.archiveSupport);
        assertEquals(environment, result.environment);
    }

    @Test
    void when_no_sechub_model_json_exists_an_illegal_state_exception_is_thrown() {
        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factoryToTest.create(environment));

        /* test */
        assertTrue(exception.getMessage().contains("No SecHub model"));
    }

    @Test
    void sechub_model_exists_created_context_is_not_null() {
        String secHubModelJson = createValidSecHubModel();

        when(environment.getSechubConfigurationModelAsJson()).thenReturn(secHubModelJson);

        /* execute */
        CheckmarxWrapperContext result = factoryToTest.create(environment);

        /* test */
        assertNotNull(result);
    }

    @Test
    void sechub_model_exists_created_context_contains_team_and_preseit_ids() {
        /* prepare */
        String secHubModelJson = createValidSecHubModel();

        when(environment.getSechubConfigurationModelAsJson()).thenReturn(secHubModelJson);
        when(environment.getNewProjectTeamIdMapping()).thenReturn("teamMappingJsonFake");
        when(environment.getNewProjectPresetIdMapping()).thenReturn("presetMappingJsonFake");

        NamePatternIdProvider teamIdProvider = mock(NamePatternIdProvider.class);
        when(teamIdProvider.getIdForName(PROJECT1)).thenReturn("team-id");

        NamePatternIdProvider presetIdProvider = mock(NamePatternIdProvider.class);
        when(presetIdProvider.getIdForName(PROJECT1)).thenReturn("4711");

        when(providerFactory.createProvider(CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_TEAM_ID, "teamMappingJsonFake")).thenReturn(teamIdProvider);
        when(providerFactory.createProvider(CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_PRESET_ID, "presetMappingJsonFake")).thenReturn(presetIdProvider);

        /* execute */
        CheckmarxWrapperContext result = factoryToTest.create(environment);

        /* test */
        assertNotNull(result);

        assertEquals(4711L, result.getPresetIdForNewProjects());
        assertEquals("team-id", result.getTeamIdForNewProjects());
    }

    private String createValidSecHubModel() {
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setProjectId(PROJECT1);

        String secHubModelJson = JSONConverter.get().toJSON(model);
        return secHubModelJson;
    }

}
