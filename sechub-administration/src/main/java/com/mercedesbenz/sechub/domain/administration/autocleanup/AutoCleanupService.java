package com.mercedesbenz.sechub.domain.administration.autocleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AutoCleanupService {

    private static final Logger LOG = LoggerFactory.getLogger(AutoCleanupService.class);

    public void cleanup(AutoCleanupConfig configuration) {
        LOG.info("Auto cleanup check starting");
        /* FIXME Albert Tregnaghi, 2022-02-21: implement */
    }

}
