// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.commons.core.resilience.ResilienceCallback;
import com.mercedesbenz.sechub.commons.core.resilience.ResilienceContext;

/**
 * A resilience callback which handles checkmarx
 *
 * @author Albert Tregnaghi
 *
 */
public class CheckmarxResilienceCallback implements ResilienceCallback {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxResilienceCallback.class);

    private boolean alwaysFullScanEnabled;
    private AdapterMetaDataCallback metaDataCallback;

    public CheckmarxResilienceCallback(boolean alwaysFullScanEnabled, AdapterMetaDataCallback metaDataCallback) {
        this.alwaysFullScanEnabled = alwaysFullScanEnabled;
        this.metaDataCallback = metaDataCallback;
    }

    @Override
    public void beforeRetry(ResilienceContext context) {
        handleCheckmarxFullScanFallback(context);
    }

    private void handleCheckmarxFullScanFallback(ResilienceContext context) {
        Boolean fallbackToFullScan = context.getValueOrNull(CheckmarxResilienceConsultant.CONTEXT_ID_FALLBACK_CHECKMARX_FULLSCAN);
        if (!Boolean.TRUE.equals(fallbackToFullScan)) {
            return;
        }
        LOG.debug("fallback to checkmarx fullscan recognized, alwaysFullScanEnabled before:{}", alwaysFullScanEnabled);

        alwaysFullScanEnabled = true;

        LOG.debug("fallback to checkmarx fullscan recognized, alwaysFullScanEnabled now:{}", alwaysFullScanEnabled);
        /*
         * we must remove the the old scan id inside metadata so the restart will do a
         * new scan and not reuse the old one! When we do not reset the file upload as
         * well, the next scan does complains about missing source locations
         */
        AdapterMetaData metaData = metaDataCallback.getMetaDataOrNull();
        if (metaData != null) {
            String keyScanId = CheckmarxMetaDataID.KEY_SCAN_ID;
            String uploadKey = CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE;
            LOG.debug("start reset checkmarx adapter meta data for {} and {}", keyScanId, uploadKey);
            metaData.setValue(keyScanId, null);
            metaData.setValue(uploadKey, null);

            metaDataCallback.persist(metaData);
            LOG.debug("persisted checkmarx adapter meta data");
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