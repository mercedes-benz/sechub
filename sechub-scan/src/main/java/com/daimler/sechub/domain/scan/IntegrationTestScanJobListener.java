package com.daimler.sechub.domain.scan;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.Profiles;

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
            LOG.debug("registered job:{}", jobUUID);
        }
    }

    @Override
    public void ended(UUID jobUUID) {
        synchronized (MONITOR) {
            map.remove(jobUUID);
            LOG.debug("unregistered job:{}", jobUUID);
        }
    }

    public void cancel(UUID jobUUID) {
        /* @formatter:off */
        /* TODO Albert Tregnaghi, handle cancel operation better and cluster proof
         * 2020-04-23: we should use spring batch job operations here - see restart job mechanism for details - instead of handling this way, or maybe in addition */
        /* 2020-04-29: spring boot batch is ... not as expected... see
         * https://stackoverflow.com/questions/48652785/nosuchjobexception-when-trying-to-restart-a-spring-batch-job
         * SimpleJobOperator stop works not expected - an exception is thrown/catch and logged. But at least "STOPPED" is set. Also abondon does setup JobRepository entries.
         * 
         * So correct way is to find job by JobExplorer, triger stop + abandon and let ScanJobExecutor check BatchStatus inside 
         * 
         */
        /* @formatter:on */
        LOG.debug("try to cancel job:{}", jobUUID);
        synchronized (MONITOR) {
            CanceableScanJob canceableScan = map.get(jobUUID);
            if (canceableScan == null) {
                LOG.warn("Was not able to cancel job:{}", jobUUID);
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
