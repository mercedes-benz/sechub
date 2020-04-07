package com.daimler.sechub.domain.scan;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ScanJobService {

    private static final Logger LOG = LoggerFactory.getLogger(ScanJobService.class);

    private static Map<UUID, CanceableScanJob> map = new HashMap<>();
    private static final Object MONITOR = new Object();

    public void register(UUID jobUUID, CanceableScanJob scan) {
        synchronized(MONITOR) {
            map.put(jobUUID, scan);
            LOG.debug("registered job:{}",jobUUID);
        }
    }

    public void unregister(UUID jobUUID) {
        synchronized(MONITOR) {
            map.remove(jobUUID);
            LOG.debug("unregistered job:{}",jobUUID);
        }
    }

    public void cancel(UUID jobUUID) {
        LOG.debug("try to cancel job:{}",jobUUID);
        synchronized(MONITOR) {
            CanceableScanJob canceableScan = map.get(jobUUID);
            if (canceableScan == null) {
                LOG.warn("Was not able to cancel job:{}",jobUUID);
                return;
            }
            canceableScan.cancelScanJob();
        }
    }
    
    /**
     * Count all registered jobs (so means running jobs)
     * @return
     */
    public long countAll() {
        synchronized(MONITOR) {
            return map.keySet().size();
        }
    }
    
    public long cancelAll() {
        LOG.debug("try to cancel all jobs");
        long count=0;
        synchronized(MONITOR) {
            for (UUID jobUUID: map.keySet()) {
                cancel(jobUUID);
                count++;
            }
        }
        return count;
    }
}
