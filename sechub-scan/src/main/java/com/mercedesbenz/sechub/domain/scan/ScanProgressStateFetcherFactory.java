// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.ProgressStateFetcher;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;

@Component
public class ScanProgressStateFetcherFactory {

    @Autowired
    @Lazy
    DomainMessageService eventBus;

    public ProgressStateFetcher createProgressStateFetcher(UUID sechubJobUUID) {
        notNull(sechubJobUUID, "sechubJobUUID must be not null!");

        return new ScanProgressStateFetcher(eventBus, sechubJobUUID);
    }

}
