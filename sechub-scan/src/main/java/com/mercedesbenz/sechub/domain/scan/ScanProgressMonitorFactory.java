// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.ProgressMonitor;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;

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
