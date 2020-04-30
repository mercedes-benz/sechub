package com.daimler.sechub.domain.scan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;

@Component
public class ScanProgressMonitorFactory {
    
    @Autowired
    @Lazy
    DomainMessageService eventBus;
    
    public ScanProgressMonitor createProgressMonitor(long batchJobId) {
        return new ScanProgressMonitor(eventBus, batchJobId);
    }
    
    
}
