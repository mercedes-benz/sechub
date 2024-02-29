// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.checkmarx;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxMetaDataID;
import com.mercedesbenz.sechub.commons.core.resilience.ResilienceContext;

public class CheckmarxResilienceCallbackTest {

    private CheckmarxResilienceCallback callbackToTest;
    private AdapterMetaDataCallback metaDataCallback;
    private AdapterMetaData metaData;

    @Before
    public void before() throws Exception {
        metaDataCallback = mock(AdapterMetaDataCallback.class);

        metaData = mock(AdapterMetaData.class);
        when(metaDataCallback.getMetaDataOrNull()).thenReturn(metaData);

        callbackToTest = new CheckmarxResilienceCallback(false, metaDataCallback);

        /* check precondition */
        assertFalse(callbackToTest.isAlwaysFullScanEnabled());

    }

    @Test
    public void callbackToTest_has_initial_value_true_for_is_alwaysFullScanEnabled_when_configSupport_returns_true() {

        /* prepare */
        callbackToTest = new CheckmarxResilienceCallback(true, metaDataCallback);

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

        verify(metaDataCallback).persist(metaData);

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

        verify(metaData, never()).setValue(eq(CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE), any());
        verify(metaData, never()).setValue(eq(CheckmarxMetaDataID.KEY_SCAN_ID), any());

        verify(metaDataCallback, never()).persist(metaData);

        // also no reset done:
        verify(context, never()).setValue(eq(CheckmarxResilienceConsultant.CONTEXT_ID_FALLBACK_CHECKMARX_FULLSCAN), any());

    }

}
