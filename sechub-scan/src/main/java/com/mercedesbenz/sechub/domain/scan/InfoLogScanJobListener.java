// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.Profiles;

@Component
@Profile("!" + Profiles.INTEGRATIONTEST)
public class InfoLogScanJobListener implements ScanJobListener {

    private static final Logger LOG = LoggerFactory.getLogger(InfoLogScanJobListener.class);

    @Override
    public void started(UUID jobUUID, CanceableScanJob scan) {
        LOG.info("Job {} will be executed", jobUUID);
    }

    @Override
    public void ended(UUID jobUUID) {
        LOG.info("Job {} has ended", jobUUID);
    }

    @Override
    public void suspended(UUID jobUUID) {
        LOG.info("Job {} has been suspended", jobUUID);
    }

}
