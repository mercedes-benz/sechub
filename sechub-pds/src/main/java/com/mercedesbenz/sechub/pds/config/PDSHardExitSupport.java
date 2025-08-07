// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PDSHardExitSupport {

    private static final Logger logger = LoggerFactory.getLogger(PDSHardExitSupport.class);

    public void exit(int exitCode, String reason) {

        logger.info("Hard application exit. Code: {}, Reason: {}", exitCode, reason);

        System.exit(exitCode);
    }
}
