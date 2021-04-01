// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.AdapterMetaData;
import com.daimler.sechub.adapter.AdapterMetaDataCallback;
import com.daimler.sechub.adapter.checkmarx.CheckmarxMetaDataID;
import com.daimler.sechub.domain.scan.product.ProductExecutorContext;
import com.daimler.sechub.sharedkernel.resilience.ResilienceContext;

public class CheckmarxResilienceCallbackTest {

    private CheckmarxResilienceCallback callbackToTest;
    private ProductExecutorContext executorContext;
    private CheckmarxExecutorConfigSuppport configSupport;
    private AdapterMetaDataCallback adapterMetaDataCallback;
    private AdapterMetaData metaData;

    @Before
    public void before() throws Exception {
        configSupport = mock(CheckmarxExecutorConfigSuppport.class);
        executorContext = mock(ProductExecutorContext.class);
        adapterMetaDataCallback=mock(AdapterMetaDataCallback.class);
        
        when(executorContext.getCallback()).thenReturn(adapterMetaDataCallback);
        metaData = mock(AdapterMetaData.class);
        when(executorContext.getCurrentMetaDataOrNull()).thenReturn(metaData);
        
        callbackToTest = new CheckmarxResilienceCallback(configSupport,executorContext);
        
        /* check precondition */
        assertFalse(callbackToTest.isAlwaysFullScanEnabled());
        
    }
    @Test
    public void callbackToTest_has_initial_value_true_for_is_alwaysFullScanEnabled_when_configSupport_returns_true() {
        CheckmarxExecutorConfigSuppport configSupport2 =mock (CheckmarxExecutorConfigSuppport.class);
        when(configSupport2.isAlwaysFullScanEnabled()).thenReturn(true);
        
        /* prepare */
        callbackToTest = new CheckmarxResilienceCallback(configSupport2,executorContext);
        
        /* test */
        assertTrue(callbackToTest.isAlwaysFullScanEnabled());
        
    }

    @Test
    public void when_context_has_true_for_CHECKMARX_FALLBACK_TO_FULLSCAN__then_old_scan_id_and_fileupload_flag_are_set_to_null_and_pesisted_and_fallbackhint_removed() {
        /* prepare */
        ResilienceContext context = mock(ResilienceContext.class);
        when(context.getValueOrNull(CheckmarxResilienceConsultant.CONTEXT_ID_FALLBACK_CHECKMARX_FULLSCAN)).thenReturn(true);
        
        /* execute */
        callbackToTest.beforeRetry(context);
        
        /* test */
        assertTrue(callbackToTest.isAlwaysFullScanEnabled());
        
        verify(metaData).setValue(CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE, null);
        verify(metaData).setValue(CheckmarxMetaDataID.KEY_SCAN_ID, null);
        
        verify(adapterMetaDataCallback).persist(metaData);
        
        // also we check the callback does a reset of the fallback hint:
        verify(context).setValue(CheckmarxResilienceConsultant.CONTEXT_ID_FALLBACK_CHECKMARX_FULLSCAN, null);
    }
    
    @Test
    public void when_context_has_false_for_CHECKMARX_FALLBACK_TO_FULLSCAN__then_old_scan_id_and_fileupload_flag_are_NOT_set_to_null_and_meta_data_NOT_pesisted() {
        /* prepare */
        ResilienceContext context = mock(ResilienceContext.class);
        when(context.getValueOrNull(CheckmarxResilienceConsultant.CONTEXT_ID_FALLBACK_CHECKMARX_FULLSCAN)).thenReturn(false);
        
        /* execute */
        callbackToTest.beforeRetry(context);
        
        /* test */
        assertFalse(callbackToTest.isAlwaysFullScanEnabled());
        
        verify(metaData,never()).setValue(eq(CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE), any());
        verify(metaData,never()).setValue(eq(CheckmarxMetaDataID.KEY_SCAN_ID), any());
        
        verify(adapterMetaDataCallback,never()).persist(metaData);
        
        // also no reset done:
        verify(context,never()).setValue(eq(CheckmarxResilienceConsultant.CONTEXT_ID_FALLBACK_CHECKMARX_FULLSCAN), any());
        
    }

}
