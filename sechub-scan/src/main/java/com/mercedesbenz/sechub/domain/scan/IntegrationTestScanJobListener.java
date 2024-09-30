// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.Profiles;

@Component
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestScanJobListener implements ScanJobListener {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestScanJobListener.class);

    private static Map<UUID, CanceableScanJob> map = new HashMap<>();
    private static final Object MONITOR = new Object();

    @Override
    public void started(UUID jobUUID, CanceableScanJob scan) {
        synchronized (MONITOR) {
            map.put(jobUUID, scan);
            LOG.debug("registered job: {}", jobUUID);
        }
    }

    @Override
    public void suspended(UUID jobUUID) {
        synchronized (MONITOR) {
            LOG.debug("informed about suspended job: {}", jobUUID);
        }
    }

    @Override
    public void ended(UUID jobUUID) {
        synchronized (MONITOR) {
            map.remove(jobUUID);
            LOG.debug("unregistered job: {}", jobUUID);
        }
    }

    public void cancel(UUID jobUUID) {
        /*
         * could be unnecessary - because now the normal cancel operation does also
         * cancel job state but this is faster and no batch job check at scheduler
         * domain is necessary (so reduce events). So we still keep this method / class
         * for integration tests.
         */
        LOG.debug("try to cancel job: {}", jobUUID);
        synchronized (MONITOR) {
            CanceableScanJob canceableScan = map.get(jobUUID);
            if (canceableScan == null) {
                LOG.warn("Was not able to cancel job: {}", jobUUID);
                return;
            }
            canceableScan.cancelScanJob();
        }
    }

    /**
     * Count all registered jobs (so means running jobs)
     *
     * @return
     */
    public long countAll() {
        synchronized (MONITOR) {
            return map.keySet().size();
        }
    }

    public long cancelAll() {
        LOG.debug("try to cancel all jobs");
        long count = 0;
        synchronized (MONITOR) {
            for (UUID jobUUID : map.keySet()) {
                cancel(jobUUID);
                count++;
            }
        }
        return count;
    }

}
