package com.daimler.sechub.domain.scan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.ProgressMonitor;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import static com.daimler.sechub.sharedkernel.util.Assert.*;

@Component
public class ScanProgressMonitorFactory {
    
    @Autowired
    @Lazy
    DomainMessageService eventBus;
    
    public ProgressMonitor createProgressMonitor(Long batchJobId) {
        notNull(batchJobId, "batchjob id must be not null!");
        
        return new ScanProgressMonitor(eventBus, batchJobId);
    }
    
    
}
