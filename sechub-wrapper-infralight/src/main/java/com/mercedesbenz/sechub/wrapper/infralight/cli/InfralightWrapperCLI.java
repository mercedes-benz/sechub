// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.infralight.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InfralightWrapperCLI implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(InfralightWrapperCLI.class);

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Infralight wrapper starting");

    }

}
