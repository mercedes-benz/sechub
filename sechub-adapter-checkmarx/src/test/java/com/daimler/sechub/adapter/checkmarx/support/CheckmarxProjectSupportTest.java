// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx.support;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.checkmarx.CheckmarxConfig;
import com.daimler.sechub.adapter.checkmarx.CheckmarxConfig.CheckmarxConfigBuilder;
import com.daimler.sechub.adapter.checkmarx.CheckmarxConstants;
import com.daimler.sechub.adapter.checkmarx.CheckmarxEngineConfiguration;
import com.daimler.sechub.adapter.checkmarx.CheckmarxSastScanSettings;
import com.daimler.sechub.adapter.checkmarx.CheckmarxSessionData;
import com.daimler.sechub.adapter.checkmarx.support.CheckmarxProjectSupport.InternalUpdateContext;
import com.daimler.sechub.adapter.support.JSONAdapterSupport;

public class CheckmarxProjectSupportTest {
	private CheckmarxProjectSupport supportToTest;
	
	@Before
	public void before() throws Exception {
		supportToTest = new CheckmarxProjectSupport();
	}

	@Test
	public void test_data_can_be_extracted() throws AdapterException {
		/* prepare */
		String data = "[{\r\n" + 
				"    \"id\": 1234,\r\n" + 
				"    \"teamId\": \"xxxx-aax7-45asdf-b194-c736b605700d\",\r\n" + 
				"    \"name\": \"TheName\",\r\n" + 
				"    \"isPublic\": true,\r\n" + 
				"    \"customFields\": [],\r\n" + 
				"    \"links\": [\r\n" + 
				"      {\r\n" + 
				"        \"rel\": \"self\",\r\n" + 
				"        \"uri\": \"/projects/113\"\r\n" + 
				"      },\r\n" + 
				"      {\r\n" + 
				"        \"rel\": \"teams\",\r\n" + 
				"        \"uri\": \"/auth/teams/\"\r\n" + 
				"      },\r\n" + 
				"      {\r\n" + 
				"        \"rel\": \"latestscan\",\r\n" + 
				"        \"uri\": \"/sast/scans?projectId=12213&last=1\"\r\n" + 
				"      },\r\n" + 
				"      {\r\n" + 
				"        \"rel\": \"allscans\",\r\n" + 
				"        \"uri\": \"/sast/scans?projectId=12213\"\r\n" + 
				"      },\r\n" + 
				"      {\r\n" + 
				"        \"rel\": \"scansettings\",\r\n" + 
				"        \"uri\": \"/sast/scanSettings/1234\"\r\n" + 
				"      },\r\n" + 
				"      {\r\n" + 
				"        \"type\": \"local\",\r\n" + 
				"        \"rel\": \"source\",\r\n" + 
				"        \"uri\": null\r\n" + 
				"      }\r\n" + 
				"    ]\r\n" + 
				"  }\r\n" + 
				"]{Cache-Control=[no-cache], Pragma=[no-cache], Content-Type=[application/json; charset=utf-8], Expires=[-1], Server=[Microsoft-IIS/8.5], api-version=[2.0], X-AspNet-Version=[4.0.30319], X-Powered-By=[ASP.NET], Date=[Tue, 25 Sep 2018 14:06:42 GMT], Content-Length=[725]}";
		
		/* execute */
		CheckmarxSessionData result = supportToTest.extractFirstProjectFromJsonWithProjectArray(JSONAdapterSupport.FOR_UNKNOWN_ADAPTER,data);
		
		/* test */
		assertNotNull(result);
		assertEquals(1234L,result.getProjectId());
		assertEquals("TheName",result.getProjectName());
	}

	
    @Test
    public void engine_configurations_can_be_extracted() throws AdapterException {
        /* prepare */
        String json_data = "[\n" + 
                "   {\n" + 
                "     \"id\": 1,\n" + 
                "     \"name\": \"Default Configuration\"\n" + 
                "   },\n" + 
                "   {\n" + 
                "     \"id\": 2,\n" + 
                "     \"name\": \"Japanese (Shift-JIS)\"\n" + 
                "   },\n" + 
                "   {\n" + 
                "     \"id\": 3,\n" + 
                "     \"name\": \"Korean\"\n" + 
                "   },\n" + 
                "   {\n" + 
                "     \"id\": 5,\n" + 
                "     \"name\": \"Multi-language Scan\"\n" + 
                "   }\n" + 
                " ]";
        
        CheckmarxEngineConfiguration multiLanguageScanConfiguration = new CheckmarxEngineConfiguration();
        multiLanguageScanConfiguration.setId(5L);
        multiLanguageScanConfiguration.setName(CheckmarxConstants.DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME);
        
        /* execute */
        List<CheckmarxEngineConfiguration> engineConfigurations = supportToTest.extractEngineConfigurationsFromGet(json_data, JSONAdapterSupport.FOR_UNKNOWN_ADAPTER);
        
        /* test */
        assertThat(engineConfigurations, notNullValue());
        assertThat(engineConfigurations, not(empty()));
        assertThat(engineConfigurations, hasSize(4));
        assertThat(engineConfigurations, hasItem(multiLanguageScanConfiguration));
    }
    
    @Test
    public void findEngineConfigurationByName() {
        /* prepare */
        List<CheckmarxEngineConfiguration> engineConfigurations = new LinkedList<>();
        
        CheckmarxEngineConfiguration defaultConfiguration = new CheckmarxEngineConfiguration();
        defaultConfiguration.setId(1L);
        defaultConfiguration.setName("Default Configuration");
        
        CheckmarxEngineConfiguration testConfiguration = new CheckmarxEngineConfiguration();
        testConfiguration.setId(3L);
        testConfiguration.setName("test");
        
        CheckmarxEngineConfiguration multiLanguageScanConfiguration = new CheckmarxEngineConfiguration();
        multiLanguageScanConfiguration.setId(5L);
        multiLanguageScanConfiguration.setName(CheckmarxConstants.DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME);
        engineConfigurations.add(testConfiguration);
        engineConfigurations.add(defaultConfiguration);
        engineConfigurations.add(multiLanguageScanConfiguration);
        
        /* execute */
        CheckmarxEngineConfiguration actualEngineConfiguration = supportToTest.findEngineConfigurationByName(CheckmarxConstants.DEFAULT_CHECKMARX_ENGINECONFIGURATION_MULTILANGANGE_SCAN_NAME, engineConfigurations);
        
        /* test */
        assertThat(actualEngineConfiguration, notNullValue());
        assertThat(actualEngineConfiguration, is(multiLanguageScanConfiguration));
    }
    
    @Test
    public void updatePresetIdWhenSetInAdapterConfig_preset_id_set_in_sechub() {
        /* prepare */
        long secHubPresetId = 39203;
        long checkmarxPresetId = 1;
        
        CheckmarxConfigBuilder builder = createBuilderWithMandatoryParamatersSet();
        builder.setPresetIdForNewProjects(secHubPresetId);
        
        CheckmarxConfig config = builder.build();
        
        CheckmarxSastScanSettings checkmarxScanSettings = new CheckmarxSastScanSettings();
        checkmarxScanSettings.setPresetId(checkmarxPresetId);
        
        InternalUpdateContext updateContext = supportToTest.new InternalUpdateContext();
        
        /* execute */
        supportToTest.updatePresetIdWhenSetInAdapterConfig(config, checkmarxScanSettings, updateContext);
        
        /* test */
        assertThat(updateContext.isUpdateOfPresetIdNecessary(), is(true));
        assertThat(updateContext.getPresetId(), is(secHubPresetId));
        assertThat(updateContext.isUpdateNecessary(), is(true));
    }
    
    @Test
    public void updatePresetIdWhenSetInAdapterConfig_presetId_sechub_not_set__no_update_necessary() {
        /* prepare */
        long presetId = 443940;
        
        CheckmarxConfigBuilder builder = createBuilderWithMandatoryParamatersSet();
        builder.setPresetIdForNewProjects(null);
        
        CheckmarxConfig config = builder.build();
        
        CheckmarxSastScanSettings checkmarxScanSettings = new CheckmarxSastScanSettings();
        checkmarxScanSettings.setPresetId(presetId);
        
        InternalUpdateContext updateContext = supportToTest.new InternalUpdateContext();
        
        /* execute */
        supportToTest.updatePresetIdWhenSetInAdapterConfig(config, checkmarxScanSettings, updateContext);
        
        /* test */
        assertThat(updateContext.isUpdateOfPresetIdNecessary(), is(false));
        assertThat(updateContext.getPresetId(), is(presetId));
        assertThat(updateContext.isUpdateNecessary(), is(false));
    }
    
    @Test
    public void updateEngineCondfigurationIdWhenSecHubAndCheckmarxDiffer_same_engine_configuration_id() {
        /* prepare */
        String engineConfigurationName = "test-configuration";
        long engineConfigurationId = 5L;
        
        List<CheckmarxEngineConfiguration> engineConfigurations = new LinkedList<>();
        
        CheckmarxEngineConfiguration defaultConfiguration = new CheckmarxEngineConfiguration();
        defaultConfiguration.setId(1L);
        defaultConfiguration.setName("Default Configuration");
        
        CheckmarxEngineConfiguration testConfiguration = new CheckmarxEngineConfiguration();
        testConfiguration.setId(engineConfigurationId);
        testConfiguration.setName(engineConfigurationName);
        
        engineConfigurations.add(defaultConfiguration);
        engineConfigurations.add(testConfiguration);
        
        CheckmarxConfigBuilder builder = createBuilderWithMandatoryParamatersSet();
        builder.setEngineConfigurationName(engineConfigurationName);
        
        CheckmarxConfig config = builder.build();
        
        CheckmarxSastScanSettings checkmarxScanSettings = new CheckmarxSastScanSettings();
        checkmarxScanSettings.setEngineConfigurationId(engineConfigurationId);
        
        InternalUpdateContext updateContext = supportToTest.new InternalUpdateContext();
        
        /* execute */
        supportToTest.updateEngineCondfigurationIdWhenSecHubAndCheckmarxDiffer(config, engineConfigurations, checkmarxScanSettings,
                updateContext);

        /* test */
        assertThat(updateContext.isUpdateOfEngineConfigurationNecessary(), is(false));
        assertThat(updateContext.getEngineConfigurationId(), is(engineConfigurationId));
        assertThat(updateContext.isUpdateNecessary(), is(false));
    }
    
    @Test
    public void updateEngineCondfigurationIdWhenSecHubAndCheckmarxDiffer_different_engine_configuration_id() {
        /* prepare */
        String engineConfigurationName = "test-configuration";
        long engineConfigurationId = 5L;
        
        List<CheckmarxEngineConfiguration> engineConfigurations = new LinkedList<>();
        
        CheckmarxEngineConfiguration defaultConfiguration = new CheckmarxEngineConfiguration();
        defaultConfiguration.setId(1L);
        defaultConfiguration.setName("Default Configuration");
        
        CheckmarxEngineConfiguration testConfiguration = new CheckmarxEngineConfiguration();
        testConfiguration.setId(engineConfigurationId);
        testConfiguration.setName(engineConfigurationName);
        
        engineConfigurations.add(defaultConfiguration);
        engineConfigurations.add(testConfiguration);
        
        CheckmarxConfigBuilder builder = createBuilderWithMandatoryParamatersSet();
        builder.setEngineConfigurationName(engineConfigurationName);
        
        CheckmarxConfig config = builder.build();
        
        CheckmarxSastScanSettings checkmarxScanSettings = new CheckmarxSastScanSettings();
        checkmarxScanSettings.setEngineConfigurationId(defaultConfiguration.getId());
        
        InternalUpdateContext updateContext = supportToTest.new InternalUpdateContext();
        
        /* execute */
        supportToTest.updateEngineCondfigurationIdWhenSecHubAndCheckmarxDiffer(config, engineConfigurations, checkmarxScanSettings,
                updateContext);

        /* test */
        assertThat(updateContext.isUpdateOfEngineConfigurationNecessary(), is(true));
        assertThat(updateContext.getEngineConfigurationId(), is(engineConfigurationId));
        assertThat(updateContext.isUpdateNecessary(), is(true));
    }
    
    private CheckmarxConfigBuilder createBuilderWithMandatoryParamatersSet() {
        CheckmarxConfigBuilder builder = CheckmarxConfig.builder();
        builder.setUser("testuserId");
        builder.setPasswordOrAPIToken("testapitoken");
        builder.setProjectId("testprojectid");
        builder.setTeamIdForNewProjects("testteamid");
        return builder;
    }
}
