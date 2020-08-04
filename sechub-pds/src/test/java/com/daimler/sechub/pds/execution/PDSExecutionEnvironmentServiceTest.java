package com.daimler.sechub.pds.execution;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.pds.config.PDSProductSetup;
import com.daimler.sechub.pds.config.PDSProdutParameterDefinition;
import com.daimler.sechub.pds.config.PDSServerConfigurationService;
import com.daimler.sechub.pds.job.PDSJobConfiguration;

public class PDSExecutionEnvironmentServiceTest {

    private PDSExecutionEnvironmentService serviceToTest;
    private PDSServerConfigurationService serverConfigService;
    private PDSKeyToEnvConverter converter;

    @Before
    public void before() throws Exception {
        converter = mock(PDSKeyToEnvConverter.class);
        serverConfigService = mock(PDSServerConfigurationService.class);

        serviceToTest = new PDSExecutionEnvironmentService();
        serviceToTest.converter = converter;
        serviceToTest.serverConfigService = serverConfigService;
    }

    @Test
    public void a_job_with_two_configured_keys_is_is_handling_them_but_third_own_is_ignored() {
        /* prepare */
        // create job configuration
        PDSJobConfiguration config = new PDSJobConfiguration();
        config.setProductId("productid1");
        PDSExecutionParameterEntry entry1 = new PDSExecutionParameterEntry();
        entry1.setKey("p1.keya");
        entry1.setValue("value1");
        PDSExecutionParameterEntry entry2 = new PDSExecutionParameterEntry();
        entry2.setKey("p1.keyb");
        entry2.setValue("value2");
        
        config.getParameters().add(entry1);
        config.getParameters().add(entry2);

        // create product setup configuration        
        PDSProductSetup setup = new PDSProductSetup();
        setup.setId("productid1");
        PDSProdutParameterDefinition def1 = new PDSProdutParameterDefinition();
        def1.setKey("p1.keya");
        PDSProdutParameterDefinition def2 = new PDSProdutParameterDefinition();
        def2.setKey("p1.keyb");
        
        setup.getParameters().getMandatory().add(def1);
        setup.getParameters().getOptional().add(def2);
        
        when(serverConfigService.getProductSetupOrNull("productid1")).thenReturn(setup);
        
        // fake key conversion
        when(converter.convertKeyToEnv("p1.keya")).thenReturn("KEY_A");
        when(converter.convertKeyToEnv("p1.keyb")).thenReturn("KEY_B");
        when(converter.convertKeyToEnv("p1.keyc.unknown")).thenReturn("KEY_C");
        
        /* execute */
        Map<String, String> result = serviceToTest.buildEnvironmentMap(config);
        
        /* test */
        assertEquals("value1",result.get("KEY_A"));
        assertEquals("value2",result.get("KEY_B"));
        assertEquals(null,result.get("KEY_C"));
        
    }

}
