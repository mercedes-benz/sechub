// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext;
import com.mercedesbenz.sechub.adapter.mock.MockedAdapterSetupService;

class DelegatingMockablePDSAdapterV1Test {

    DelegatingMockablePDSAdapterV1 adapterToTest;
    private AdapterRuntimeContext runtimeContext;
    private PDSAdapterConfig config;
    private UUID jobUUID;
    private MockedAdapterSetupService setupService;
    private PDSAdapterV1 realPdsAdapter;
    private PDSAdapterConfigData data;

    @BeforeEach
    void beforeEach() {
        setupService = mock(MockedAdapterSetupService.class);
        PDSContextFactory contextFactory = mock(PDSContextFactory.class);
        adapterToTest = new DelegatingMockablePDSAdapterV1(setupService, contextFactory);
        // we simulate real PDS adapter - mocked adapter can be used as is*/
        realPdsAdapter = mock(PDSAdapterV1.class);
        adapterToTest.realPdsAdapterV1 = realPdsAdapter;

        jobUUID = UUID.randomUUID();
        data = mock(PDSAdapterConfigData.class);

        config = mock(PDSAdapterConfig.class);
        when(config.getPDSAdapterConfigData()).thenReturn(data);
        when(data.getSecHubJobUUID()).thenReturn(jobUUID);

        runtimeContext = new AdapterRuntimeContext();
    }

    @Test
    void when_nothing_special_real_pds_adapter_is_not_used() throws Exception {
        when(setupService.getSetupFor(any(), any())).thenReturn(null);
        /* execute */
        AdapterExecutionResult adapterResult = adapterToTest.execute(config, runtimeContext);

        /* test */
        verify(realPdsAdapter, never()).execute(config, runtimeContext);
        assertEquals("", adapterResult.getProductResult()); // no setup defined, so mocked adapter does always return ""
    }

    @Test
    void when_config_pds_mocking_disabled_real_pds_adapter_is_used() throws Exception {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("pds.mocking.disabled", "true");

        /* prepare */
        when(realPdsAdapter.execute(any(), any())).thenReturn(new AdapterExecutionResult("pseudo-test-result"));
        when(data.getJobParameters()).thenReturn(map);
        /* execute */
        AdapterExecutionResult adapterResult = adapterToTest.execute(config, runtimeContext);

        /* test */
        verify(realPdsAdapter, times(1)).execute(config, runtimeContext);
        assertEquals("pseudo-test-result", adapterResult.getProductResult());
    }

}
