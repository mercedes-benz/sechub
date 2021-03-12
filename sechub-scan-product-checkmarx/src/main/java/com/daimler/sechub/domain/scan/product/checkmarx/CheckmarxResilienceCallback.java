// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import com.daimler.sechub.adapter.AdapterMetaData;
import com.daimler.sechub.adapter.checkmarx.CheckmarxMetaDataID;
import com.daimler.sechub.domain.scan.product.ProductExecutorContext;
import com.daimler.sechub.sharedkernel.resilience.ResilienceCallback;
import com.daimler.sechub.sharedkernel.resilience.ResilienceContext;

/**
 * A resilience callback which handles checkmarx 
 * @author Albert Tregnaghi
 *
 */
class CheckmarxResilienceCallback implements ResilienceCallback {

    private ProductExecutorContext executorContext;
    private boolean alwaysFullScanEnabled;

    public CheckmarxResilienceCallback(CheckmarxExecutorConfigSuppport configSupport, ProductExecutorContext executorContext) {
        this.alwaysFullScanEnabled = configSupport.isAlwaysFullScanEnabled();
        this.executorContext = executorContext;
    }

    @Override
    public void beforeRetry(ResilienceContext context) {
        handleCheckmarxFullScanFallback(context);
    }

    private void handleCheckmarxFullScanFallback(ResilienceContext context) {
        Boolean fallbackToFullScan = context.getValueOrNull(CheckmarxResilienceConsultant.CONTEXT_ID_FALLBACK_CHECKMARX_FULLSCAN);
        if (! Boolean.TRUE.equals(fallbackToFullScan)) {
            return;
        }
        CheckmarxProductExecutor.LOG.debug("fallback to checkmarx fullscan recognized, alwaysFullScanEnabled before:{}",alwaysFullScanEnabled);
        
        alwaysFullScanEnabled = true;
        
        CheckmarxProductExecutor.LOG.debug("fallback to checkmarx fullscan recognized, alwaysFullScanEnabled now:{}",alwaysFullScanEnabled);
        /*
         * we must remove the the old scan id inside metadata so the restart will do a
         * new scan and not reuse the old one! When we do not rest the file upload as well,
         * the next scan does complains about missing source locations
         */
        AdapterMetaData metaData = executorContext.getCurrentMetaDataOrNull();
        if (metaData != null) {
            String keyScanId = CheckmarxMetaDataID.KEY_SCAN_ID;
            String uploadKey = CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE;
            CheckmarxProductExecutor.LOG.debug("start reset checkmarx adapter meta data for {} and {}", keyScanId, uploadKey);
            metaData.setValue(keyScanId, null);
            metaData.setValue(uploadKey, null);
            
            executorContext.getCallback().persist(metaData);
            CheckmarxProductExecutor.LOG.debug("persisted checkmarx adapter meta data");
        }
        /*
         * we reset the context information, so former parts will only by triggered
         * again, when the problem really come up again.
         */
        context.setValue(CheckmarxResilienceConsultant.CONTEXT_ID_FALLBACK_CHECKMARX_FULLSCAN, null);
        
    }
    
    public boolean isAlwaysFullScanEnabled() {
        return alwaysFullScanEnabled;
    }
}