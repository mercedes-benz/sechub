package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConstants;
import com.mercedesbenz.sechub.adapter.mock.MockDataIdentifierFactory;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProvider;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProviderFactory;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.pds.PDSUserMessageSupport;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperEnvironment;

class CheckmarxWrapperScanContextFactoryTest {

    private static final String PROJECT1 = "project1";
    private CheckmarxWrapperScanContextFactory factoryToTest;
    private CheckmarxWrapperEnvironment environment;
    private NamePatternIdProviderFactory providerFactory;
    private ArchiveSupport archiveSupport;
    private NamePatternIdProvider presetIdProvider;
    private NamePatternIdProvider teamIdProvider;
    private MockDataIdentifierFactory mockDataIdentifierFactory;
    private PDSUserMessageSupport messageSupport;

    @BeforeEach
    void beforeEach() {
        providerFactory = mock(NamePatternIdProviderFactory.class);
        environment = mock(CheckmarxWrapperEnvironment.class);

        presetIdProvider = mock(NamePatternIdProvider.class);
        teamIdProvider = mock(NamePatternIdProvider.class);
        archiveSupport = mock(ArchiveSupport.class);
        mockDataIdentifierFactory = mock(MockDataIdentifierFactory.class);
        messageSupport = mock(PDSUserMessageSupport.class);

        factoryToTest = new CheckmarxWrapperScanContextFactory();

        factoryToTest.providerFactory = providerFactory;
        factoryToTest.archiveSupport = archiveSupport;
        factoryToTest.mockDataIdentifierFactory = mockDataIdentifierFactory;
        factoryToTest.messageSupport = messageSupport;

        when(providerFactory.createProvider(eq(CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_PRESET_ID), any())).thenReturn(presetIdProvider);
        when(providerFactory.createProvider(eq(CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_TEAM_ID), any())).thenReturn(teamIdProvider);

    }

    @Test
    void created_context_has_fields() {
        /* prepare */
        String secHubModelJson = createValidSecHubModel();

        when(environment.getSechubConfigurationModelAsJson()).thenReturn(secHubModelJson);

        /* execute */
        CheckmarxWrapperScanContext result = factoryToTest.create(environment);

        /* test */
        assertNotNull(result.configuration);
        assertNotNull(result.environment);
        assertNotNull(result.presetIdProvider);
        assertNotNull(result.teamIdProvider);
        assertNotNull(result.archiveSupport);
        assertNotNull(result.mockDataIdentifierFactory);
        assertNotNull(result.messageSupport);

        assertEquals(teamIdProvider, result.teamIdProvider);
        assertEquals(presetIdProvider, result.presetIdProvider);
        assertEquals(mockDataIdentifierFactory, result.mockDataIdentifierFactory);
        assertEquals(archiveSupport, result.archiveSupport);
        assertEquals(environment, result.environment);
        assertEquals(messageSupport, result.messageSupport);
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
        /* prepare */
        String secHubModelJson = createValidSecHubModel();

        when(environment.getSechubConfigurationModelAsJson()).thenReturn(secHubModelJson);

        /* execute */
        CheckmarxWrapperScanContext result = factoryToTest.create(environment);

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
        CheckmarxWrapperScanContext result = factoryToTest.create(environment);

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
